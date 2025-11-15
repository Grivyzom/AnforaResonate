package gc.grivyzom.AnforaXP.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Implementación profesional de MySQL con HikariCP Connection Pool
 * Optimizado para redes grandes con alto tráfico y concurrencia
 * 
 * Características principales:
 * - HikariCP connection pooling para máximo rendimiento
 * - PreparedStatements para prevenir SQL injection
 * - Try-with-resources para auto-cerrar conexiones
 * - Índices optimizados en todas las tablas
 * - ENGINE InnoDB con utf8mb4 para soporte completo Unicode
 * - REPLACE INTO para operaciones atómicas de inserción/actualización
 */
public class MySqlStorage implements StorageEngine {

    private final AnforaMain plugin;
    private HikariDataSource dataSource;
    private final String host;
    private final int port;
    private final String database;

    /**
     * Constructor que inicializa el connection pool de HikariCP
     * 
     * @param plugin Instancia del plugin principal
     * @throws SQLException Si no se puede conectar a la base de datos
     */
    public MySqlStorage(AnforaMain plugin) throws SQLException {
        this.plugin = plugin;
        
        FileConfiguration dbConfig = plugin.getDatabaseManager().getDatabaseConfig();
        this.host = dbConfig.getString("mysql.host", "localhost");
        this.port = dbConfig.getInt("mysql.port", 3306);
        this.database = dbConfig.getString("mysql.database", "anforadb");
        
        initializeConnectionPool(dbConfig);
        createTables();
    }

