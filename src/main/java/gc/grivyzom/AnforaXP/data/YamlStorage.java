package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location; // Importar Location para el constructor de AnforaData

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class YamlStorage implements StorageEngine {

    private final AnforaMain plugin;
    private File playersFile;
    private YamlConfiguration playersConfig;
    private File anforasFile;
    private YamlConfiguration anforasConfig;

    public YamlStorage(AnforaMain plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        // Asegurarse de que la carpeta del plugin existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Inicializar players.yml
        playersFile = new File(plugin.getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo crear el archivo players.yml", e);
            }
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        // Inicializar anforas.yml
        anforasFile = new File(plugin.getDataFolder(), "anforas.yml");
        if (!anforasFile.exists()) {
            try {
                anforasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo crear el archivo anforas.yml", e);
            }
        }
        anforasConfig = YamlConfiguration.loadConfiguration(anforasFile);
    }


    @Override
    public void savePlayerData(UUID uuid, PlayerData data) {
        // La ruta en el YAML será el UUID del jugador.
        String path = uuid.toString();
        // Guardamos los datos del jugador.
        playersConfig.set(path + ".anforaCount", data.getAnforaCount());
        playersConfig.set(path + ".isActive", data.isActive()); // Guardar el nuevo campo

        // Guardar los cambios en el archivo.
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar los datos del jugador " + uuid, e);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        // Creamos un objeto PlayerData con valores por defecto.
        PlayerData playerData = new PlayerData(uuid);

        // La ruta en el YAML es el UUID del jugador.
        String path = uuid.toString();

        // Comprobamos si el jugador tiene una sección en el archivo.
        if (playersConfig.isSet(path)) {
            // Si existe, cargamos los datos y los establecemos en el objeto.
            int anforaCount = playersConfig.getInt(path + ".anforaCount", 0);
            boolean isActive = playersConfig.getBoolean(path + ".isActive", false); // Cargar el nuevo campo

            playerData.setAnforaCount(anforaCount);
            playerData.setActive(isActive);
        }

        // Devolvemos el objeto, ya sea con los datos cargados o los de por defecto.
        return playerData;
    }

    @Override
    public void saveAnfora(AnforaData data) {
        String path = data.getId();
        anforasConfig.set(path + ".uniqueId", data.getUniqueId().toString());
        anforasConfig.set(path + ".ownerUUID", data.getOwnerUUID().toString());
        anforasConfig.set(path + ".ownerName", data.getOwnerName()); // Guardar el nombre del propietario
        anforasConfig.set(path + ".location", data.getLocation());
        anforasConfig.set(path + ".level", data.getLevel());
        anforasConfig.set(path + ".experience", data.getExperience());

        try {
            anforasConfig.save(anforasFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar los datos del ánfora " + data.getId(), e);
        }
    }

    @Override
    public AnforaData loadAnfora(String anforaId) {
        String path = anforaId;
        if (!anforasConfig.isSet(path)) {
            return null;
        }

        String uniqueIdString = anforasConfig.getString(path + ".uniqueId");
        String ownerUUIDString = anforasConfig.getString(path + ".ownerUUID");
        String ownerName = anforasConfig.getString(path + ".ownerName"); // Cargar el nombre del propietario
        UUID uniqueId = null;
        UUID ownerUUID = null;

        try {
            if (uniqueIdString != null) {
                uniqueId = UUID.fromString(uniqueIdString);
            }
            if (ownerUUIDString != null) {
                ownerUUID = UUID.fromString(ownerUUIDString);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "UUID inválido para el ánfora " + anforaId, e);
            return null;
        }

        if (uniqueId == null || ownerUUID == null || ownerName == null) { // Validar también ownerName
            plugin.getLogger().log(Level.WARNING, "Anfora " + anforaId + " no tiene uniqueId, ownerUUID o ownerName. Dato corrupto o antiguo.");
            return null;
        }

        AnforaData anforaData = new AnforaData(anforaId, uniqueId, ownerUUID, ownerName); // Constructor actualizado
        anforaData.setLocation(anforasConfig.getLocation(path + ".location"));
        anforaData.setLevel(anforasConfig.getInt(path + ".level"));
        anforaData.setExperience(anforasConfig.getDouble(path + ".experience"));

        return anforaData;
    }

    @Override
    public void deleteAnfora(String anforaId) {
        // Para borrar una sección en YAML, simplemente la establecemos a null.
        anforasConfig.set(anforaId, null);

        // Guardar los cambios en el archivo.
        try {
            anforasConfig.save(anforasFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo borrar los datos del ánfora " + anforaId, e);
        }
    }

    @Override
    public Set<String> getAllPlacedAnforaUUIDs() {
        Set<String> uuids = new HashSet<>();
        // Itera sobre todas las claves de primer nivel (los anforaId)
        for (String anforaId : anforasConfig.getKeys(false)) {
            String uniqueId = anforasConfig.getString(anforaId + ".uniqueId");
            if (uniqueId != null && !uniqueId.isEmpty()) {
                uuids.add(uniqueId);
            }
        }
        return uuids;
    }
}