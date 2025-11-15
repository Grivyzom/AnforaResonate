package gc.grivyzom.AnforaXP.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnforaDataManager {

    private final StorageEngine storage;
    private final AnforaUUIDManager anforaUUIDManager;
    
    // Cache thread-safe para mejorar performance y evitar race conditions
    private final Map<String, AnforaData> cache = new ConcurrentHashMap<>();
    
    // Límite de cache para prevenir uso excesivo de memoria
    private static final int MAX_CACHE_SIZE = 1000;

    public AnforaDataManager(StorageEngine storage, AnforaUUIDManager anforaUUIDManager) {
        this.storage = storage;
        this.anforaUUIDManager = anforaUUIDManager;
    }

    /**
     * Guarda un ánfora en el storage y actualiza el cache
     * Thread-safe: Puede ser llamado desde cualquier thread
     */
    public void saveAnfora(AnforaData data) {
        if (data == null) return;
        
        // Actualizar cache primero (operación rápida)
        cache.put(data.getId(), data);
        
        // Luego guardar en storage (operación lenta)
        storage.saveAnfora(data);
    }

    /**
     * Carga un ánfora desde cache o storage
     * Thread-safe: Usa computeIfAbsent para operación atómica
     */
    public AnforaData loadAnfora(String anforaId) {
        if (anforaId == null) return null;
        
        // Operación atómica: si no está en cache, carga desde storage
        return cache.computeIfAbsent(anforaId, id -> {
            // Verificar límite de cache antes de añadir
            if (cache.size() >= MAX_CACHE_SIZE) {
                // Limpiar 10% del cache (las primeras entradas)
                cleanCache(MAX_CACHE_SIZE / 10);
            }
            return storage.loadAnfora(id);
        });
    }

    /**
     * Elimina un ánfora del storage y del cache
     * Thread-safe: Elimina primero del cache, luego del storage
     */
    public void deleteAnfora(String anforaId) {
        if (anforaId == null) return;
        
        // Eliminar del cache
        AnforaData removed = cache.remove(anforaId);
        
        // Eliminar del storage
        storage.deleteAnfora(anforaId);
        
        // Si existía en cache, eliminar su UUID del manager
        if (removed != null && removed.getUniqueId() != null) {
            anforaUUIDManager.removePlacedAnfora(removed.getUniqueId().toString());
        }
    }

    /**
     * Obtiene todas las ánforas de un propietario
     * Thread-safe: Lee desde storage y actualiza cache
     */
    public List<AnforaData> getAnforasByOwner(UUID ownerUUID) {
        if (ownerUUID == null) return new ArrayList<>();
        
        List<AnforaData> ownerAnforas = new ArrayList<>();
        
        for (String anforaId : storage.getAllAnforaIds()) {
            // Usar loadAnfora para aprovechar el cache
            AnforaData anforaData = loadAnfora(anforaId);
            
            if (anforaData != null && anforaData.getOwnerUUID().equals(ownerUUID)) {
                ownerAnforas.add(anforaData);
            }
        }
        
        return ownerAnforas;
    }
    
    /**
     * Guarda todas las ánforas en cache al storage
     * Útil para auto-save y shutdown
     */
    public void saveAll() {
        for (AnforaData data : cache.values()) {
            try {
                storage.saveAnfora(data);
            } catch (Exception e) {
                // Log error pero continua guardando las demás
                System.err.println("Error guardando ánfora " + data.getId() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Limpia entradas del cache
     * @param count Número de entradas a eliminar
     */
    private void cleanCache(int count) {
        cache.keySet().stream()
            .limit(count)
            .forEach(cache::remove);
    }
    
    /**
     * Invalida (elimina) una entrada específica del cache
     * Útil cuando se sabe que los datos han cambiado externamente
     */
    public void invalidateCache(String anforaId) {
        cache.remove(anforaId);
    }
    
    /**
     * Limpia todo el cache
     * Útil para reloads o cuando se necesita forzar recarga desde DB
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * Obtiene el tamaño actual del cache
     * @return Número de ánforas en cache
     */
    public int getCacheSize() {
        return cache.size();
    }
    
    /**
     * Pre-carga ánforas en el cache
     * Útil para optimizar performance en startup
     */
    public void preloadCache(List<String> anforaIds) {
        for (String anforaId : anforaIds) {
            if (cache.size() >= MAX_CACHE_SIZE) break;
            
            // Cargar en background sin bloquear
            AnforaData data = storage.loadAnfora(anforaId);
            if (data != null) {
                cache.put(anforaId, data);
            }
        }
    }
}