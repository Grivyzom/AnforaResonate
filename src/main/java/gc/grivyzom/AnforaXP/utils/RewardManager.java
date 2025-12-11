package gc.grivyzom.AnforaXP.utils;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.AnforaType; // Import AnforaType
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RewardManager {

    private static final AnforaMain plugin = JavaPlugin.getPlugin(AnforaMain.class);
    private static final Map<Integer, List<String>> rewardsMap = new HashMap<>();

    private RewardManager() {
        // Static utility class
    }

    /**
     * Carga las recompensas desde rewards.yml.
     */
    public static void loadRewards() {
        rewardsMap.clear();
        File rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        if (!rewardsFile.exists()) {
            plugin.getLogger().warning("rewards.yml no encontrado. No se cargarán recompensas.");
            return;
        }

        FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        ConfigurationSection levelsSection = rewardsConfig.getConfigurationSection("levels");

        if (levelsSection == null) {
            return;
        }

        for (String levelKey : levelsSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(levelKey);
                List<String> commands = levelsSection.getStringList(levelKey);
                if (commands != null && !commands.isEmpty()) {
                    rewardsMap.put(level, commands);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Clave de nivel inválida en rewards.yml: '" + levelKey + "'. Se omitirá.");
            }
        }
        plugin.getLogger().info("Se han cargado recompensas para " + rewardsMap.size() + " niveles.");
    }

    /**
     * Otorga las recompensas de un nivel a un jugador.
     * @param player El jugador que recibirá las recompensas.
     * @param level El nivel alcanzado.
     */
    public static void giveRewards(Player player, int level) {
        if (!rewardsMap.containsKey(level)) {
            return;
        }

        List<String> commands = rewardsMap.get(level);
        for (String command : commands) {
            String processedCommand = command
                    .replace("%player%", player.getName())
                    .replace("%uuid%", player.getUniqueId().toString())
                    .replace("%level%", String.valueOf(level));
            
            // Ejecutar el comando desde la consola
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }

    /**
     * Obtiene un AnforaType aleatorio basado en las configuraciones de anfora-types.yml.
     * @return Un AnforaType aleatorio, o AnforaType.COMMON si no hay tipos válidos configurados.
     */
    public static AnforaType getRandomAnforaType() {
        FileConfiguration anforaTypesConfig = AnforaMain.getAnforaTypesConfig();
        ConfigurationSection typesSection = anforaTypesConfig.getConfigurationSection("anfora_types");

        if (typesSection == null || typesSection.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("No se encontraron tipos de ánfora configurados en anfora-types.yml. Se usará AnforaType.COMMON por defecto.");
            return AnforaType.COMMON;
        }

        Map<AnforaType, Integer> weightedTypes = new HashMap<>();
        int totalWeight = 0;

        for (String typeName : typesSection.getKeys(false)) {
            try {
                AnforaType anforaType = AnforaType.valueOf(typeName.toUpperCase());
                int weight = typesSection.getInt(typeName);
                if (weight > 0) {
                    weightedTypes.put(anforaType, weight);
                    totalWeight += weight;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Tipo de ánfora inválido en anfora-types.yml: '" + typeName + "'. Se omitirá.");
            }
        }

        if (weightedTypes.isEmpty() || totalWeight <= 0) {
            plugin.getLogger().warning("No hay tipos de ánfora válidos con peso positivo configurados. Se usará AnforaType.COMMON por defecto.");
            return AnforaType.COMMON;
        }

        int randomIndex = (int) (Math.random() * totalWeight);
        for (Map.Entry<AnforaType, Integer> entry : weightedTypes.entrySet()) {
            randomIndex -= entry.getValue();
            if (randomIndex < 0) {
                return entry.getKey();
            }
        }

        // Fallback en caso de que algo salga mal (nunca debería ocurrir si totalWeight es > 0)
        return AnforaType.COMMON;
    }
}
