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
import java.util.Random;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;

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

        // Check for right-click on a block and not sneaking
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || clickedBlock == null || player.isSneaking()) {
            return;
        }

        // Check if the block is an amphora
        if (clickedBlock.getType() == Material.DECORATED_POT) {
            String anforaId = String.format("%s_%d_%d_%d", clickedBlock.getWorld().getName(), clickedBlock.getX(),
                    clickedBlock.getY(), clickedBlock.getZ());
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

                // Check if player is holding a glass bottle
                if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE) {
                    // --- BOTTLE CLICK LOGIC: Create Experience Bottle ---
                    Random random = new Random();
                    int expToWithdraw = random.nextInt(9) + 3; // 3 to 11

                    if (anforaData.getExperience() >= expToWithdraw) {
                        anforaData.setExperience(anforaData.getExperience() - expToWithdraw);
                        anforaDataManager.saveAnfora(anforaData);

                        // Log transaction
                        plugin.getTransactionManager().logWithdraw(
                                player.getUniqueId(),
                                anforaId,
                                expToWithdraw);

                        // Remove one glass bottle
                        player.getInventory().getItemInMainHand()
                                .setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

                        // Give one experience bottle
                        player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 1));

                        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
                        player.sendMessage(messageManager.getMessage("exp_bottle_created",
                                Map.of("exp_amount", String.valueOf(expToWithdraw))));
                    } else {
                        player.sendMessage(messageManager.getMessage("not_enough_exp_for_bottle"));
                    }
                } else {
                    // --- NORMAL CLICK LOGIC: Open GUI ---
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                    guiListener.addPlayer(player.getUniqueId(), anforaId);
                    plugin.getGuiManager().openAnforaGui(player, anforaData);
                }
            }
        }
    }
}