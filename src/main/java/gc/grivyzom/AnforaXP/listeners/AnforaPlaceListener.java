package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.*;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import gc.grivyzom.AnforaXP.utils.ParticleAnimations;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnforaPlaceListener implements Listener {

    private final AnforaMain plugin;
    private final PlayerDataManager playerDataManager;
    private final AnforaDataManager anforaDataManager;
    private final AnforaUUIDManager anforaUUIDManager;
    private final MessageManager messageManager;

    public AnforaPlaceListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.anforaDataManager = plugin.getAnforaDataManager();
        this.anforaUUIDManager = plugin.getAnforaUUIDManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onAnforaPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();

        if (block.getType() == Material.DECORATED_POT && ItemFactory.isAnforaResonante(itemInHand)) {
            UUID anforaUniqueId = ItemFactory.getAnforaUniqueId(itemInHand);

            if (anforaUniqueId == null) {
                player.sendMessage(messageManager.getMessage("invalid_item"));
                event.setCancelled(true);
                return;
            }

            // Anti-Dupe Check
            if (anforaUUIDManager.isAnforaPlaced(anforaUniqueId.toString())) {
                player.sendMessage(messageManager.getMessage("anfora_dupe_error"));
                event.setCancelled(true);
                return;
            }

            PlayerData playerData = playerDataManager.loadPlayer(player.getUniqueId());

            if (!playerData.isActive()) {
                playerData.setActive(true);
                player.sendMessage(messageManager.getMessage("account_activated"));
            }

            playerData.setAnforaCount(playerData.getAnforaCount() + 1);

            Location loc = block.getLocation();
            String anforaId = String.format("%s_%d_%d_%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            // Leer nivel y experiencia del ítem
            int level = ItemFactory.getAnforaLevel(itemInHand);
            double experience = ItemFactory.getAnforaExperience(itemInHand);

            AnforaData anforaData = new AnforaData(anforaId, anforaUniqueId, player.getUniqueId(), player.getName()); // Constructor actualizado
            anforaData.setLocation(loc);
            anforaData.setLevel(level);
            anforaData.setExperience(experience);

            playerDataManager.savePlayer(player.getUniqueId(), playerData);
            anforaDataManager.saveAnfora(anforaData);
            anforaUUIDManager.addPlacedAnfora(anforaUniqueId.toString());

            // Reproducir animación de partículas al colocar el ánfora
            ParticleAnimations.playAnforaPlaceAnimation(plugin, loc);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("anfora_count", String.valueOf(playerData.getAnforaCount()));
            player.sendMessage(messageManager.getMessage("anfora_placed", placeholders));
        }
    }
}