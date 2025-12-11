package gc.grivyzom.AnforaXP.utils;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GuiManager {

    private final AnforaMain plugin;

    public GuiManager(AnforaMain plugin) {
        this.plugin = plugin;
    }

    public void openAnforaGui(Player player, AnforaData anforaData) {
        Inventory gui = Bukkit.createInventory(null, 27, "Anfora Resonante");

        // Static Items
        gui.setItem(10, createGuiItem(Material.RED_DYE, "§cDepositar 1 Nivel"));
        gui.setItem(11, createGuiItem(Material.RED_DYE, "§cDepositar 5 Niveles"));
        gui.setItem(12, createGuiItem(Material.RED_DYE, "§cDepositar 10 Niveles"));
        gui.setItem(14, createGuiItem(Material.GREEN_DYE, "§aRetirar 1 Nivel"));
        gui.setItem(15, createGuiItem(Material.GREEN_DYE, "§aRetirar 5 Niveles"));
        gui.setItem(16, createGuiItem(Material.GREEN_DYE, "§aRetirar 10 Niveles"));

        // Dynamic Items
        int currentLevel = anforaData.getLevel();
        int currentXp = anforaData.getExperience();
        int xpForCurrentLevel = LevelManager.getXpForLevel(currentLevel);
        int xpForNextLevel = LevelManager.getXpCapacityForLevel(currentLevel);

        double progressPercentage = 0;
        String progressLore;
        
        if (currentLevel >= LevelManager.getMaxLevel()) {
            progressPercentage = 100;
            progressLore = "§a¡NIVEL MÁXIMO ALCANZADO!";
        } else {
            int xpInThisLevel = currentXp - xpForCurrentLevel;
            int xpNeededForThisLevel = xpForNextLevel - xpForCurrentLevel;
            if (xpNeededForThisLevel > 0) {
                progressPercentage = ((double) xpInThisLevel / xpNeededForThisLevel) * 100;
            }
            progressLore = "§7" + xpInThisLevel + " / " + xpNeededForThisLevel;
        }

        // Slot 13: Experience Bottle with Progress and User XP
        int playerTotalXp = ExperienceManager.getTotalExperience(player);
        ItemStack progressItem = createGuiItem(Material.EXPERIENCE_BOTTLE, "§eProgreso de Experiencia",
                progressLore,
                "§7" + buildProgressBar(progressPercentage),
                "",
                "§7Tu Experiencia: §a" + playerTotalXp
        );
        gui.setItem(13, progressItem);

        // Slot 4: Nether Star (Upgrade Button or Info)
        ItemStack levelItem;
        if (LevelManager.canLevelUp(anforaData)) {
            levelItem = createGuiItem(Material.NETHER_STAR, "§a¡Mejorar Ánfora!",
                    "§7Haz click para mejorar el ánfora",
                    "§7al nivel " + (currentLevel + 1)
            );
        } else {
            levelItem = createGuiItem(Material.NETHER_STAR, "§bNivel del Ánfora", "§eNivel " + currentLevel);
        }
        gui.setItem(4, levelItem);

        gui.setItem(22, createGuiItem(Material.BOOK, "§dInformación General",
                "§7Dueño: §e" + anforaData.getOwnerName(),
                "§7Nivel: §e" + currentLevel,
                "§7XP Total: §a" + currentXp));

        player.openInventory(gui);
        plugin.getGuiListener().addPlayer(player.getUniqueId(), anforaData.getId());
    }

    private String buildProgressBar(double percentage) {
        StringBuilder bar = new StringBuilder("§8[");
        int progress = (int) (percentage / 10);
        for (int i = 0; i < 10; i++) {
            if (i < progress) {
                bar.append("§a=");
            } else {
                bar.append("§7=");
            }
        }
        bar.append("§8] §r").append(String.format("%.1f%%", percentage));
        return bar.toString();
    }

    public ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}