package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.utils.AdminGuiManager;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class AdminGuiListener implements Listener {

    private final AnforaMain plugin;
    private final AdminGuiManager adminGuiManager;

    public AdminGuiListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.adminGuiManager = new AdminGuiManager(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Verificar si es alguno de nuestros menús
        if (!title.equals(AdminGuiManager.MAIN_MENU_TITLE) &&
                !title.equals(AdminGuiManager.LOCATIONS_MENU_TITLE) &&
                !title.equals(AdminGuiManager.UPGRADES_MENU_TITLE) &&
                !title.equals(AdminGuiManager.TRANSACTIONS_MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Material type = clickedItem.getType();
        int slot = event.getSlot();

        // === MENÚ PRINCIPAL ===
        if (title.equals(AdminGuiManager.MAIN_MENU_TITLE)) {
            handleMainMenuClick(player, type, slot);
            return;
        }

        // === MENÚ DE UBICACIONES ===
        if (title.equals(AdminGuiManager.LOCATIONS_MENU_TITLE)) {
            handleLocationsMenuClick(player, clickedItem, type, slot);
            return;
        }

        // === MENÚ DE MEJORAS ===
        if (title.equals(AdminGuiManager.UPGRADES_MENU_TITLE)) {
            handleUpgradesMenuClick(player, type, slot);
            return;
        }

        // === MENÚ DE TRANSACCIONES ===
        if (title.equals(AdminGuiManager.TRANSACTIONS_MENU_TITLE)) {
            handleTransactionsMenuClick(player, type, slot);
        }
    }

    private void handleMainMenuClick(Player player, Material type, int slot) {
        // Brújula - Ubicaciones (slot 11)
        if (type == Material.COMPASS || slot == 11) {
            adminGuiManager.openLocationsMenu(player);
            return;
        }

        // Nether Star - Mejoras (slot 13)
        if (type == Material.NETHER_STAR || slot == 13) {
            adminGuiManager.openUpgradesMenu(player);
            return;
        }

        // Papel - Transacciones (slot 15)
        if (type == Material.PAPER || slot == 15) {
            adminGuiManager.openTransactionsMenu(player);
            return;
        }

        // Barrier - Cerrar (slot 31)
        if (type == Material.BARRIER || slot == 31) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    private void handleLocationsMenuClick(Player player, ItemStack clickedItem, Material type, int slot) {
        // Botón volver (slot 49)
        if (type == Material.ARROW || slot == 49) {
            adminGuiManager.openMainMenu(player);
            return;
        }

        // Click en un ánfora - Teleport
        if (type == Material.DECORATED_POT) {
            if (clickedItem.hasItemMeta() &&
                    clickedItem.getItemMeta().getPersistentDataContainer().has(ItemFactory.ANFORA_UNIQUE_ID_KEY,
                            PersistentDataType.STRING)) {

                String uuidStr = clickedItem.getItemMeta().getPersistentDataContainer()
                        .get(ItemFactory.ANFORA_UNIQUE_ID_KEY, PersistentDataType.STRING);
                try {
                    UUID anforaUUID = UUID.fromString(uuidStr);
                    AnforaData anforaData = plugin.getAnforaDataManager().getAnforaByUUID(anforaUUID);

                    if (anforaData != null && anforaData.getLocation() != null) {
                        player.closeInventory();
                        player.teleport(anforaData.getLocation().clone().add(0.5, 1, 0.5));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        player.sendMessage(plugin.getMessageManager().getMessage("teleport_to_anfora"));
                    } else {
                        player.sendMessage(plugin.getMessageManager().getMessage("anfora_not_found"));
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getMessageManager().getMessage("invalid_uuid"));
                }
            }
        }
    }

    private void handleUpgradesMenuClick(Player player, Material type, int slot) {
        // Botón volver (slot 22)
        if (type == Material.ARROW || slot == 22) {
            adminGuiManager.openMainMenu(player);
        }
    }

    private void handleTransactionsMenuClick(Player player, Material type, int slot) {
        // Botón volver (slot 49)
        if (type == Material.ARROW || slot == 49) {
            adminGuiManager.openMainMenu(player);
        }
    }
}
