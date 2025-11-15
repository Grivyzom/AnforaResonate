package gc.grivyzom.AnforaXP.tasks;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaDataManager;
import gc.grivyzom.AnforaXP.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Tarea asíncrona que guarda automáticamente todos los datos en caché
 * Previene pérdida de datos en caso de crash del servidor
 */
public class AutoSaveTask extends BukkitRunnable {
    
    private final AnforaMain plugin;
    private final PlayerDataManager playerDataManager;
    private final AnforaDataManager anforaDataManager;
    private BukkitTask task;
    
    // Intervalo de guardado en ticks (20 ticks = 1 segundo)
    private static final long SAVE_INTERVAL_TICKS = 6000L; // 5 minutos (300 segundos)
    
    public AutoSaveTask(AnforaMain plugin, PlayerDataManager playerDataManager, AnforaDataManager anforaDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.anforaDataManager = anforaDataManager;
    }
    
    /**
     * Inicia la tarea de auto-guardado
     */
    public void start() {
        if (task != null) {
            plugin.getLogger().warning("Auto-save ya está activo");
            return;
        }
        
        // Ejecutar asíncronamente para no bloquear el main thread
        task = this.runTaskTimerAsynchronously(plugin, SAVE_INTERVAL_TICKS, SAVE_INTERVAL_TICKS);
        plugin.getLogger().info("Auto-save iniciado (intervalo: 5 minutos)");
    }
    
    /**
     * Detiene la tarea de auto-guardado
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            plugin.getLogger().info("Auto-save detenido");
        }
    }
    
    @Override
    public void run() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Guardar datos de jugadores
            final int playersSaved;
            if (playerDataManager != null) {
                playersSaved = playerDataManager.getCacheSize();
                playerDataManager.saveAll();
            } else {
                playersSaved = 0;
            }
            
            // Guardar datos de ánforas
            final int anforasSaved;
            if (anforaDataManager != null) {
                anforasSaved = anforaDataManager.getCacheSize();
                anforaDataManager.saveAll();
            } else {
                anforasSaved = 0;
            }
            
            final long elapsed = System.currentTimeMillis() - startTime;
            
            // Log en el main thread para evitar problemas de concurrencia
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info(String.format(
                    "Auto-save completado: %d jugadores, %d ánforas guardadas en %dms",
                    playersSaved, anforasSaved, elapsed
                ));
            });
            
        } catch (Exception e) {
            // Log de errores en el main thread
            final String errorMessage = e.getMessage();
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().severe("Error durante auto-save: " + errorMessage);
                e.printStackTrace();
            });
        }
    }
    
    /**
     * Verifica si el auto-save está activo
     */
    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }
    
    /**
     * Obtiene el intervalo de guardado en segundos
     */
    public static int getIntervalSeconds() {
        return (int) (SAVE_INTERVAL_TICKS / 20);
    }
}
