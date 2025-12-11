package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnforaDataManager {

    private final AnforaMain plugin;
    private final StorageEngine storage;
    private final AnforaUUIDManager anforaUUIDManager;
    private final Map<String, AnforaData> cache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;

    public AnforaDataManager(AnforaMain plugin, StorageEngine storage, AnforaUUIDManager anforaUUIDManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.anforaUUIDManager = anforaUUIDManager;
    }

    public void saveAnfora(AnforaData data) {
        if (data == null)
            return;
        cache.put(data.getId(), data);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> storage.saveAnfora(data));
    }

    public AnforaData loadAnfora(String anforaId) {
        if (anforaId == null)
            return null;
        return cache.computeIfAbsent(anforaId, id -> {
            if (cache.size() >= MAX_CACHE_SIZE) {
                cleanCache(MAX_CACHE_SIZE / 10);
            }
            // Note: This is still synchronous and can be a source of lag if the cache
            // misses often.
            // A full async implementation would require a significant refactor using
            // CompletableFuture or callbacks.
            return storage.loadAnfora(id);
        });
    }

    public void deleteAnfora(String anforaId) {
        if (anforaId == null)
            return;
        AnforaData removed = cache.remove(anforaId);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            storage.deleteAnfora(anforaId);
            if (removed != null && removed.getUniqueId() != null) {
                anforaUUIDManager.removePlacedAnfora(removed.getUniqueId().toString());
            }
        });
    }

    public List<AnforaData> getAnforasByOwner(UUID ownerUUID) {
        if (ownerUUID == null)
            return new ArrayList<>();

        // Note: This is still synchronous.
        List<AnforaData> anforasFromStorage = storage.getAnforasByOwner(ownerUUID);

        for (AnforaData anfora : anforasFromStorage) {
            cache.put(anfora.getId(), anfora);
        }

        return anforasFromStorage;
    }

    public List<AnforaData> getAllAnforas() {
        // Note: This is still synchronous.
        List<AnforaData> allAnforas = storage.loadAllAnforas();
        for (AnforaData anfora : allAnforas) {
            cache.put(anfora.getId(), anfora);
        }
        return allAnforas;
    }

    public void saveAll() {
        for (AnforaData data : cache.values()) {
            try {
                storage.saveAnfora(data);
            } catch (Exception e) {
                plugin.getLogger().severe("Error guardando ánfora " + data.getId() + ": " + e.getMessage());
            }
        }
    }

    private void cleanCache(int count) {
        cache.keySet().stream()
                .limit(count)
                .forEach(cache::remove);
    }

    public void invalidateCache(String anforaId) {
        cache.remove(anforaId);
    }

    public void clearCache() {
        cache.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }

    public AnforaData getAnforaByUUID(UUID anforaUUID) {
        if (anforaUUID == null)
            return null;

        for (AnforaData data : cache.values()) {
            if (data.getUniqueId().equals(anforaUUID)) {
                return data;
            }
        }

        // Note: This is synchronous
        Map<String, String> uuidToIdMap = storage.getUniqueIdToAnforaIdMap();
        String anforaId = uuidToIdMap.get(anforaUUID.toString());

        if (anforaId != null) {
            return loadAnfora(anforaId);
        }

        return null;
    }

    public String getAnforaIdByLocation(org.bukkit.Location location) {
        if (location == null)
            return null;

        for (AnforaData data : cache.values()) {
            if (data.getLocation() != null && data.getLocation().equals(location)) {
                return data.getId();
            }
        }

        // This is inefficient, but fixing it is a separate task.
        // It's also synchronous.
        // TODO: This is inefficient, add a method to StorageEngine to get anfora by
        // location
        for (String anforaId : storage.getAllAnforaIds()) {
            AnforaData anforaData = loadAnfora(anforaId);
            if (anforaData != null && anforaData.getLocation() != null && anforaData.getLocation().equals(location)) {
                return anforaData.getId();
            }
        }

        return null;
    }

    public void preloadCache(List<String> anforaIds) {
        // Note: This is synchronous
        for (String anforaId : anforaIds) {
            if (cache.size() >= MAX_CACHE_SIZE)
                break;
            AnforaData data = storage.loadAnfora(anforaId);
            if (data != null) {
                cache.put(anforaId, data);
            }
        }
    }
}
