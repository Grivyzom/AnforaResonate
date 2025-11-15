package gc.grivyzom.AnforaXP.data;

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

public class MySqlStorage implements StorageEngine {

    private final AnforaMain plugin;
    private Connection connection;

    public MySqlStorage(AnforaMain plugin) throws SQLException {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() throws SQLException {
        FileConfiguration dbConfig = plugin.getDatabaseManager().getDatabaseConfig();

        String host = dbConfig.getString("mysql.host", "localhost");
        int port = dbConfig.getInt("mysql.port", 3306);
        String database = dbConfig.getString("mysql.database", "anforadb");
        String user = dbConfig.getString("mysql.user", "root");
        String password = dbConfig.getString("mysql.password", "");
        boolean useSSL = dbConfig.getBoolean("mysql.use-ssl", false);

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL;

        this.connection = DriverManager.getConnection(url, user, password);
        plugin.getLogger().info("Conexión con MySQL establecida.");
    }

    private void createTables() throws SQLException {
        if (connection == null) {
            throw new SQLException("No hay conexión con la base de datos para crear las tablas.");
        }
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "anforaCount INT NOT NULL," +
                "isActive BOOLEAN NOT NULL" +
                ");";

        String createAnforasTable = "CREATE TABLE IF NOT EXISTS anforas (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "uniqueId VARCHAR(36) NOT NULL," +
                "ownerUUID VARCHAR(36) NOT NULL," +
                "ownerName VARCHAR(16) NOT NULL," + // Columna añadida
                "world VARCHAR(255) NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "level INT NOT NULL," +
                "experience DOUBLE NOT NULL" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createPlayersTable);
            statement.execute(createAnforasTable);
            plugin.getLogger().info("Tablas de MySQL creadas o verificadas.");
        }
    }

    @Override
    public void savePlayerData(UUID uuid, PlayerData data) {
        String sql = "INSERT INTO players (uuid, anforaCount, isActive) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE anforaCount = VALUES(anforaCount), isActive = VALUES(isActive);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setInt(2, data.getAnforaCount());
            pstmt.setBoolean(3, data.isActive());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar los datos del jugador " + uuid + " en MySQL.", e);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        PlayerData playerData = new PlayerData(uuid);
        String sql = "SELECT anforaCount, isActive FROM players WHERE uuid = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    playerData.setAnforaCount(rs.getInt("anforaCount"));
                    playerData.setActive(rs.getBoolean("isActive"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo cargar los datos del jugador " + uuid + " de MySQL.", e);
        }
        return playerData;
    }

    @Override
    public void saveAnfora(AnforaData data) {
        String sql = "INSERT INTO anforas (id, uniqueId, ownerUUID, ownerName, world, x, y, z, level, experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE uniqueId = VALUES(uniqueId), ownerUUID = VALUES(ownerUUID), ownerName = VALUES(ownerName), world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), level = VALUES(level), experience = VALUES(experience);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.getId());
            pstmt.setString(2, data.getUniqueId().toString());
            pstmt.setString(3, data.getOwnerUUID().toString());
            pstmt.setString(4, data.getOwnerName()); // Parámetro añadido
            pstmt.setString(5, data.getLocation().getWorld().getName());
            pstmt.setInt(6, data.getLocation().getBlockX());
            pstmt.setInt(7, data.getLocation().getBlockY());
            pstmt.setInt(8, data.getLocation().getBlockZ());
            pstmt.setInt(9, data.getLevel());
            pstmt.setDouble(10, data.getExperience());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar el ánfora " + data.getId() + " en MySQL.", e);
        }
    }

    @Override
    public AnforaData loadAnfora(String anforaId) {
        String sql = "SELECT * FROM anforas WHERE id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, anforaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    UUID uniqueId = UUID.fromString(rs.getString("uniqueId"));
                    UUID ownerUUID = UUID.fromString(rs.getString("ownerUUID"));
                    String ownerName = rs.getString("ownerName"); // Parámetro añadido
                    String worldName = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    int level = rs.getInt("level");
                    double experience = rs.getDouble("experience");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("Mundo '" + worldName + "' para el ánfora " + id + " no encontrado.");
                        return null;
                    }

                    AnforaData anforaData = new AnforaData(id, uniqueId, ownerUUID, ownerName); // Constructor corregido
                    anforaData.setLocation(new Location(world, x, y, z));
                    anforaData.setLevel(level);
                    anforaData.setExperience(experience);

                    return anforaData;
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo cargar el ánfora " + anforaId + " de MySQL.", e);
        }
        return null;
    }

    @Override
    public void deleteAnfora(String anforaId) {
        String sql = "DELETE FROM anforas WHERE id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, anforaId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo borrar el ánfora " + anforaId + " de MySQL.", e);
        }
    }

    @Override
    public Set<String> getAllPlacedAnforaUUIDs() {
        Set<String> uuids = new HashSet<>();
        String sql = "SELECT uniqueId FROM anforas;";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                uuids.add(rs.getString("uniqueId"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo cargar los UUIDs de las ánforas desde la base de datos MySQL.", e);
        }
        return uuids;
    }

    @Override
    public Set<String> getAllAnforaIds() {
        Set<String> ids = new HashSet<>();
        String sql = "SELECT id FROM anforas;";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo cargar los IDs de las ánforas desde la base de datos MySQL.", e);
        }
        return ids;
    }

    @Override
    public Map<String, String> getUniqueIdToAnforaIdMap() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT uniqueId, id FROM anforas;";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String uniqueId = rs.getString("uniqueId");
                String id = rs.getString("id");
                map.put(uniqueId, id);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo cargar el mapeo uniqueId -> anforaId desde la base de datos MySQL.", e);
        }
        return map;
    }
}
