package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.data.PlayerData;
import gc.grivyzom.AnforaXP.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    private final AnforaMain plugin;
    private final PlayerDataManager playerDataManager;
    private final AnforaDataManager anforaDataManager;

    public PlayerConnectionListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.anforaDataManager = plugin.getAnforaDataManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        PlayerData playerData = playerDataManager.loadPlayer(playerUUID);

        // Sincronizar contador de ánforas al entrar
        // Esto corrige inconsistencias entre PlayerData y las ánforas reales en la base de datos
        if (playerData != null) {
            List<AnforaData> realAnforas = anforaDataManager.getAnforasByOwner(playerUUID);
            int realCount = realAnforas.size();
            
            if (playerData.getAnforaCount() != realCount) {
                plugin.getLogger().info("Corrigiendo contador de ánforas para " + player.getName() + ": " + playerData.getAnforaCount() + " -> " + realCount);
                playerData.setAnforaCount(realCount);
                playerDataManager.savePlayer(playerUUID, playerData);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        PlayerData playerData = playerDataManager.getLoadedPlayer(playerUUID);
        if (playerData != null) {
            playerDataManager.savePlayer(playerUUID, playerData);
            playerDataManager.unloadPlayer(playerUUID);
        }
    }
}
