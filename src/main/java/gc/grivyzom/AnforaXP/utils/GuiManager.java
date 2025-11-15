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

        // --- Static Items ---
        // Deposit buttons
        gui.setItem(10, createGuiItem(Material.RED_DYE, "§cDepositar 1 Nivel"));
        gui.setItem(11, createGuiItem(Material.RED_DYE, "§cDepositar 5 Niveles"));
        gui.setItem(12, createGuiItem(Material.RED_DYE, "§cDepositar 10 Niveles"));

        // Withdraw buttons
        gui.setItem(14, createGuiItem(Material.GREEN_DYE, "§aRetirar 1 Nivel"));
        gui.setItem(15, createGuiItem(Material.GREEN_DYE, "§aRetirar 5 Niveles"));
        gui.setItem(16, createGuiItem(Material.GREEN_DYE, "§aRetirar 10 Niveles"));


        // --- Dynamic Items (like in GuiListener.updateGui) ---
        // Ender Pearl (Capacity)
        LevelManager.LevelInfo levelInfo = LevelManager.getLevelInfo(anforaData.getLevel());
        int maxExperience = levelInfo.getMaxExperience();
        double currentExperience = anforaData.getExperience();
        double percentage = (maxExperience > 0) ? (currentExperience / maxExperience) * 100 : 0;

        gui.setItem(13, createGuiItem(Material.ENDER_PEARL, "§eCapacidad",
                "§7Experiencia: §a" + String.format("%.0f", currentExperience) + " / " + maxExperience,
                "§7Nivel: §e" + anforaData.getLevel(),
                "§7" + buildProgressBar(percentage)
        ));

        // Nether Star (Upgrade)
        int nextLevel = anforaData.getLevel() + 1;
        if (anforaData.getLevel() < LevelManager.getMaxLevel()) {
            LevelManager.LevelInfo nextLevelInfo = LevelManager.getLevelInfo(anforaData.getLevel());
            gui.setItem(4, createGuiItem(Material.NETHER_STAR, "§bMejorar a Nivel " + nextLevel,
                    "§7Costo: §c" + nextLevelInfo.getUpgradeCost() + " XP"));
        } else {
            gui.setItem(4, createGuiItem(Material.NETHER_STAR, "§bNivel Máximo Alcanzado", "§a¡Felicidades!"));
        }

        // Book (Info)
        gui.setItem(22, createGuiItem(Material.BOOK, "§dInformación",
                "§7Dueño: §e" + anforaData.getOwnerName(),
                "§7Nivel: §e" + anforaData.getLevel(),
                "§7Capacidad: §a" + maxExperience));

        player.openInventory(gui);
    }

    private String buildProgressBar(double percentage) {
        StringBuilder bar = new StringBuilder("§8[");
        int progress = (int) (percentage / 10); // 10 bars total
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
