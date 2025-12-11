package gc.grivyzom.AnforaXP.utils;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class LevelManager {

    private static final AnforaMain plugin = JavaPlugin.getPlugin(AnforaMain.class);
    private static final TreeMap<Integer, Integer> xpToLevelMap = new TreeMap<>();
    private static final TreeMap<Integer, Integer> levelToXpMap = new TreeMap<>();
    private static int maxLevel = 1;

    public static void loadLevels() {
        xpToLevelMap.clear();
        levelToXpMap.clear();
        ConfigurationSection levelSection = plugin.getConfig().getConfigurationSection("leveling.xp-per-level");
        if (levelSection == null) {
            plugin.getLogger().severe("La sección 'leveling.xp-per-level' no se encontró en config.yml. El sistema de niveles no funcionará.");
            xpToLevelMap.put(0, 1);
            levelToXpMap.put(1, 0);
            maxLevel = 1;
            return;
        }

        for (String levelKey : levelSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(levelKey);
                int xpRequired = levelSection.getInt(levelKey);
                xpToLevelMap.put(xpRequired, level);
                levelToXpMap.put(level, xpRequired);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Clave de nivel inválida en config.yml: '" + levelKey + "'. Se omitirá.");
            }
        }

        if (xpToLevelMap.isEmpty()) {
            plugin.getLogger().severe("No se cargaron datos de nivel desde config.yml. Añadiendo un nivel por defecto.");
            xpToLevelMap.put(0, 1);
            levelToXpMap.put(1, 0);
        }

        maxLevel = Collections.max(levelToXpMap.keySet());
        plugin.getLogger().info("Sistema de niveles cargado. " + levelToXpMap.size() + " niveles definidos. Nivel máximo: " + maxLevel);
    }

    public static int getMaxLevel() {
        return maxLevel;
    }

    public static int getLevelFromXp(int totalXp) {
        Map.Entry<Integer, Integer> entry = xpToLevelMap.floorEntry(totalXp);
        return entry != null ? entry.getValue() : 1;
    }

    public static int getXpForLevel(int level) {
        return levelToXpMap.getOrDefault(level, -1);
    }
    
    public static int getXpCapacityForLevel(int level) {
        if (level >= maxLevel) {
            return getXpForLevel(maxLevel); 
        }
        return getXpForLevel(level + 1);
    }

    public static boolean canLevelUp(AnforaData anfora) {
        if (anfora.getLevel() >= maxLevel) return false;
        int xpRequired = getXpCapacityForLevel(anfora.getLevel());
        return anfora.getExperience() >= xpRequired;
    }

    public static boolean levelUp(AnforaData anfora) {
        if (!canLevelUp(anfora)) return false;

        int oldLevel = anfora.getLevel();
        int newLevel = oldLevel + 1;

        anfora.setLevel(newLevel);
        EffectsManager.playLevelUpEffect(anfora.getLocation());

        Player owner = Bukkit.getPlayer(anfora.getOwnerUUID());
        if (owner != null && owner.isOnline()) {
            RewardManager.giveRewards(owner, newLevel);
        }
        return true;
    }

    public static int addExperience(AnforaData anfora, int amountToAdd) {
        int currentXp = anfora.getExperience();
        int currentLevel = anfora.getLevel();

        // Capacidad del nivel actual (XP necesaria para el siguiente nivel)
        int maxCapacityForCurrentLevel = getXpCapacityForLevel(currentLevel);

        if (currentXp >= maxCapacityForCurrentLevel) {
            return 0;
        }

        int spaceAvailable = maxCapacityForCurrentLevel - currentXp;
        int actualAmountToAdd = Math.min(amountToAdd, spaceAvailable);

        if (actualAmountToAdd <= 0) {
            return 0;
        }
        
        int newXp = currentXp + actualAmountToAdd;
        anfora.setExperience(newXp);

        // Eliminada la subida de nivel automática
        
        if (actualAmountToAdd > 0) {
            // HologramManager.updateHologram(anfora);
        }
        
        return actualAmountToAdd;
    }
}