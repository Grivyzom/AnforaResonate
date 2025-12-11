package gc.grivyzom.AnforaXP.utils;

import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageManager {

    private final AnforaMain plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messages = new HashMap<>();

    public MessageManager(AnforaMain plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messages.clear();

        // Cargar mensajes por defecto desde el JAR
        try (InputStream defaultConfigStream = plugin.getResource("messages.yml")) {
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
                for (String key : defaultConfig.getKeys(true)) {
                    if (!defaultConfig.isConfigurationSection(key)) {
                        messagesConfig.addDefault(key, defaultConfig.getString(key));
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo cargar la configuración de mensajes por defecto.", e);
        }

        messagesConfig.options().copyDefaults(true);
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar la configuración de mensajes.", e);
        }

        // Cargar todos los mensajes a un mapa para acceso rápido
        for (String key : messagesConfig.getKeys(true)) {
            if (!messagesConfig.isConfigurationSection(key)) {
                messages.put(key, messagesConfig.getString(key));
            }
        }
    }

    public void reloadMessages() {
        loadMessages();
    }

    public String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, "&cMensaje no encontrado: " + key);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
