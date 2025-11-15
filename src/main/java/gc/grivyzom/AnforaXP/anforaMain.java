package gc.grivyzom.AnforaXP;

import gc.grivyzom.AnforaXP.commands.AnforaCommands;
import gc.grivyzom.AnforaXP.data.*;
import gc.grivyzom.AnforaXP.listeners.*;
import gc.grivyzom.AnforaXP.utils.GuiManager;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AnforaMain extends JavaPlugin {

    private DatabaseManager databaseManager;
    private DataStorageProvider dataStorageProvider;
    private PlayerDataManager playerDataManager;
    private AnforaDataManager anforaDataManager;
    private MessageManager messageManager;
    private GuiManager guiManager;
    private GuiListener guiListener;
    private AnforaUUIDManager anforaUUIDManager;

    @Override
    public void onEnable() {
        // Inicializar las NamespacedKeys de la ItemFactory
        gc.grivyzom.AnforaXP.utils.ItemFactory.initKeys(this);

        // 1. Cargar configuraciones
        saveDefaultConfig();
        saveResource("databases.yml", false);

        // Inicializar MessageManager
        this.messageManager = new MessageManager(this);
        this.guiManager = new GuiManager(this);

        // 2. Inicializar el gestor de base de datos
        this.databaseManager = new DatabaseManager(this);
        getLogger().info("Modo DB activo: " + databaseManager.getDatabaseType());

        // 3. Inicializar el proveedor de almacenamiento de datos
        this.dataStorageProvider = new DataStorageProvider(databaseManager, this);
        StorageEngine storage = dataStorageProvider.getActiveStorage();

        // 4. Inicializar los gestores de datos
        this.playerDataManager = new PlayerDataManager(storage);
        this.anforaDataManager = new AnforaDataManager(storage);
        this.anforaUUIDManager = new AnforaUUIDManager(storage);
        getLogger().info("Cargados " + anforaUUIDManager.getPlacedAnforaCount() + " UUIDs de ánforas existentes.");


        // 5. Registrar comandos y listeners
        AnforaCommands commandExecutor = new AnforaCommands(this);
        getCommand("anfora").setExecutor(commandExecutor);
        getCommand("anfora").setTabCompleter(commandExecutor);
        registerListeners();

        getLogger().info("El complemento de Ánfora ha habilitado exitosamente");
    }

    private void registerListeners(){
        PluginManager pm = getServer().getPluginManager();
        this.guiListener = new GuiListener(this);
        pm.registerEvents(new AnforaPlaceListener(this), this);
        pm.registerEvents(new AnforaBreakListener(this), this);
        pm.registerEvents(new AnforaInteractListener(this),this);
        pm.registerEvents(new AnforaShiftActionListener(this), this);
        pm.registerEvents(new AnforaExplosionListener(this), this);
        pm.registerEvents(new PlayerConnectionListener(this), this);

        pm.registerEvents(this.guiListener, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("El complemento de Ánfora ha deshabilitado exitosamente");
    }

    // Getters para acceder a los managers desde otras clases
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public DataStorageProvider getDataStorageProvider() { return dataStorageProvider; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public AnforaDataManager getAnforaDataManager() { return anforaDataManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public GuiListener getGuiListener() { return guiListener; }
    public AnforaUUIDManager getAnforaUUIDManager() { return anforaUUIDManager; }
}