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

public class MariaDbStorage implements StorageEngine {

    private final AnforaMain plugin;
    private HikariDataSource dataSource;
    private final String host;
    private final int port;
    private final String database;

    public MariaDbStorage(AnforaMain plugin) throws SQLException {
        this.plugin = plugin;

        FileConfiguration dbConfig = plugin.getDatabaseManager().getDatabaseConfig();
        this.host = dbConfig.getString("mariadb.host", "localhost");
        this.port = dbConfig.getInt("mariadb.port", 3306);
        this.database = dbConfig.getString("mariadb.database", "anforadb");

        initializeConnectionPool(dbConfig);
        createTables();
    }

    private void initializeConnectionPool(FileConfiguration dbConfig) throws SQLException {
        try {
            HikariConfig config = new HikariConfig();

            String user = dbConfig.getString("mariadb.user", "root");
            String password = dbConfig.getString("mariadb.password", "");
            boolean useSSL = dbConfig.getBoolean("mariadb.use-ssl", false);

            config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
            config.setUsername(user);
            config.setPassword(password);

            config.setMaximumPoolSize(dbConfig.getInt("mariadb.pool.maximum-pool-size", 10));
            config.setMinimumIdle(dbConfig.getInt("mariadb.pool.minimum-idle", 2));
            config.setConnectionTimeout(dbConfig.getLong("mariadb.pool.connection-timeout", 10000));
            config.setIdleTimeout(dbConfig.getLong("mariadb.pool.idle-timeout", 600000));
            config.setMaxLifetime(dbConfig.getLong("mariadb.pool.max-lifetime", 1800000));
            config.setLeakDetectionThreshold(dbConfig.getLong("mariadb.pool.leak-detection-threshold", 60000));
            config.setPoolName("AnforaXP-MariaDB-Pool");

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

            config.addDataSourceProperty("useSSL", String.valueOf(useSSL));
            if (!useSSL) {
                config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
            }

            config.addDataSourceProperty("serverTimezone", "UTC");
            config.addDataSourceProperty("characterEncoding", "utf8mb4");
            config.addDataSourceProperty("useUnicode", "true");
            config.setConnectionTestQuery("SELECT 1");

            this.dataSource = new HikariDataSource(config);

            plugin.getLogger().info("========================================");
            plugin.getLogger().info("✓ HikariCP Connection Pool inicializado para MariaDB");
            plugin.getLogger().info("  - Host: " + host + ":" + port);
            plugin.getLogger().info("  - Database: " + database);
            plugin.getLogger().info("  - Pool Size: " + config.getMaximumPoolSize());
            plugin.getLogger().info("  - Min Idle: " + config.getMinimumIdle());
            plugin.getLogger().info("========================================");

        } catch (Exception e) {
            throw new SQLException("Error al inicializar HikariCP para MariaDB: " + e.getMessage(), e);
        }
    }