    /**
     * Inicializa el connection pool de HikariCP con configuración optimizada
     * para alto rendimiento y estabilidad en producción
     */
    private void initializeConnectionPool(FileConfiguration dbConfig) throws SQLException {
        try {
            HikariConfig config = new HikariConfig();
            
            // === CONFIGURACIÓN DE CONEXIÓN ===
            String user = dbConfig.getString("mysql.user", "root");
            String password = dbConfig.getString("mysql.password", "");
            boolean useSSL = dbConfig.getBoolean("mysql.use-ssl", false);
            
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(user);
            config.setPassword(password);
            
            // === CONFIGURACIÓN DEL POOL ===
            // Optimizado para servidores de Minecraft con múltiples threads
            config.setMaximumPoolSize(dbConfig.getInt("mysql.pool.maximum-pool-size", 10));
            config.setMinimumIdle(dbConfig.getInt("mysql.pool.minimum-idle", 2));
            config.setConnectionTimeout(dbConfig.getLong("mysql.pool.connection-timeout", 10000)); // 10s
            config.setIdleTimeout(dbConfig.getLong("mysql.pool.idle-timeout", 600000)); // 10 minutos
            config.setMaxLifetime(dbConfig.getLong("mysql.pool.max-lifetime", 1800000)); // 30 minutos
            config.setLeakDetectionThreshold(dbConfig.getLong("mysql.pool.leak-detection-threshold", 60000)); // 1 minuto
            
            // Pool name para identificación en logs
            config.setPoolName("AnforaXP-MySQL-Pool");
            
            // === OPTIMIZACIONES DE PERFORMANCE ===
            // Estas configuraciones mejoran drásticamente el rendimiento de MySQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // === CONFIGURACIÓN DE SSL/TLS ===
            config.addDataSourceProperty("useSSL", String.valueOf(useSSL));
            if (!useSSL) {
                config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
            }
            
            // === CONFIGURACIÓN DE TIMEZONE ===
            config.addDataSourceProperty("serverTimezone", "UTC");
            
            // === CONFIGURACIÓN DE CHARACTER ENCODING ===
            // utf8mb4 para soporte completo de emojis y caracteres especiales
            config.addDataSourceProperty("characterEncoding", "utf8mb4");
            config.addDataSourceProperty("useUnicode", "true");
            
            // === CONNECTION TEST QUERY ===
            config.setConnectionTestQuery("SELECT 1");
            
            // Crear el DataSource con la configuración
            this.dataSource = new HikariDataSource(config);
            
            // Logs de información
            plugin.getLogger().info("========================================");
            plugin.getLogger().info("✓ HikariCP Connection Pool inicializado");
            plugin.getLogger().info("  - Host: " + host + ":" + port);
            plugin.getLogger().info("  - Database: " + database);
            plugin.getLogger().info("  - Pool Size: " + config.getMaximumPoolSize());
            plugin.getLogger().info("  - Min Idle: " + config.getMinimumIdle());
            plugin.getLogger().info("========================================");
            
        } catch (Exception e) {
            throw new SQLException("Error al inicializar HikariCP: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene una conexión del pool de HikariCP
     * Este método es thread-safe y optimizado para alto rendimiento
     * 
     * @return Conexión lista para usar
     * @throws SQLException Si el pool no está disponible
     */
    private Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Connection pool no disponible");
        }
        return dataSource.getConnection();
    }

    /**
     * Crea las tablas de MySQL con configuración optimizada
     * - ENGINE=InnoDB para transacciones y consistencia
     * - CHARSET=utf8mb4 para soporte completo Unicode
     * - Índices en columnas frecuentemente consultadas
     */
    private void createTables() throws SQLException {
        // Tabla de jugadores con índice en isActive
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "anforaCount INT NOT NULL DEFAULT 0," +
                "isActive BOOLEAN NOT NULL DEFAULT FALSE," +
                "INDEX idx_active (isActive)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        // Tabla de ánforas con índices múltiples para optimizar búsquedas
        String createAnforasTable = "CREATE TABLE IF NOT EXISTS anforas (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "uniqueId VARCHAR(36) NOT NULL," +
                "ownerUUID VARCHAR(36) NOT NULL," +
                "ownerName VARCHAR(16) NOT NULL," +
                "world VARCHAR(255) NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "level INT NOT NULL DEFAULT 1," +
                "experience DOUBLE NOT NULL DEFAULT 0," +
                "INDEX idx_uniqueId (uniqueId)," +
                "INDEX idx_ownerUUID (ownerUUID)," +
                "INDEX idx_location (world, x, y, z)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createPlayersTable);
            stmt.execute(createAnforasTable);
            
            plugin.getLogger().info("✓ Tablas de MySQL creadas o verificadas");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al crear tablas MySQL", e);
            throw e;
        }
    }

