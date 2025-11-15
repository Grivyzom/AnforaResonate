package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.*;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class AnforaExplosionListener implements Listener {

    private final AnforaDataManager anforaDataManager;
    private final PlayerDataManager playerDataManager;
    private final AnforaUUIDManager anforaUUIDManager;
    private final AnforaMain plugin;

    public AnforaExplosionListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.anforaDataManager = plugin.getAnforaDataManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.anforaUUIDManager = plugin.getAnforaUUIDManager();
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Location loc = block.getLocation();
            String anforaId = String.format("%s_%d_%d_%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            AnforaData anforaData = anforaDataManager.loadAnfora(anforaId);
            if (anforaData != null) {
                // It's an amphora, handle it.

                // 1. Create the custom item with all its data.
                ItemStack anforaItem = ItemFactory.createAnforaItem(plugin, 1, anforaData.getUniqueId(), anforaData.getLevel(), anforaData.getExperience(), anforaData.getOwnerName());

                // 2. Drop the item at the location.
                loc.getWorld().dropItemNaturally(loc, anforaItem);

                // 3. Update the owner's player data.
                PlayerData playerData = playerDataManager.loadPlayer(anforaData.getOwnerUUID());
                if (playerData != null) {
                    playerData.setAnforaCount(Math.max(0, playerData.getAnforaCount() - 1));
                    playerDataManager.savePlayer(anforaData.getOwnerUUID(), playerData);
                }

                // 4. Delete the amphora from the database and UUID tracker.
                anforaDataManager.deleteAnfora(anforaId);
                anforaUUIDManager.removePlacedAnfora(anforaData.getUniqueId().toString());


                // 5. Prevent the block from being destroyed by the explosion and dropping vanilla items.
                // We've already dropped our custom item.
                iterator.remove();
            }
        }
    }
}