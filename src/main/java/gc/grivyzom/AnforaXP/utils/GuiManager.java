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

        // Fila 1: Item de mejora
        gui.setItem(4, createGuiItem(Material.NETHER_STAR, "§bMejorar Ánfora", "§7Aumenta la capacidad o el nivel."));

        // Fila 2: Botones de depósito y retiro
        gui.setItem(10, createGuiItem(Material.RED_DYE, "§cDepositar 1 Nivel"));
        gui.setItem(11, createGuiItem(Material.RED_DYE, "§cDepositar 5 Niveles"));
        gui.setItem(12, createGuiItem(Material.RED_DYE, "§cDepositar 10 Niveles"));

        gui.setItem(13, createGuiItem(Material.ENDER_PEARL, "§eCapacidad",
                "§7Experiencia: §a" + String.format("%.0f", anforaData.getExperience()),
                "§7Nivel: §e" + anforaData.getLevel()));

        gui.setItem(14, createGuiItem(Material.GREEN_DYE, "§aRetirar 1 Nivel"));
        gui.setItem(15, createGuiItem(Material.GREEN_DYE, "§aRetirar 5 Niveles"));
        gui.setItem(16, createGuiItem(Material.GREEN_DYE, "§aRetirar 10 Niveles"));

        // Fila 3: Información del ánfora
        gui.setItem(22, createGuiItem(Material.BOOK, "§dInformación",
                "§7Dueño: §e" + anforaData.getOwnerName(),
                "§7Nivel: §e" + anforaData.getLevel(),
                "§7Capacidad: §a" + "Placeholder")); // Placeholder for capacity

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
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
