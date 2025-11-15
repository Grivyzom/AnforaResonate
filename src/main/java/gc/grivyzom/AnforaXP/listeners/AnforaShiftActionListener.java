package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import gc.grivyzom.AnforaXP.utils.LevelManager.LevelInfo;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class AnforaShiftActionListener implements Listener {

    private final AnforaMain plugin;
    private final AnforaDataManager anforaDataManager;
    private final MessageManager messageManager;

    public AnforaShiftActionListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.anforaDataManager = plugin.getAnforaDataManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onAnforaShiftInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Action action = event.getAction();

        // Check for shift-click on a block (either right or left)
        if ((action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) || clickedBlock == null || !player.isSneaking()) {
            return;
        }

        // Check if the block is an amphora
        if (clickedBlock.getType() == Material.DECORATED_POT) {
            String anforaId = String.format("%s_%d_%d_%d", clickedBlock.getWorld().getName(), clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
            AnforaData anforaData = anforaDataManager.loadAnfora(anforaId);

            if (anforaData != null) {
                // Check for ownership
                if (!player.getUniqueId().equals(anforaData.getOwnerUUID())) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("owner", anforaData.getOwnerName());
                    player.sendMessage(messageManager.getMessage("not_your_anfora_interact", placeholders));
                    event.setCancelled(true);
                    return;
                }

                // Cancel the event to handle the action manually
                event.setCancelled(true);

                if (action == Action.RIGHT_CLICK_BLOCK) {
                    // --- SHIFT-RIGHT-CLICK LOGIC: Withdraw all experience ---
                    double experienceToWithdraw = anforaData.getExperience();
                    if (experienceToWithdraw <= 0) {
                        player.sendMessage(messageManager.getMessage("anfora_empty"));
                        return;
                    }

                    // Transfer experience
                    player.giveExp((int) Math.round(experienceToWithdraw));
                    anforaData.setExperience(0);
                    anforaDataManager.saveAnfora(anforaData);

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("exp_amount", String.format("%.0f", experienceToWithdraw));
                    player.sendMessage(messageManager.getMessage("exp_withdrawn_all", placeholders));
                } else if (action == Action.LEFT_CLICK_BLOCK) {
                    // --- SHIFT-LEFT-CLICK LOGIC: Deposit all player experience ---
                    int playerExperience = player.getTotalExperience();
                    if (playerExperience <= 0) {
                        player.sendMessage(messageManager.getMessage("player_exp_empty"));
                        return;
                    }

                    LevelInfo anforaLevelInfo = LevelManager.getLevelInfo(anforaData.getLevel());
                    if (anforaLevelInfo == null) {
                        player.sendMessage(messageManager.getMessage("error_anfora_level_info"));
                        return;
                    }

                    double maxExperience = anforaLevelInfo.getMaxExperience();
                    double currentAnforaExperience = anforaData.getExperience();
                    double spaceAvailable = maxExperience - currentAnforaExperience;

                    if (spaceAvailable <= 0) {
                        player.sendMessage(messageManager.getMessage("anfora_full"));
                        return;
                    }

                    double experienceToDeposit = Math.min(playerExperience, spaceAvailable);

                    // Deposit experience
                    anforaData.addExperience(experienceToDeposit);
                    player.giveExp(-((int) Math.round(experienceToDeposit))); // Remove deposited experience from player
                    anforaDataManager.saveAnfora(anforaData);

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("exp_amount", String.format("%.0f", experienceToDeposit));
                    player.sendMessage(messageManager.getMessage("exp_deposited_all", placeholders));

                    if (experienceToDeposit < playerExperience) {
                        player.sendMessage(messageManager.getMessage("anfora_capacity_reached"));
                    }
                }
            }
        }
    }
}