    private Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Connection pool no disponible");
        }
        return dataSource.getConnection();
    }

    private void createTables() throws SQLException {
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "anforaCount INT NOT NULL DEFAULT 0," +
                "isActive BOOLEAN NOT NULL DEFAULT FALSE," +
                "INDEX idx_active (isActive)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

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
                "experience INT NOT NULL DEFAULT 0," +
                "INDEX idx_uniqueId (uniqueId)," +
                "INDEX idx_ownerUUID (ownerUUID)," +
                "INDEX idx_location (world, x, y, z)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute(createPlayersTable);
            stmt.execute(createAnforasTable);

            plugin.getLogger().info("✓ Tablas de MariaDB creadas o verificadas");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al crear tablas MariaDB", e);
            throw e;
        }
    }

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
            plugin.getLogger().log(Level.SEVERE, "Error al guardar datos del jugador " + uuid + " en MariaDB", e);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        String sql = "SELECT anforaCount, isActive FROM players WHERE uuid = ?;";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PlayerData playerData = new PlayerData(uuid);
                    playerData.setAnforaCount(rs.getInt("anforaCount"));
                    playerData.setActive(rs.getBoolean("isActive"));
                    return playerData;
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar datos del jugador " + uuid + " de MariaDB", e);
        }

        return null;
    }

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
            pstmt.setInt(10, data.getExperience());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar ánfora " + data.getId() + " en MariaDB", e);
        }
    }

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
                    int experience = rs.getInt("experience");

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
            plugin.getLogger().log(Level.SEVERE, "Error al cargar ánfora " + anforaId + " de MariaDB", e);
        }

        return null;
    }

    @Override
    public void deleteAnfora(String anforaId) {
        String sql = "DELETE FROM anforas WHERE id = ?;";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, anforaId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al eliminar ánfora " + anforaId + " de MariaDB", e);
        }
    }

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
            plugin.getLogger().log(Level.SEVERE, "Error al cargar UUIDs de ánforas de MariaDB", e);
        }

        return uuids;
    }

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
            plugin.getLogger().log(Level.SEVERE, "Error al cargar IDs de ánforas de MariaDB", e);
        }

        return ids;
    }

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
            plugin.getLogger().log(Level.SEVERE, "Error al cargar mapeo uniqueId->anforaId de MariaDB", e);
        }

        return map;
    }

    @Override
    public java.util.List<AnforaData> getAnforasByOwner(UUID ownerUUID) {
        java.util.List<AnforaData> anforas = new java.util.ArrayList<>();
        String sql = "SELECT * FROM anforas WHERE ownerUUID = ?;";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ownerUUID.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    UUID uniqueId = UUID.fromString(rs.getString("uniqueId"));
                    String ownerName = rs.getString("ownerName");
                    String worldName = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    int level = rs.getInt("level");
                    int experience = rs.getInt("experience");

                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("Mundo '" + worldName + "' para ánfora " + id + " no encontrado");
                        continue;
                    }

                    AnforaData anforaData = new AnforaData(id, uniqueId, ownerUUID, ownerName);
                    anforaData.setLocation(new org.bukkit.Location(world, x, y, z));
                    anforaData.setLevel(level);
                    anforaData.setExperience(experience);
                    anforas.add(anforaData);
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE,
                    "Error al cargar ánforas del jugador " + ownerUUID + " de MariaDB", e);
        }

        return anforas;
    }

    @Override
    public java.util.List<AnforaData> loadAllAnforas() {
        java.util.List<AnforaData> anforas = new java.util.ArrayList<>();
        String sql = "SELECT * FROM anforas;";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                UUID uniqueId = UUID.fromString(rs.getString("uniqueId"));
                UUID ownerUUID = UUID.fromString(rs.getString("ownerUUID"));
                String ownerName = rs.getString("ownerName");
                String worldName = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                int level = rs.getInt("level");
                int experience = rs.getInt("experience");

                org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Mundo '" + worldName + "' para ánfora " + id + " no encontrado");
                    continue;
                }

                AnforaData anforaData = new AnforaData(id, uniqueId, ownerUUID, ownerName);
                anforaData.setLocation(new org.bukkit.Location(world, x, y, z));
                anforaData.setLevel(level);
                anforaData.setExperience(experience);
                anforas.add(anforaData);
            }

        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error al cargar todas las ánforas de MariaDB", e);
        }

        return anforas;
    }

    @Override
    public void saveTransaction(TransactionData transaction) {
        // Crear tabla si no existe (lazy creation)
        ensureTransactionsTableExists();

        String sql = "INSERT INTO transactions (id, playerUUID, anforaId, type, amount, timestamp) VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getId().toString());
            pstmt.setString(2, transaction.getPlayerUUID().toString());
            pstmt.setString(3, transaction.getAnforaId());
            pstmt.setString(4, transaction.getType().name());
            pstmt.setLong(5, transaction.getAmount());
            pstmt.setTimestamp(6, Timestamp.valueOf(transaction.getTimestamp()));
            pstmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar transacción en MariaDB", e);
        }
    }

    @Override
    public java.util.List<TransactionData> loadTransactionsByPlayer(UUID playerUUID) {
        java.util.List<TransactionData> transactions = new java.util.ArrayList<>();

        String sql = "SELECT * FROM transactions WHERE playerUUID = ? ORDER BY timestamp DESC LIMIT 100;";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUUID.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    String anforaId = rs.getString("anforaId");
                    TransactionData.TransactionType type = TransactionData.TransactionType
                            .valueOf(rs.getString("type"));
                    long amount = rs.getLong("amount");
                    java.time.LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

                    transactions.add(new TransactionData(id, playerUUID, anforaId, type, amount, timestamp));
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Error al cargar transacciones del jugador " + playerUUID + " de MariaDB", e);
        }

        return transactions;
    }

    private void ensureTransactionsTableExists() {
        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "playerUUID VARCHAR(36) NOT NULL," +
                "anforaId VARCHAR(255) NOT NULL," +
                "type VARCHAR(20) NOT NULL," +
                "amount BIGINT NOT NULL," +
                "timestamp DATETIME NOT NULL," +
                "INDEX idx_playerUUID (playerUUID)," +
                "INDEX idx_timestamp (timestamp)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTransactionsTable);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al crear tabla de transacciones en MariaDB", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("✓ HikariCP Connection Pool cerrado correctamente");
        }
    }
}