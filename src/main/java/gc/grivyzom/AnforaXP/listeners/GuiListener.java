package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.utils.ExperienceManager;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import gc.grivyzom.AnforaXP.utils.ParticleAnimations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

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

        boolean isAction = false;
        switch (slot) {
            // Deposit
            case 10: // Deposit 1 level
                depositExperience(player, anforaData, anforaId, 1);
                isAction = true;
                break;
            case 11: // Deposit 5 levels
                depositExperience(player, anforaData, anforaId, 5);
                isAction = true;
                break;
            case 12: // Deposit 10 levels
                depositExperience(player, anforaData, anforaId, 10);
                isAction = true;
                break;

            // Withdraw
            case 14: // Withdraw 1 level
                withdrawExperience(player, anforaData, anforaId, 1);
                isAction = true;
                break;
            case 15: // Withdraw 5 levels
                withdrawExperience(player, anforaData, anforaId, 5);
                isAction = true;
                break;
            case 16: // Withdraw 10 levels
                withdrawExperience(player, anforaData, anforaId, 10);
                isAction = true;
                break;

            // Upgrade
            case 4:
                handleUpgrade(player, anforaData);
                isAction = true; // Sound will be handled inside handleUpgrade
                break;

            // Information (Ender Pearl or Book)
            case 13:
            case 22:
                // The information is already displayed on the item, so we don't need to do anything here.
                break;
        }

        if (isAction) {
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }

        // Update the GUI after an action
        updateGui(player, anforaData);
    }

    private void handleUpgrade(Player player, AnforaData anforaData) {
        int currentLevel = anforaData.getLevel();
        if (currentLevel >= LevelManager.getMaxLevel()) {
            player.sendMessage("§cEl ánfora ya ha alcanzado el nivel máximo.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            return;
        }

        LevelManager.LevelInfo levelInfo = LevelManager.getLevelInfo(currentLevel);
        int upgradeCost = levelInfo.getUpgradeCost();

        if (anforaData.getExperience() >= upgradeCost) {
            // Actualizar datos en memoria (operación rápida)
            anforaData.setExperience(anforaData.getExperience() - upgradeCost);
            anforaData.setLevel(currentLevel + 1);
            
            // Guardar en DB de forma asíncrona (no bloquea el main thread)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                anforaDataManager.saveAnfora(anforaData);
            });

            player.sendMessage("§a¡Has mejorado el ánfora al nivel " + (currentLevel + 1) + "!");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        } else {
            player.sendMessage("§cNo tienes suficiente experiencia en el ánfora para mejorarla. Necesitas " + upgradeCost + " XP.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
        }
    }

    private void updateGui(Player player, AnforaData anforaData) {
        org.bukkit.inventory.InventoryView openInventoryView = player.getOpenInventory();
        if (openInventoryView == null || !openInventoryView.getTitle().equals("Anfora Resonante")) {
            return;
        }
        Inventory openInventory = openInventoryView.getTopInventory();

        // Update Ender Pearl (Capacity)
        LevelManager.LevelInfo levelInfo = LevelManager.getLevelInfo(anforaData.getLevel());
        int maxExperience = levelInfo.getMaxExperience();
        double currentExperience = anforaData.getExperience();
        double percentage = (maxExperience > 0) ? (currentExperience / maxExperience) * 100 : 0;

        openInventory.setItem(13, plugin.getGuiManager().createGuiItem(org.bukkit.Material.ENDER_PEARL, "§eCapacidad",
                "§7Experiencia: §a" + String.format("%.0f", currentExperience) + " / " + maxExperience,
                "§7Nivel: §e" + anforaData.getLevel(),
                "§7" + buildProgressBar(percentage)
        ));

        // Update Nether Star (Upgrade)
        int nextLevel = anforaData.getLevel() + 1;
        if (anforaData.getLevel() < LevelManager.getMaxLevel()) {
            LevelManager.LevelInfo nextLevelInfo = LevelManager.getLevelInfo(anforaData.getLevel());
            openInventory.setItem(4, plugin.getGuiManager().createGuiItem(org.bukkit.Material.NETHER_STAR, "§bMejorar a Nivel " + nextLevel,
                    "§7Costo: §c" + nextLevelInfo.getUpgradeCost() + " XP"));
        } else {
            openInventory.setItem(4, plugin.getGuiManager().createGuiItem(org.bukkit.Material.NETHER_STAR, "§bNivel Máximo Alcanzado", "§a¡Felicidades!"));
        }

        // Update Book (Info)
        openInventory.setItem(22, plugin.getGuiManager().createGuiItem(org.bukkit.Material.BOOK, "§dInformación",
                "§7Dueño: §e" + anforaData.getOwnerName(),
                "§7Nivel: §e" + anforaData.getLevel(),
                "§7Capacidad: §a" + maxExperience));
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

    /**
     * Obtiene la ubicación del ánfora desde el anforaId
     * @param anforaId ID del ánfora en formato "world_x_y_z"
     * @return Location del ánfora o null si no se puede parsear
     */
    private Location getAnforaLocationFromId(String anforaId) {
        try {
            String[] parts = anforaId.split("_");
            if (parts.length != 4) return null;
            
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

        LevelManager.LevelInfo levelInfo = LevelManager.getLevelInfo(anforaData.getLevel());
        double spaceAvailable = levelInfo.getMaxExperience() - anforaData.getExperience();

        if (spaceAvailable <= 0) {
            player.sendMessage("§cEl ánfora está llena.");
            return;
        }

        int playerCurrentExp = ExperienceManager.getTotalExperience(player);
        int playerTargetExp = ExperienceManager.getTotalExperience(player.getLevel() - levels, 0);
        int expToDeposit = playerCurrentExp - playerTargetExp;

        int actualAmountToDeposit = (int) Math.min(expToDeposit, spaceAvailable);

        if (actualAmountToDeposit <= 0) {
            player.sendMessage("§cEl ánfora no tiene suficiente capacidad para almacenar esa cantidad de experiencia.");
            return;
        }

        // Actualizar datos en memoria (operación rápida)
        anforaData.addExperience(actualAmountToDeposit);
        ExperienceManager.setTotalExperience(player, playerCurrentExp - actualAmountToDeposit);
        
        // Guardar en DB de forma asíncrona (no bloquea el main thread)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            anforaDataManager.saveAnfora(anforaData);
        });

        // Reproducir animación de partículas al depositar
        Location anforaLocation = getAnforaLocationFromId(anforaId);
        if (anforaLocation != null) {
            ParticleAnimations.playDepositAnimation(plugin, player.getLocation(), anforaLocation);
        }

        player.sendMessage("§aHas depositado " + actualAmountToDeposit + " puntos de experiencia.");
    }

    private void withdrawExperience(Player player, AnforaData anforaData, String anforaId, int levels) {
        int playerCurrentExp = ExperienceManager.getTotalExperience(player);
        int playerTargetExp = ExperienceManager.getTotalExperience(player.getLevel() + levels, 0);
        int expToWithdraw = playerTargetExp - playerCurrentExp;

        if (anforaData.getExperience() >= expToWithdraw) {
            // Actualizar datos en memoria (operación rápida)
            anforaData.setExperience(anforaData.getExperience() - expToWithdraw);
            ExperienceManager.setTotalExperience(player, playerCurrentExp + expToWithdraw);
            
            // Guardar en DB de forma asíncrona (no bloquea el main thread)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                anforaDataManager.saveAnfora(anforaData);
            });
            
            // Reproducir animación de partículas al retirar
            Location anforaLocation = getAnforaLocationFromId(anforaId);
            if (anforaLocation != null) {
                ParticleAnimations.playWithdrawAnimation(plugin, anforaLocation, player.getLocation());
            }
            
            player.sendMessage("§aHas retirado " + expToWithdraw + " puntos de experiencia.");
        } else {
            player.sendMessage("§cEl ánfora no tiene suficiente experiencia.");
        }
    }
}
