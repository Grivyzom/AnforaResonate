package gc.grivyzom.AnforaXP.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final StorageEngine storage;
    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();

    public PlayerDataManager(StorageEngine storage) {
        this.storage = storage;
    }

    public void savePlayer(UUID uuid, PlayerData data) {
        storage.savePlayerData(uuid, data);
    }

    public PlayerData loadPlayer(UUID uuid) {
        if (playerDataCache.containsKey(uuid)) {
            return playerDataCache.get(uuid);
        }
        PlayerData playerData = storage.loadPlayerData(uuid);
        playerDataCache.put(uuid, playerData);
        return playerData;
    }

    public PlayerData getLoadedPlayer(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public void unloadPlayer(UUID uuid) {
        playerDataCache.remove(uuid);
    }
}
