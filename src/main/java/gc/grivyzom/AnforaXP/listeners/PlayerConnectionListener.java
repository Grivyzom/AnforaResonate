package gc.grivyzom.AnforaXP.listeners;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.PlayerData;
import gc.grivyzom.AnforaXP.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    private final AnforaMain plugin;
    private final PlayerDataManager playerDataManager;

    public PlayerConnectionListener(AnforaMain plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        playerDataManager.loadPlayer(playerUUID);
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
