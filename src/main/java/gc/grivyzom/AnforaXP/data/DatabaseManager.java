package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DatabaseManager {

    private final AnforaMain plugin;
    private final FileConfiguration dbConfig;
    private DatabaseType databaseType;

    public enum DatabaseType {
        YAML,
        MYSQL,
        SQLITE
    }

    public DatabaseManager(AnforaMain plugin){
        this.plugin = plugin;

        plugin.saveResource("databases.yml", false);
        dbConfig = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(),"databases.yml")
        );

        loadDatabaseType();
    }

    private void loadDatabaseType() {
        String selected = plugin.getConfig().getString("database-type", "yaml").toLowerCase();

        switch (selected) {
            case "mysql":
                databaseType = DatabaseType.MYSQL;
                plugin.getLogger().info("Motor seleccionado: MySQL");
                break;
            case "sqlite":
                databaseType = DatabaseType.SQLITE;
                plugin.getLogger().info("Motor seleccionado: SQLite");
                break;

            case "yaml":
                databaseType = DatabaseType.YAML;
                plugin.getLogger().info("Motor seleccionado: Yaml");
                break;
    }
    }

   public DatabaseType getDatabaseType(){
        return databaseType;
   }

   public FileConfiguration getDatabaseConfig(){
        return dbConfig;
   }

}
