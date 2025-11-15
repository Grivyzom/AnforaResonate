package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;

import java.sql.SQLException;
import java.util.logging.Level;

public class DataStorageProvider {

    private StorageEngine activeStorage;

    public DataStorageProvider(DatabaseManager databaseManager, AnforaMain plugin) {
        switch (databaseManager.getDatabaseType()) {
            case MYSQL:
                try {
                    this.activeStorage = new MySqlStorage(plugin);
                    plugin.getLogger().info("Usando MySQL para el almacenamiento de datos.");
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error al inicializar MySQL. Cambiando a almacenamiento YAML como respaldo.", e);
                    this.activeStorage = new YamlStorage(plugin);
                    plugin.getLogger().info("Usando YAML para el almacenamiento de datos (fallback).");
                }
                break;
            case SQLITE:
                try {
                    this.activeStorage = new SqliteStorage(plugin);
                    plugin.getLogger().info("Usando SQLite para el almacenamiento de datos.");
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error al inicializar SQLite. Cambiando a almacenamiento YAML como respaldo.", e);
                    this.activeStorage = new YamlStorage(plugin);
                    plugin.getLogger().info("Usando YAML para el almacenamiento de datos (fallback).");
                }
                break;
            case YAML:
            default:
                this.activeStorage = new YamlStorage(plugin);
                plugin.getLogger().info("Usando YAML para el almacenamiento de datos.");
                break;
        }
    }

    public StorageEngine getActiveStorage() {
        return activeStorage;
    }
}