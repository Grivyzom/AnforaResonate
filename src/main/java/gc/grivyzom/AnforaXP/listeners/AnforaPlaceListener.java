package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.AnforaType; // Import AnforaType
import gc.grivyzom.AnforaXP.data.*;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import gc.grivyzom.AnforaXP.utils.ParticleAnimations;
import gc.grivyzom.AnforaXP.utils.RewardManager; // Import RewardManager
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority; // Import EventPriority
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections; // Import Collections
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // Added EventPriority and ignoreCancelled
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
                boolean isGhost = false;
                AnforaData existingAnfora = anforaDataManager.getAnforaByUUID(anforaUniqueId);

                if (existingAnfora != null) {
                    Location storedLoc = existingAnfora.getLocation();
                    if (storedLoc != null && storedLoc.getWorld() != null) {
                        if (storedLoc.getBlock().getType() != Material.DECORATED_POT) {
                            isGhost = true;
                        }
                    } else {
                        isGhost = true;
                    }
                } else {
                    isGhost = true;
                }

                if (isGhost) {
                    if (existingAnfora != null) {
                        anforaDataManager.deleteAnfora(existingAnfora.getId());
                    }
                    anforaUUIDManager.removePlacedAnfora(anforaUniqueId.toString());
                    plugin.getLogger().warning("Detectada y reparada ánfora fantasma para UUID: " + anforaUniqueId);
                } else {
                    player.sendMessage(messageManager.getMessage("anfora_dupe_error"));
                    event.setCancelled(true);
                    return;
                }
            }

            PlayerData playerData = playerDataManager.loadPlayer(player.getUniqueId());

            // Validación de límite de ánforas
            int maxAnforas = getMaxAnforasForPlayer(player);
            if (playerData.getAnforaCount() >= maxAnforas) {
                Map<String, String> limitPlaceholders = new HashMap<>();
                limitPlaceholders.put("max_anforas", String.valueOf(maxAnforas));
                player.sendMessage(messageManager.getMessage("anfora_limit_reached", limitPlaceholders));
                event.setCancelled(true);
                return;
            }

            if (!playerData.isActive()) {
                playerData.setActive(true);
                player.sendMessage(messageManager.getMessage("account_activated"));
            }

            // Check if player has an AnforaType and assign one if not
            if (playerData.getAnforaType() == null) {
                AnforaType randomAnforaType = RewardManager.getRandomAnforaType();
                playerData.setAnforaType(randomAnforaType);
                player.sendMessage(messageManager.getMessage("anfora_type_assigned",
                        Collections.singletonMap("anfora_type", randomAnforaType.name())));
            }

            playerData.setAnforaCount(playerData.getAnforaCount() + 1);

            Location loc = block.getLocation();
            String anforaId = String.format("%s_%d_%d_%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(),
                    loc.getBlockZ());

            // Leer nivel y experiencia del ítem
            int level = ItemFactory.getAnforaLevel(itemInHand);
            int experience = ItemFactory.getAnforaExperience(itemInHand);

            AnforaData anforaData = new AnforaData(anforaId, anforaUniqueId, player.getUniqueId(), player.getName()); // Constructor
                                                                                                                      // actualizado
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

    /**
     * Determina el número máximo de ánforas que un jugador puede tener
     * basándose en sus permisos (anforaresonante.max.N)
     * 
     * @param player El jugador a verificar
     * @return El número máximo de ánforas permitidas
     */
    private int getMaxAnforasForPlayer(Player player) {
        // Verificar permiso ilimitado
        if (player.hasPermission("anforaresonante.max.unlimited")) {
            return Integer.MAX_VALUE;
        }

        // Buscar el permiso más alto anforaresonante.max.N
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("anforaresonante.max." + i)) {
                return i;
            }
        }

        // Por defecto 1 ánfora
        return 1;
    }
}