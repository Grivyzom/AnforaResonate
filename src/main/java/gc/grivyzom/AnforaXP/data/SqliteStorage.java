package gc.grivyzom.AnforaXP.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class SqliteStorage implements StorageEngine {

    private final AnforaMain plugin;
    private HikariDataSource dataSource;

    public SqliteStorage(AnforaMain plugin) throws SQLException {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                throw new SQLException("Could not create data folder.");
            }
        }
        File dbFile = new File(dataFolder, "database.db");

        HikariConfig config = new HikariConfig();
        config.setPoolName("AnforaXP-SQLite-Pool");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000); // 60 seconds
        config.setIdleTimeout(45000); // 45 seconds
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        this.dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connection pool for SQLite established.");
    }

    private void createTables() {
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "anforaCount INT NOT NULL," +
                "isActive BOOLEAN NOT NULL" +
                ");";

        String createAnforasTable = "CREATE TABLE IF NOT EXISTS anforas (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "uniqueId VARCHAR(36) NOT NULL," +
                "ownerUUID VARCHAR(36) NOT NULL," +
                "ownerName VARCHAR(16) NOT NULL," +
                "world VARCHAR(255) NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "level INT NOT NULL," +
                "experience INTEGER NOT NULL" +
                ");";

        String indexOwner = "CREATE INDEX IF NOT EXISTS idx_anforas_owner ON anforas(ownerUUID);";
        String indexUniqueId = "CREATE INDEX IF NOT EXISTS idx_anforas_uniqueid ON anforas(uniqueId);";

        try (Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement()) {
            statement.execute(createPlayersTable);
            statement.execute(createAnforasTable);
            statement.execute(indexOwner);
            statement.execute(indexUniqueId);
            plugin.getLogger().info("SQLite tables and indexes created or verified.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create database tables or indexes.", e);
        }
    }

    @Override
    public void savePlayerData(UUID uuid, PlayerData data) {
        String sql = "INSERT OR REPLACE INTO players (uuid, anforaCount, isActive) VALUES (?, ?, ?);";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setInt(2, data.getAnforaCount());
            pstmt.setBoolean(3, data.isActive());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player data for " + uuid, e);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        String sql = "SELECT anforaCount, isActive FROM players WHERE uuid = ?;";
        try (Connection conn = dataSource.getConnection();
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
            plugin.getLogger().log(Level.SEVERE, "Could not load player data for " + uuid, e);
        }
        return new PlayerData(uuid); // Return new data if not found
    }

    @Override
    public void saveAnfora(AnforaData data) {
        String sql = "INSERT OR REPLACE INTO anforas (id, uniqueId, ownerUUID, ownerName, world, x, y, z, level, experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        if (data.getLocation() == null || data.getLocation().getWorld() == null) {
            plugin.getLogger().severe("Attempted to save anfora " + data.getId() + " with a null location or world.");
            return;
        }
        try (Connection conn = dataSource.getConnection();
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
            plugin.getLogger().log(Level.SEVERE, "Could not save anfora " + data.getId(), e);
        }
    }

    @Override
    public AnforaData loadAnfora(String anforaId) {
        String sql = "SELECT * FROM anforas WHERE id = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, anforaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return anforaFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load anfora " + anforaId, e);
        }
        return null;
    }

    @Override
    public void deleteAnfora(String anforaId) {
        String sql = "DELETE FROM anforas WHERE id = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, anforaId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not delete anfora " + anforaId, e);
        }
    }

    @Override
    public Set<String> getAllPlacedAnforaUUIDs() {
        Set<String> uuids = new HashSet<>();
        String sql = "SELECT uniqueId FROM anforas";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                uuids.add(rs.getString("uniqueId"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load anfora UUIDs.", e);
        }
        return uuids;
    }

    @Override
    public Set<String> getAllAnforaIds() {
        Set<String> ids = new HashSet<>();
        String sql = "SELECT id FROM anforas";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load anfora IDs.", e);
        }
        return ids;
    }

    @Override
    public Map<String, String> getUniqueIdToAnforaIdMap() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT uniqueId, id FROM anforas";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("uniqueId"), rs.getString("id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load uniqueId to anforaId map.", e);
        }
        return map;
    }

    @Override
    public List<AnforaData> getAnforasByOwner(UUID ownerUUID) {
        List<AnforaData> anforas = new ArrayList<>();
        String sql = "SELECT * FROM anforas WHERE ownerUUID = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ownerUUID.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    anforas.add(anforaFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load anforas for owner " + ownerUUID, e);
        }
        return anforas;
    }

    @Override
    public List<AnforaData> loadAllAnforas() {
        List<AnforaData> anforas = new ArrayList<>();
        String sql = "SELECT * FROM anforas;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                anforas.add(anforaFromResultSet(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load all anforas.", e);
        }
        return anforas;
    }

    private AnforaData anforaFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        UUID uniqueId = UUID.fromString(rs.getString("uniqueId"));
        UUID ownerUUID = UUID.fromString(rs.getString("ownerUUID"));
        String ownerName = rs.getString("ownerName");
        String worldName = rs.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' for anfora " + id + " not found. Skipping.");
            return null;
        }
        Location location = new Location(world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));

        AnforaData data = new AnforaData(id, uniqueId, ownerUUID, ownerName);
        data.setLocation(location);
        data.setLevel(rs.getInt("level"));
        data.setExperience(rs.getInt("experience"));
        return data;
    }

    @Override
    public void saveTransaction(TransactionData transaction) {
        ensureTransactionsTableExists();

        String sql = "INSERT INTO transactions (id, playerUUID, anforaId, type, amount, timestamp) VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getId().toString());
            pstmt.setString(2, transaction.getPlayerUUID().toString());
            pstmt.setString(3, transaction.getAnforaId());
            pstmt.setString(4, transaction.getType().name());
            pstmt.setLong(5, transaction.getAmount());
            pstmt.setString(6, transaction.getTimestamp().toString());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving transaction to SQLite", e);
        }
    }

    @Override
    public List<TransactionData> loadTransactionsByPlayer(UUID playerUUID) {
        List<TransactionData> transactions = new ArrayList<>();

        String sql = "SELECT * FROM transactions WHERE playerUUID = ? ORDER BY timestamp DESC LIMIT 100;";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUUID.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    String anforaId = rs.getString("anforaId");
                    TransactionData.TransactionType type = TransactionData.TransactionType
                            .valueOf(rs.getString("type"));
                    long amount = rs.getLong("amount");
                    java.time.LocalDateTime timestamp = java.time.LocalDateTime.parse(rs.getString("timestamp"));

                    transactions.add(new TransactionData(id, playerUUID, anforaId, type, amount, timestamp));
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading transactions for player " + playerUUID + " from SQLite",
                    e);
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
                "timestamp TEXT NOT NULL" +
                ");";

        String indexPlayer = "CREATE INDEX IF NOT EXISTS idx_transactions_player ON transactions(playerUUID);";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTransactionsTable);
            stmt.execute(indexPlayer);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating transactions table in SQLite", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("✓ SQLite connection pool closed.");
        }
    }
}
