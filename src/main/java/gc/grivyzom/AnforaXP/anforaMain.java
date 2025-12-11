package gc.grivyzom.AnforaXP;

import gc.grivyzom.AnforaXP.commands.AnforaCommands;
import gc.grivyzom.AnforaXP.data.*;
import gc.grivyzom.AnforaXP.expansion.AnforaExpansion;
import gc.grivyzom.AnforaXP.listeners.*;
import gc.grivyzom.AnforaXP.tasks.AutoSaveTask;
import gc.grivyzom.AnforaXP.utils.GuiManager;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import gc.grivyzom.AnforaXP.utils.ParticleAnimations;
import gc.grivyzom.AnforaXP.utils.RewardManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AnforaMain extends JavaPlugin {

    private DatabaseManager databaseManager;
    private DataStorageProvider dataStorageProvider;
    private PlayerDataManager playerDataManager;
    private AnforaDataManager anforaDataManager;
    private MessageManager messageManager;
    private GuiManager guiManager;
    private GuiListener guiListener;
    private AnforaUUIDManager anforaUUIDManager;
    private TransactionManager transactionManager;
    private AutoSaveTask autoSaveTask;
    private static FileConfiguration anforaTypesConfig; // New field for anfora-types.yml

    @Override
    public void onEnable() {
        gc.grivyzom.AnforaXP.utils.ItemFactory.initKeys(this);

        saveDefaultConfig();

        if (!new File(getDataFolder(), "databases.yml").exists()) {
            saveResource("databases.yml", false);
        }
        if (!new File(getDataFolder(), "rewards.yml").exists()) {
            saveResource("rewards.yml", false);
        }
        if (!new File(getDataFolder(), "anfora-types.yml").exists()) {
            saveResource("anfora-types.yml", false);
        }

        anforaTypesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "anfora-types.yml")); // Load
                                                                                                                // anfora-types.yml

        LevelManager.loadLevels();
        RewardManager.loadRewards();

        this.messageManager = new MessageManager(this);
        this.guiManager = new GuiManager(this);

        this.databaseManager = new DatabaseManager(this);
        getLogger().info("Modo DB activo: " + databaseManager.getDatabaseType());

        this.dataStorageProvider = new DataStorageProvider(databaseManager, this);
        StorageEngine storage = dataStorageProvider.getActiveStorage();

        this.playerDataManager = new PlayerDataManager(this, storage);
        this.anforaUUIDManager = new AnforaUUIDManager(storage);
        this.anforaDataManager = new AnforaDataManager(this, storage, anforaUUIDManager);
        this.transactionManager = new TransactionManager(this, storage);
        getLogger().info("Cargados " + anforaUUIDManager.getPlacedAnforaCount() + " UUIDs de ánforas existentes.");

        AnforaCommands commandExecutor = new AnforaCommands(this);
        getCommand("anfora").setExecutor(commandExecutor);
        getCommand("anfora").setTabCompleter(commandExecutor);
        registerListeners();

        this.autoSaveTask = new AutoSaveTask(this, playerDataManager, anforaDataManager, storage);
        this.autoSaveTask.start();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AnforaExpansion(this).register();
            getLogger().info("Successfully registered PlaceholderAPI expansion!");
        }

        getLogger().info("El complemento de Ánfora ha habilitado exitosamente");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        this.guiListener = new GuiListener(this);
        pm.registerEvents(new AnforaPlaceListener(this), this);
        pm.registerEvents(new AnforaBreakListener(this), this);
        pm.registerEvents(new AnforaInteractListener(this), this);
        pm.registerEvents(new AnforaShiftActionListener(this), this);
        pm.registerEvents(new AnforaExplosionListener(this), this);
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(this.guiListener, this);
        pm.registerEvents(new AdminTableListener(this), this);
        pm.registerEvents(new AdminGuiListener(this), this);
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.stop();
        }

        ParticleAnimations.cancelAllAnimations();
        getLogger().info("Animaciones canceladas: " + ParticleAnimations.getActiveAnimationCount());

        getLogger().info("Realizando guardado final...");

        if (playerDataManager != null) {
            playerDataManager.saveAll();
            getLogger().info("✓ Jugadores guardados: " + playerDataManager.getCacheSize());
        }

        if (anforaDataManager != null) {
            anforaDataManager.saveAll();
            getLogger().info("✓ Ánforas guardadas: " + anforaDataManager.getCacheSize());
        }

        if (dataStorageProvider != null && dataStorageProvider.getActiveStorage() != null) {
            dataStorageProvider.getActiveStorage().close();
        }

        getLogger().info("El complemento de Ánfora ha deshabilitado exitosamente");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public DataStorageProvider getDataStorageProvider() {
        return dataStorageProvider;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public AnforaDataManager getAnforaDataManager() {
        return anforaDataManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public GuiListener getGuiListener() {
        return guiListener;
    }

    public AnforaUUIDManager getAnforaUUIDManager() {
        return anforaUUIDManager;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public static FileConfiguration getAnforaTypesConfig() { // New static getter
        return anforaTypesConfig;
    }
}
