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

        // Verificar si es un clic derecho en un bloque
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || clickedBlock == null) {
            return;
        }

        // Verificar si el bloque es una ánfora
        if (clickedBlock.getType() == Material.DECORATED_POT) {
            String anforaId = String.format("%s_%d_%d_%d", clickedBlock.getWorld().getName(), clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
            AnforaData anforaData = anforaDataManager.loadAnfora(anforaId);

            if (anforaData != null) {
                // Verificar si el jugador es el propietario
                if (!player.getUniqueId().equals(anforaData.getOwnerUUID())) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("owner", anforaData.getOwnerName());
                    player.sendMessage(messageManager.getMessage("not_your_anfora_interact", placeholders));
                    event.setCancelled(true);
                    return;
                }

                // Registrar que este jugador está abriendo esta ánfora
                guiListener.addPlayer(player.getUniqueId(), anforaId);

                // Abrir la GUI del ánfora
                event.setCancelled(true);
                plugin.getGuiManager().openAnforaGui(player, anforaData);
            }
        }
    }
}