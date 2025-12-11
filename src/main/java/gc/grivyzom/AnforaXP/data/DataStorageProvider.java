package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;

import java.sql.SQLException;
import java.util.logging.Level;

public class DataStorageProvider {

    private StorageEngine activeStorage;

    public DataStorageProvider(DatabaseManager databaseManager, AnforaMain plugin) {
        try {
            switch (databaseManager.getDatabaseType()) {
                case MARIADB:
                    this.activeStorage = new MariaDbStorage(plugin);
                    plugin.getLogger().info("Usando MariaDB para el almacenamiento de datos.");
                    break;
                case SQLITE:
                    this.activeStorage = new SqliteStorage(plugin);
                    plugin.getLogger().info("Usando SQLite para el almacenamiento de datos.");
                    break;
                case YAML:
                default:
                    this.activeStorage = new YamlStorage(plugin);
                    plugin.getLogger().info("Usando YAML para el almacenamiento de datos.");
                    break;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al inicializar la base de datos. El plugin AnforaXP se desactivará.", e);
            this.activeStorage = null;
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public StorageEngine getActiveStorage() {
        return activeStorage;
    }
}