package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {

    private final AnforaMain plugin;
    private final AnforaDataManager anforaDataManager;
    private final Map<UUID, String> openAnforas = new HashMap<>(); // Player UUID -> Anfora ID

    public GuiListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.anforaDataManager = plugin.getAnforaDataManager();
    }

    public void addPlayer(UUID playerUuid, String anforaId) {
        openAnforas.put(playerUuid, anforaId);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Anfora Resonante")) {
            openAnforas.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Anfora Resonante")) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        String anforaId = openAnforas.get(player.getUniqueId());
        if (anforaId == null) {
            player.closeInventory();
            player.sendMessage("§cError: No se pudo encontrar el ánfora asociada. Por favor, intenta abrirla de nuevo.");
            return;
        }

        AnforaData anforaData = anforaDataManager.loadAnfora(anforaId);
        if (anforaData == null) {
            player.closeInventory();
            player.sendMessage("§cError: El ánfora ya no existe.");
            return;
        }

        switch (slot) {
            // Deposit
            case 10: // Deposit 1 level
                depositExperience(player, anforaData, 1);
                break;
            case 11: // Deposit 5 levels
                depositExperience(player, anforaData, 5);
                break;
            case 12: // Deposit 10 levels
                depositExperience(player, anforaData, 10);
                break;

            // Withdraw
            case 14: // Withdraw 1 level
                withdrawExperience(player, anforaData, 1);
                break;
            case 15: // Withdraw 5 levels
                withdrawExperience(player, anforaData, 5);
                break;
            case 16: // Withdraw 10 levels
                withdrawExperience(player, anforaData, 10);
                break;

            // Upgrade
            case 4:
                player.sendMessage("La función de mejora aún no está implementada.");
                break;

            // Information (Ender Pearl or Book)
            case 13:
            case 22:
                // The information is already displayed on the item, so we don't need to do anything here.
                break;
        }
        // Update the GUI after an action
        plugin.getGuiManager().openAnforaGui(player, anforaData);
    }

    private void depositExperience(Player player, AnforaData anforaData, int levels) {
        if (player.getLevel() >= levels) {
            int expToDeposit = 0;
            for (int i = 0; i < levels; i++) {
                expToDeposit += getExpForLevel(player.getLevel() - i);
            }
            player.giveExp(-expToDeposit);
            anforaData.setExperience(anforaData.getExperience() + expToDeposit);
            anforaDataManager.saveAnfora(anforaData);
            player.sendMessage("Has depositado " + levels + " niveles de experiencia.");
        } else {
            player.sendMessage("No tienes suficientes niveles para depositar.");
        }
    }

    private void withdrawExperience(Player player, AnforaData anforaData, int levels) {
        int expToWithdraw = 0;
        for (int i = 0; i < levels; i++) {
            expToWithdraw += getExpForLevel(player.getLevel() + i);
        }

        if (anforaData.getExperience() >= expToWithdraw) {
            anforaData.setExperience(anforaData.getExperience() - expToWithdraw);
            player.giveExp(expToWithdraw);
            anforaDataManager.saveAnfora(anforaData);
            player.sendMessage("Has retirado " + levels + " niveles de experiencia.");
        } else {
            player.sendMessage("El ánfora no tiene suficiente experiencia.");
        }
    }

    private int getExpForLevel(int level) {
        if (level >= 31) {
            return 9 * level - 158;
        } else if (level >= 16) {
            return 5 * level - 38;
        } else {
            return 2 * level + 7;
        }
    }
}
