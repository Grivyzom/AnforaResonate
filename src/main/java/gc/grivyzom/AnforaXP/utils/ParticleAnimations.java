package gc.grivyzom.AnforaXP.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.Plugin;

public class ParticleAnimations {

    private static final Map<Location, BukkitTask> activeTasks = new ConcurrentHashMap<>();

    /**
     * Crea una animación de partículas girando alrededor de la parte inferior del ánfora
     * @param plugin Plugin principal para programar la tarea
     * @param location Ubicación del bloque del ánfora
     */
    public static void playAnforaPlaceAnimation(Plugin plugin, Location location) {
        if (location == null || plugin == null) return;
        
        World world = location.getWorld();
        if (world == null) return;

        // Cancelar cualquier animación previa en esta ubicación
        cancelAnimation(location);

        // Centro del bloque, ajustado a la parte inferior
        final Location center = location.clone().add(0.5, 0.2, 0.5);
        // Crear clave normalizada para el mapa
        final Location key = normalizeLocation(location);

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            final int duration = 60; // 3 segundos (60 ticks)
            final double radius = 0.5;

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    activeTasks.remove(key);
                    return;
                }

                // Verificar que el mundo sigue cargado
                if (world.getPlayers().isEmpty() && ticks > 20) {
                    this.cancel();
                    activeTasks.remove(key);
                    return;
                }

                // Calcular el ángulo para la rotación
                double angle = (ticks * Math.PI * 2) / 20.0; // Rotación completa cada segundo (20 ticks)

                // Generar múltiples partículas en círculo
                for (int i = 0; i < 8; i++) {
                    double currentAngle = angle + (i * Math.PI / 4); // 8 partículas distribuidas
                    double x = center.getX() + radius * Math.cos(currentAngle);
                    double z = center.getZ() + radius * Math.sin(currentAngle);

                    try {
                        world.spawnParticle(
                            Particle.SCULK_SOUL,
                            x, center.getY(), z,
                            1, // count
                            0, 0, 0, // offset
                            0 // extra (velocity)
                        );
                    } catch (Exception e) {
                        // Si SCULK_SOUL no está disponible, intentar con SOUL
                        try {
                            world.spawnParticle(
                                Particle.SOUL,
                                x, center.getY(), z,
                                1, 0, 0, 0, 0
                            );
                        } catch (Exception ex) {
                            // Usar END_ROD como alternativa
                            world.spawnParticle(
                                Particle.END_ROD,
                                x, center.getY(), z,
                                1, 0, 0, 0, 0
                            );
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Guardar la referencia del task
        activeTasks.put(key, task);
    }

    /**
     * Crea una animación de partículas desde el jugador hacia el ánfora (al depositar)
     * @param plugin Plugin principal para programar la tarea
     * @param playerLocation Ubicación del jugador
     * @param anforaLocation Ubicación del ánfora
     */
    public static void playDepositAnimation(Plugin plugin, Location playerLocation, Location anforaLocation) {
        if (playerLocation == null || anforaLocation == null || plugin == null) return;
        
        World world = playerLocation.getWorld();
        if (world == null) return;

        // Centro del jugador (a la altura del pecho)
        final Location start = playerLocation.clone().add(0, 1.2, 0);
        // Centro del ánfora (parte superior)
        final Location end = anforaLocation.clone().add(0.5, 0.5, 0.5);
        // Clave única para esta animación (usar ubicación del jugador)
        final Location key = normalizeLocation(playerLocation);

        // Cancelar animación previa del mismo jugador si existe
        cancelAnimation(key);

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            final int duration = 20; // 1 segundo

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    activeTasks.remove(key);
                    return;
                }

                // Verificar que el mundo sigue cargado
                if (world.getPlayers().isEmpty()) {
                    this.cancel();
                    activeTasks.remove(key);
                    return;
                }

                double progress = (double) ticks / duration;

                // Interpolación lineal entre el inicio y el final
                double x = start.getX() + (end.getX() - start.getX()) * progress;
                double y = start.getY() + (end.getY() - start.getY()) * progress;
                double z = start.getZ() + (end.getZ() - start.getZ()) * progress;

                // Generar partículas en el camino
                world.spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    x, y, z,
                    3, // count
                    0.1, 0.1, 0.1, // offset
                    0 // extra (velocity)
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Guardar la referencia del task
        activeTasks.put(key, task);
    }

    /**
     * Crea una animación de partículas desde el ánfora hacia el jugador (al retirar)
     * @param plugin Plugin principal para programar la tarea
     * @param anforaLocation Ubicación del ánfora
     * @param playerLocation Ubicación del jugador
     */
    public static void playWithdrawAnimation(Plugin plugin, Location anforaLocation, Location playerLocation) {
        if (playerLocation == null || anforaLocation == null || plugin == null) return;
        
        World world = playerLocation.getWorld();
        if (world == null) return;

        // Centro del ánfora (parte superior)
        final Location start = anforaLocation.clone().add(0.5, 0.5, 0.5);
        // Centro del jugador (a la altura del pecho)
        final Location end = playerLocation.clone().add(0, 1.2, 0);
        // Clave única para esta animación (usar ubicación del jugador)
        final Location key = normalizeLocation(playerLocation);

        // Cancelar animación previa del mismo jugador si existe
        cancelAnimation(key);

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            final int duration = 20; // 1 segundo

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    activeTasks.remove(key);
                    return;
                }

                // Verificar que el mundo sigue cargado
                if (world.getPlayers().isEmpty()) {
                    this.cancel();
                    activeTasks.remove(key);
                    return;
                }

                double progress = (double) ticks / duration;

                // Interpolación lineal entre el inicio y el final
                double x = start.getX() + (end.getX() - start.getX()) * progress;
                double y = start.getY() + (end.getY() - start.getY()) * progress;
                double z = start.getZ() + (end.getZ() - start.getZ()) * progress;

                // Generar partículas en el camino
                world.spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    x, y, z,
                    3, // count
                    0.1, 0.1, 0.1, // offset
                    0 // extra (velocity)
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Guardar la referencia del task
        activeTasks.put(key, task);
    }

    /**
     * Cancela la animación de partículas en una ubicación específica
     * @param location Ubicación donde cancelar la animación
     */
    public static void cancelAnimation(Location location) {
        if (location == null) return;
        
        Location key = normalizeLocation(location);
        BukkitTask task = activeTasks.remove(key);
        
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    /**
     * Cancela TODAS las animaciones activas
     * CRÍTICO: Llamar esto en onDisable() para prevenir fugas de memoria
     */
    public static void cancelAllAnimations() {
        for (BukkitTask task : activeTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();
    }

    /**
     * Obtiene la cantidad de animaciones activas
     * @return Número de animaciones en ejecución
     */
    public static int getActiveAnimationCount() {
        return activeTasks.size();
    }

    /**
     * Normaliza una ubicación para usarla como clave en el mapa
     * Redondea las coordenadas a enteros para evitar problemas de precisión
     * @param location Ubicación a normalizar
     * @return Ubicación normalizada
     */
    private static Location normalizeLocation(Location location) {
        if (location == null) return null;
        
        return new Location(
            location.getWorld(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }
}
