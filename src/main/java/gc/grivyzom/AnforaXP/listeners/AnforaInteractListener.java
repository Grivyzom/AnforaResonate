package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class AnforaInteractListener implements Listener {

    private final AnforaMain plugin;
    private final AnforaDataManager anforaDataManager;
    private final MessageManager messageManager;
    private final GuiListener guiListener;

    public AnforaInteractListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.anforaDataManager = plugin.getAnforaDataManager();
        this.messageManager = plugin.getMessageManager();
        this.guiListener = plugin.getGuiListener();
    }

    @EventHandler
    public void onAnforaInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // Check for right-click on a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || clickedBlock == null) {
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

                if (player.isSneaking()) {
                    // --- SHIFT-CLICK LOGIC: Withdraw all experience ---
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
                } else {
                    // --- NORMAL CLICK LOGIC: Open GUI ---
                    guiListener.addPlayer(player.getUniqueId(), anforaId);
                    plugin.getGuiManager().openAnforaGui(player, anforaData);
                }
            }
        }
    }
}