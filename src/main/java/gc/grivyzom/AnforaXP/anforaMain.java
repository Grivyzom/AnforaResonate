package gc.grivyzom.AnforaXP;
import gc.grivyzom.AnforaXP.commands.anforaCommands;
import gc.grivyzom.AnforaXP.listeners.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class anforaMain extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getCommand("anfora").setExecutor(new anforaCommands());
    }
    @Override
    public void onDisable() {
        getLogger().info("El complemento de Ánfora ha deshabilitado exitosamente");
    }
}