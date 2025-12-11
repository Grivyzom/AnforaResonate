package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.*;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AnforaBreakListener implements Listener {

    private final AnforaMain plugin;
    private final PlayerDataManager playerDataManager;
    private final AnforaDataManager anforaDataManager;
    private final AnforaUUIDManager anforaUUIDManager;
    private final MessageManager messageManager;

    public AnforaBreakListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.anforaDataManager = plugin.getAnforaDataManager();
        this.anforaUUIDManager = plugin.getAnforaUUIDManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onAnforaBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        String anforaId = String.format("%s_%d_%d_%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        AnforaData anforaData = anforaDataManager.loadAnfora(anforaId);
        if (anforaData != null) {
            Player breaker = event.getPlayer();
            event.setDropItems(false);

            if (breaker.getUniqueId().equals(anforaData.getOwnerUUID())) {
                // --- EL JUGADOR ES EL PROPIETARIO ---

                // 1. Crear el ítem personalizado, pasando el UUID, nivel, experiencia y nombre del propietario.
                ItemStack anforaItem = ItemFactory.createAnforaItem(plugin, 1, anforaData.getUniqueId(), anforaData.getLevel(), anforaData.getExperience(), anforaData.getOwnerName());

                // 2. Dropear el ítem personalizado.
                loc.getWorld().dropItemNaturally(loc, anforaItem);

                // 3. Actualizar PlayerData: Sincronizar con el conteo real
                // Eliminamos el ánfora primero para que el conteo sea correcto
                anforaDataManager.deleteAnfora(anforaId);
                anforaUUIDManager.removePlacedAnfora(anforaData.getUniqueId().toString());

                PlayerData playerData = playerDataManager.loadPlayer(breaker.getUniqueId());
                int realCount = anforaDataManager.getAnforasByOwner(breaker.getUniqueId()).size();
                playerData.setAnforaCount(realCount);
                playerDataManager.savePlayer(breaker.getUniqueId(), playerData);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("anfora_count", String.valueOf(realCount));
                breaker.sendMessage(messageManager.getMessage("anfora_picked_up", placeholders));

                // 5. Reproducir sonido de desactivación del faro.
                breaker.playSound(breaker.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.2f);

            } else {
                // --- INTENTO DE OTRO JUGADOR ---
                event.setCancelled(true);
                breaker.sendMessage(messageManager.getMessage("not_your_anfora_break"));
            }
        }
    }
}
