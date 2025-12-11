package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final AnforaMain plugin;
    private final StorageEngine storage;
    // Cache thread-safe para evitar race conditions
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    public PlayerDataManager(AnforaMain plugin, StorageEngine storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /**
     * Guarda los datos del jugador en storage y actualiza el cache
     * La operación de guardado en disco/DB es asíncrona.
     */
    public void savePlayer(UUID uuid, PlayerData data) {
        if (uuid == null || data == null)
            return;

        // Actualizar cache primero (síncrono y rápido)
        playerDataCache.put(uuid, data);

        // Guardar en storage de forma asíncrona
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> storage.savePlayerData(uuid, data));
    }

    /**
     * Carga los datos del jugador desde cache o storage
     * Thread-safe: Usa computeIfAbsent para operación atómica
     */
    public PlayerData loadPlayer(UUID uuid) {
        if (uuid == null)
            return null;

        // Operación atómica: si no está en cache, carga desde storage
        // NOTA: La carga sigue siendo síncrona si no está en caché.
        return playerDataCache.computeIfAbsent(uuid, id -> storage.loadPlayerData(id));
    }

    /**
     * Obtiene los datos del jugador solo si ya están cargados en cache
     * 
     * @return PlayerData o null si no está en cache
     */
    public PlayerData getLoadedPlayer(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    /**
     * Descargar datos del jugador del cache, guardando antes de forma asíncrona.
     * Debe ser llamado cuando el jugador se desconecta
     */
    public void unloadPlayer(UUID uuid) {
        if (uuid == null)
            return;

        // Guardar antes de descargar para asegurar persistencia
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> storage.savePlayerData(uuid, data));
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
                plugin.getLogger().severe("Error guardando jugador " + entry.getKey() + ": " + e.getMessage());
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