    /**
     * Guarda o actualiza los datos de un jugador usando REPLACE INTO
     * REPLACE INTO es más eficiente que INSERT ... ON DUPLICATE KEY UPDATE
     * 
     * @param uuid UUID del jugador
     * @param data Datos del jugador a guardar
     */
    @Override
    public void savePlayerData(UUID uuid, PlayerData data) {
        String sql = "REPLACE INTO players (uuid, anforaCount, isActive) VALUES (?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, uuid.toString());
            pstmt.setInt(2, data.getAnforaCount());
            pstmt.setBoolean(3, data.isActive());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar datos del jugador " + uuid + " en MySQL", e);
        }
    }

    /**
     * Carga los datos de un jugador desde MySQL
     * 
     * @param uuid UUID del jugador
     * @return PlayerData con los datos cargados, o valores por defecto si no existe
     */
    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        PlayerData playerData = new PlayerData(uuid);
        String sql = "SELECT anforaCount, isActive FROM players WHERE uuid = ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, uuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    playerData.setAnforaCount(rs.getInt("anforaCount"));
                    playerData.setActive(rs.getBoolean("isActive"));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar datos del jugador " + uuid + " de MySQL", e);
        }
        
        return playerData;
    }

    /**
     * Guarda o actualiza un ánfora usando REPLACE INTO
     * Esta operación es atómica y previene race conditions
     * 
     * @param data Datos del ánfora a guardar
     */
    @Override
    public void saveAnfora(AnforaData data) {
        String sql = "REPLACE INTO anforas (id, uniqueId, ownerUUID, ownerName, world, x, y, z, level, experience) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, data.getId());
            pstmt.setString(2, data.getUniqueId().toString());
            pstmt.setString(3, data.getOwnerUUID().toString());
            pstmt.setString(4, data.getOwnerName());
            pstmt.setString(5, data.getLocation().getWorld().getName());
            pstmt.setInt(6, data.getLocation().getBlockX());
            pstmt.setInt(7, data.getLocation().getBlockY());
            pstmt.setInt(8, data.getLocation().getBlockZ());
            pstmt.setInt(9, data.getLevel());
            pstmt.setDouble(10, data.getExperience());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar ánfora " + data.getId() + " en MySQL", e);
        }
    }

    /**
     * Carga un ánfora por su ID desde MySQL
     * 
     * @param anforaId ID único del ánfora
     * @return AnforaData con los datos cargados, o null si no existe
     */
    @Override
    public AnforaData loadAnfora(String anforaId) {
        String sql = "SELECT * FROM anforas WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, anforaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    UUID uniqueId = UUID.fromString(rs.getString("uniqueId"));
                    UUID ownerUUID = UUID.fromString(rs.getString("ownerUUID"));
                    String ownerName = rs.getString("ownerName");
                    String worldName = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    int level = rs.getInt("level");
                    double experience = rs.getDouble("experience");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("Mundo '" + worldName + "' para ánfora " + id + " no encontrado");
                        return null;
                    }

                    AnforaData anforaData = new AnforaData(id, uniqueId, ownerUUID, ownerName);
                    anforaData.setLocation(new Location(world, x, y, z));
                    anforaData.setLevel(level);
                    anforaData.setExperience(experience);

                    return anforaData;
                }
            }
            
        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar ánfora " + anforaId + " de MySQL", e);
        }
        
        return null;
    }

    /**
     * Elimina un ánfora de la base de datos
     * 
     * @param anforaId ID único del ánfora a eliminar
     */
    @Override
    public void deleteAnfora(String anforaId) {
        String sql = "DELETE FROM anforas WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, anforaId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al eliminar ánfora " + anforaId + " de MySQL", e);
        }
    }

    /**
     * Obtiene todos los UUIDs únicos de ánforas colocadas
     * 
     * @return Set con todos los uniqueId
     */
    @Override
    public Set<String> getAllPlacedAnforaUUIDs() {
        Set<String> uuids = new HashSet<>();
        String sql = "SELECT uniqueId FROM anforas;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                uuids.add(rs.getString("uniqueId"));
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar UUIDs de ánforas de MySQL", e);
        }
        
        return uuids;
    }

    /**
     * Obtiene todos los IDs de ánforas (basados en ubicación)
     * 
     * @return Set con todos los IDs
     */
    @Override
    public Set<String> getAllAnforaIds() {
        Set<String> ids = new HashSet<>();
        String sql = "SELECT id FROM anforas;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar IDs de ánforas de MySQL", e);
        }
        
        return ids;
    }

    /**
     * Obtiene el mapeo completo uniqueId -> anforaId
     * Útil para sincronización y verificación de datos
     * 
     * @return Map con el mapeo uniqueId -> anforaId
     */
    @Override
    public Map<String, String> getUniqueIdToAnforaIdMap() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT uniqueId, id FROM anforas;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String uniqueId = rs.getString("uniqueId");
                String id = rs.getString("id");
                map.put(uniqueId, id);
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar mapeo uniqueId->anforaId de MySQL", e);
        }
        
        return map;
    }

    /**
     * Cierra el connection pool de HikariCP
     * CRÍTICO: Este método debe ser llamado en onDisable() del plugin
     * para liberar todos los recursos correctamente
     */
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("✓ HikariCP Connection Pool cerrado correctamente");
        }
    }
}
