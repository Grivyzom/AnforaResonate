package gc.grivyzom.AnforaXP.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final StorageEngine storage;
    // Cache thread-safe para evitar race conditions
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    public PlayerDataManager(StorageEngine storage) {
        this.storage = storage;
    }

    /**
     * Guarda los datos del jugador en storage y actualiza el cache
     * Thread-safe: Puede ser llamado desde cualquier thread
     */
    public void savePlayer(UUID uuid, PlayerData data) {
        if (uuid == null || data == null) return;
        
        // Actualizar cache primero
        playerDataCache.put(uuid, data);
        
        // Guardar en storage
        storage.savePlayerData(uuid, data);
    }

    /**
     * Carga los datos del jugador desde cache o storage
     * Thread-safe: Usa computeIfAbsent para operación atómica
     */
    public PlayerData loadPlayer(UUID uuid) {
        if (uuid == null) return null;
        
        // Operación atómica: si no está en cache, carga desde storage
        return playerDataCache.computeIfAbsent(uuid, id -> storage.loadPlayerData(id));
    }

    /**
     * Obtiene los datos del jugador solo si ya están cargados en cache
     * @return PlayerData o null si no está en cache
     */
    public PlayerData getLoadedPlayer(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    /**
     * Descargar datos del jugador del cache
     * Debe ser llamado cuando el jugador se desconecta
     */
    public void unloadPlayer(UUID uuid) {
        if (uuid == null) return;
        
        // Guardar antes de descargar para asegurar persistencia
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            storage.savePlayerData(uuid, data);
        }
        
        playerDataCache.remove(uuid);
    }
    
    /**
     * Guarda todos los jugadores en cache al storage
     * Útil para auto-save y shutdown
     */
    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            try {
                storage.savePlayerData(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                System.err.println("Error guardando jugador " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Limpia todo el cache de jugadores
     * Útil para reloads
     */
    public void clearCache() {
        playerDataCache.clear();
    }
    
    /**
     * Obtiene el número de jugadores en cache
     */
    public int getCacheSize() {
        return playerDataCache.size();
    }
}