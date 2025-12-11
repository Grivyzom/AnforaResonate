package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.utils.EffectsManager;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import org.bukkit.Bukkit;
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

        if ((action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) || clickedBlock == null
                || !player.isSneaking()) {
            return;
        }

        if (clickedBlock.getType() == Material.DECORATED_POT) {
            String anforaId = String.format("%s_%d_%d_%d", clickedBlock.getWorld().getName(), clickedBlock.getX(),
                    clickedBlock.getY(), clickedBlock.getZ());
            AnforaData anforaData = anforaDataManager.loadAnfora(anforaId);

            if (anforaData != null) {
                if (!player.getUniqueId().equals(anforaData.getOwnerUUID())) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("owner", anforaData.getOwnerName());
                    player.sendMessage(messageManager.getMessage("not_your_anfora_interact", placeholders));
                    event.setCancelled(true);
                    return;
                }

                event.setCancelled(true);

                if (action == Action.RIGHT_CLICK_BLOCK) {
                    int experienceToWithdraw = anforaData.getExperience();
                    if (experienceToWithdraw <= 0) {
                        player.sendMessage(messageManager.getMessage("anfora_empty"));
                        return;
                    }

                    player.giveExp(experienceToWithdraw);
                    anforaData.setExperience(0);

                    // Log transaction
                    plugin.getTransactionManager().logWithdraw(
                            player.getUniqueId(),
                            anforaId,
                            experienceToWithdraw);

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        anforaDataManager.saveAnfora(anforaData);
                    });

                    EffectsManager.playWithdrawEffect(player, anforaData.getLocation());

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("exp_amount", String.valueOf(experienceToWithdraw));
                    player.sendMessage(messageManager.getMessage("exp_withdrawn_all", placeholders));
                } else if (action == Action.LEFT_CLICK_BLOCK) {
                    int playerExperience = player.getTotalExperience();
                    if (playerExperience <= 0) {
                        player.sendMessage(messageManager.getMessage("player_exp_empty"));
                        return;
                    }

                    int depositedAmount = LevelManager.addExperience(anforaData, playerExperience);

                    if (depositedAmount > 0) {
                        player.giveExp(-depositedAmount);

                        // Log transaction
                        plugin.getTransactionManager().logDeposit(
                                player.getUniqueId(),
                                anforaId,
                                depositedAmount);

                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            anforaDataManager.saveAnfora(anforaData);
                        });

                        EffectsManager.playDepositEffect(player, anforaData.getLocation());

                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("exp_amount", String.valueOf(depositedAmount));
                        player.sendMessage(messageManager.getMessage("exp_deposited_all", placeholders));

                        if (depositedAmount < playerExperience) {
                            player.sendMessage(messageManager.getMessage("anfora_capacity_reached"));
                        }
                    } else {
                        player.sendMessage(messageManager.getMessage("anfora_full"));
                    }
                }
            }
        }
    }
}
