package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.utils.EffectsManager;
import gc.grivyzom.AnforaXP.utils.ExperienceManager;
import gc.grivyzom.AnforaXP.utils.GuiManager;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
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
        String title = event.getView().getTitle();
        if (title.equals(GuiManager.ANFORA_GUI_TITLE) || title.equals("Anfora Resonante")) {
            openAnforas.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(GuiManager.ANFORA_GUI_TITLE) && !title.equals("Anfora Resonante")) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        String anforaId = openAnforas.get(player.getUniqueId());
        if (anforaId == null) {
            player.closeInventory();
            player.sendMessage(
                    "§cError: No se pudo encontrar el ánfora asociada. Por favor, intenta abrirla de nuevo.");
            return;
        }

        AnforaData anforaData = anforaDataManager.loadAnfora(anforaId);
        if (anforaData == null) {
            player.closeInventory();
            player.sendMessage("§cError: El ánfora ya no existe.");
            return;
        }

        boolean isAction = false;
        boolean shouldRefresh = false;

        switch (slot) {
            // === NUEVOS SLOTS (GUI de 45 slots) ===

            // Depositar (fila 4, slots 28-30)
            case 28: // Deposit 1 level
                depositExperience(player, anforaData, anforaId, 1);
                isAction = true;
                shouldRefresh = true;
                break;
            case 29: // Deposit 5 levels
                depositExperience(player, anforaData, anforaId, 5);
                isAction = true;
                shouldRefresh = true;
                break;
            case 30: // Deposit 10 levels
                depositExperience(player, anforaData, anforaId, 10);
                isAction = true;
                shouldRefresh = true;
                break;

            // Retirar (fila 4, slots 32-34)
            case 32: // Withdraw 1 level
                withdrawExperience(player, anforaData, anforaId, 1);
                isAction = true;
                shouldRefresh = true;
                break;
            case 33: // Withdraw 5 levels
                withdrawExperience(player, anforaData, anforaId, 5);
                isAction = true;
                shouldRefresh = true;
                break;
            case 34: // Withdraw 10 levels
                withdrawExperience(player, anforaData, anforaId, 10);
                isAction = true;
                shouldRefresh = true;
                break;

            // Mejorar ánfora (slot 13)
            case 13:
                if (LevelManager.levelUp(anforaData)) {
                    player.sendMessage("§a§l¡Ánfora mejorada al nivel " + anforaData.getLevel() + "!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        anforaDataManager.saveAnfora(anforaData);
                    });
                    isAction = true;
                    shouldRefresh = true;
                }
                break;

            // Cerrar (slot 40)
            case 40:
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                return;

            // === SLOTS LEGACY (GUI original de 27 slots) ===
            // Mantenidos para compatibilidad por si hay GUIs antiguos abiertos
            case 10: // Deposit 1 level (legacy)
                depositExperience(player, anforaData, anforaId, 1);
                isAction = true;
                shouldRefresh = true;
                break;
            case 11: // Deposit 5 levels (legacy)
                depositExperience(player, anforaData, anforaId, 5);
                isAction = true;
                shouldRefresh = true;
                break;
            case 12: // Deposit 10 levels (legacy)
                depositExperience(player, anforaData, anforaId, 10);
                isAction = true;
                shouldRefresh = true;
                break;
            case 14: // Withdraw 1 level (legacy)
                withdrawExperience(player, anforaData, anforaId, 1);
                isAction = true;
                shouldRefresh = true;
                break;
            case 15: // Withdraw 5 levels (legacy)
                withdrawExperience(player, anforaData, anforaId, 5);
                isAction = true;
                shouldRefresh = true;
                break;
            case 16: // Withdraw 10 levels (legacy)
                withdrawExperience(player, anforaData, anforaId, 10);
                isAction = true;
                shouldRefresh = true;
                break;
            case 4: // Level up (legacy)
                if (LevelManager.levelUp(anforaData)) {
                    player.sendMessage("§a§l¡Ánfora mejorada al nivel " + anforaData.getLevel() + "!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        anforaDataManager.saveAnfora(anforaData);
                    });
                    isAction = true;
                    shouldRefresh = true;
                }
                break;
        }

        if (isAction) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }

        // Refrescar GUI completo en lugar de actualización parcial
        if (shouldRefresh) {
            // Re-abrir el GUI para mostrar los cambios
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Recargar datos actualizados
                AnforaData refreshedData = anforaDataManager.loadAnfora(anforaId);
                if (refreshedData != null) {
                    plugin.getGuiManager().openAnforaGui(player, refreshedData);
                }
            }, 1L);
        }
    }

    private Location getAnforaLocationFromId(String anforaId) {
        try {
            String[] parts = anforaId.split("_");
            if (parts.length != 4)
                return null;

            String worldName = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            return new Location(Bukkit.getWorld(worldName), x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    private void depositExperience(Player player, AnforaData anforaData, String anforaId, int levels) {
        if (player.getLevel() < levels) {
            player.sendMessage("§cNo tienes suficientes niveles para depositar.");
            return;
        }

        int playerCurrentExp = ExperienceManager.getTotalExperience(player);
        int playerTargetExp = ExperienceManager.getTotalExperience(player.getLevel() - levels, 0);
        int expToDeposit = playerCurrentExp - playerTargetExp;

        if (expToDeposit <= 0)
            return;

        int actualAmountDeposited = LevelManager.addExperience(anforaData, expToDeposit);

        if (actualAmountDeposited > 0) {
            ExperienceManager.setTotalExperience(player, playerCurrentExp - actualAmountDeposited);

            // Log transaction
            plugin.getTransactionManager().logDeposit(
                    player.getUniqueId(),
                    anforaId,
                    actualAmountDeposited);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                anforaDataManager.saveAnfora(anforaData);
            });

            Location anforaLocation = getAnforaLocationFromId(anforaId);
            if (anforaLocation != null) {
                EffectsManager.playDepositEffect(player, anforaLocation);
            }

            player.sendMessage("§a✓ Has depositado §e" + actualAmountDeposited + "§a puntos de experiencia.");
            if (actualAmountDeposited < expToDeposit) {
                player.sendMessage("§cEl ánfora ha alcanzado su capacidad máxima para este nivel.");
            }
        } else {
            player.sendMessage("§cEl ánfora está llena. ¡Mejórala para aumentar su capacidad!");
        }
    }

    private void withdrawExperience(Player player, AnforaData anforaData, String anforaId, int levels) {
        int playerCurrentExp = ExperienceManager.getTotalExperience(player);
        int playerTargetExp = ExperienceManager.getTotalExperience(player.getLevel() + levels, 0);
        int expToWithdraw = playerTargetExp - playerCurrentExp;

        if (anforaData.getExperience() >= expToWithdraw) {
            anforaData.setExperience(anforaData.getExperience() - expToWithdraw);
            ExperienceManager.setTotalExperience(player, playerCurrentExp + expToWithdraw);

            // Log transaction
            plugin.getTransactionManager().logWithdraw(
                    player.getUniqueId(),
                    anforaId,
                    expToWithdraw);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                anforaDataManager.saveAnfora(anforaData);
            });

            Location anforaLocation = getAnforaLocationFromId(anforaId);
            if (anforaLocation != null) {
                EffectsManager.playWithdrawEffect(player, anforaLocation);
            }

            player.sendMessage("§a✓ Has retirado §e" + expToWithdraw + "§a puntos de experiencia.");
        } else {
            player.sendMessage("§cEl ánfora no tiene suficiente experiencia.");
        }
    }
}
