package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.utils.EffectsManager;
import gc.grivyzom.AnforaXP.utils.ExperienceManager;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

            case 4:
                if (LevelManager.levelUp(anforaData)) {
                    player.sendMessage("§a¡Ánfora mejorada al nivel " + anforaData.getLevel() + "!");
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        anforaDataManager.saveAnfora(anforaData);
                    });
                    isAction = true;
                }
                break;

            case 13:
            case 22:
                break;
        }

        if (isAction) {
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }

        updateGui(player, anforaData);
    }

    private void updateGui(Player player, AnforaData anforaData) {
        org.bukkit.inventory.InventoryView openInventoryView = player.getOpenInventory();
        if (openInventoryView == null || !openInventoryView.getTitle().equals("Anfora Resonante")) {
            return;
        }
        Inventory openInventory = openInventoryView.getTopInventory();

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
        ItemStack progressItem = plugin.getGuiManager().createGuiItem(org.bukkit.Material.EXPERIENCE_BOTTLE, "§eProgreso de Experiencia",
                progressLore,
                "§7" + buildProgressBar(progressPercentage),
                "",
                "§7Tu Experiencia: §a" + playerTotalXp
        );
        openInventory.setItem(13, progressItem);

        // Slot 4: Nether Star (Upgrade Button or Info)
        ItemStack levelItem;
        if (LevelManager.canLevelUp(anforaData)) {
            levelItem = plugin.getGuiManager().createGuiItem(org.bukkit.Material.NETHER_STAR, "§a¡Mejorar Ánfora!",
                    "§7Haz click para mejorar el ánfora",
                    "§7al nivel " + (currentLevel + 1)
            );
        } else {
            levelItem = plugin.getGuiManager().createGuiItem(org.bukkit.Material.NETHER_STAR, "§bNivel del Ánfora", "§eNivel " + currentLevel);
        }
        openInventory.setItem(4, levelItem);

        openInventory.setItem(22, plugin.getGuiManager().createGuiItem(org.bukkit.Material.BOOK, "§dInformación General",
                "§7Dueño: §e" + anforaData.getOwnerName(),
                "§7Nivel: §e" + currentLevel,
                "§7XP Total: §a" + currentXp));
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

        int playerCurrentExp = ExperienceManager.getTotalExperience(player);
        int playerTargetExp = ExperienceManager.getTotalExperience(player.getLevel() - levels, 0);
        int expToDeposit = playerCurrentExp - playerTargetExp;

        if (expToDeposit <= 0) return;

        int actualAmountDeposited = LevelManager.addExperience(anforaData, expToDeposit);

        if (actualAmountDeposited > 0) {
            ExperienceManager.setTotalExperience(player, playerCurrentExp - actualAmountDeposited);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                anforaDataManager.saveAnfora(anforaData);
            });

            Location anforaLocation = getAnforaLocationFromId(anforaId);
            if (anforaLocation != null) {
                EffectsManager.playDepositEffect(player, anforaLocation);
            }

            player.sendMessage("§aHas depositado " + actualAmountDeposited + " puntos de experiencia.");
            if (actualAmountDeposited < expToDeposit) {
                player.sendMessage("§cEl ánfora ha alcanzado su capacidad máxima.");
            }
        } else {
            player.sendMessage("§cEl ánfora está llena.");
        }
    }

    private void withdrawExperience(Player player, AnforaData anforaData, String anforaId, int levels) {
        int playerCurrentExp = ExperienceManager.getTotalExperience(player);
        int playerTargetExp = ExperienceManager.getTotalExperience(player.getLevel() + levels, 0);
        int expToWithdraw = playerTargetExp - playerCurrentExp;

        if (anforaData.getExperience() >= expToWithdraw) {
            anforaData.setExperience(anforaData.getExperience() - expToWithdraw);
            ExperienceManager.setTotalExperience(player, playerCurrentExp + expToWithdraw);
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                anforaDataManager.saveAnfora(anforaData);
            });
            
            Location anforaLocation = getAnforaLocationFromId(anforaId);
            if (anforaLocation != null) {
                EffectsManager.playWithdrawEffect(player, anforaLocation);
            }
            
            player.sendMessage("§aHas retirado " + expToWithdraw + " puntos de experiencia.");
        } else {
            player.sendMessage("§cEl ánfora no tiene suficiente experiencia.");
        }
    }
}
