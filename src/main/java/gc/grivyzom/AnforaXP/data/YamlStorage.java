package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.configuration.file.YamlConfiguration;

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
    public synchronized void savePlayerData(UUID uuid, PlayerData data) {
        // La ruta en el YAML será el UUID del jugador.
        String path = uuid.toString();
        // Guardamos los datos del jugador.
        playersConfig.set(path + ".anforaCount", data.getAnforaCount());
        playersConfig.set(path + ".isActive", data.isActive()); // Guardar el nuevo campo
    }

    @Override
    public synchronized PlayerData loadPlayerData(UUID uuid) {
        String path = uuid.toString();

        if (playersConfig.isSet(path)) {
            PlayerData playerData = new PlayerData(uuid);
            int anforaCount = playersConfig.getInt(path + ".anforaCount", 0);
            boolean isActive = playersConfig.getBoolean(path + ".isActive", false);

            playerData.setAnforaCount(anforaCount);
            playerData.setActive(isActive);
            return playerData;
        }

        return null;
    }

    @Override
    public synchronized void saveAnfora(AnforaData data) {
        String path = data.getId();
        anforasConfig.set(path + ".uniqueId", data.getUniqueId().toString());
        anforasConfig.set(path + ".ownerUUID", data.getOwnerUUID().toString());
        anforasConfig.set(path + ".ownerName", data.getOwnerName()); // Guardar el nombre del propietario
        anforasConfig.set(path + ".location", data.getLocation());
        anforasConfig.set(path + ".level", data.getLevel());
        anforasConfig.set(path + ".experience", data.getExperience());
    }

    @Override
    public synchronized AnforaData loadAnfora(String anforaId) {
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
            plugin.getLogger().log(Level.WARNING,
                    "Anfora " + anforaId + " no tiene uniqueId, ownerUUID o ownerName. Dato corrupto o antiguo.");
            return null;
        }

        AnforaData anforaData = new AnforaData(anforaId, uniqueId, ownerUUID, ownerName); // Constructor actualizado
        anforaData.setLocation(anforasConfig.getLocation(path + ".location"));
        anforaData.setLevel(anforasConfig.getInt(path + ".level"));
        anforaData.setExperience(anforasConfig.getInt(path + ".experience"));

        return anforaData;
    }

    @Override
    public synchronized void deleteAnfora(String anforaId) {
        // Para borrar una sección en YAML, simplemente la establecemos a null.
        anforasConfig.set(anforaId, null);
    }

    @Override
    public synchronized Set<String> getAllPlacedAnforaUUIDs() {
        Set<String> uuids = new HashSet<>();
        for (String anforaId : anforasConfig.getKeys(false)) {
            String uniqueId = anforasConfig.getString(anforaId + ".uniqueId");
            if (uniqueId != null && !uniqueId.isEmpty()) {
                uuids.add(uniqueId);
            }
        }
        return uuids;
    }

    public synchronized void saveAllToDisk() {
        try {
            playersConfig.save(playersFile);
            anforasConfig.save(anforasFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error masivo al guardar archivos YAML", e);
        }
    }

    @Override
    public synchronized Set<String> getAllAnforaIds() {
        return anforasConfig.getKeys(false);
    }

    @Override
    public synchronized java.util.Map<String, String> getUniqueIdToAnforaIdMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        for (String anforaId : anforasConfig.getKeys(false)) {
            String uniqueId = anforasConfig.getString(anforaId + ".uniqueId");
            if (uniqueId != null && !uniqueId.isEmpty()) {
                map.put(uniqueId, anforaId);
            }
        }
        return map;
    }

    @Override
    public synchronized java.util.List<AnforaData> getAnforasByOwner(UUID ownerUUID) {
        java.util.List<AnforaData> anforas = new java.util.ArrayList<>();
        String ownerUUIDString = ownerUUID.toString();

        for (String anforaId : anforasConfig.getKeys(false)) {
            String path = anforaId;
            String storedOwnerUUID = anforasConfig.getString(path + ".ownerUUID");

            if (ownerUUIDString.equals(storedOwnerUUID)) {
                AnforaData anforaData = loadAnfora(anforaId);
                if (anforaData != null) {
                    anforas.add(anforaData);
                }
            }
        }
        return anforas;
    }

    @Override
    public synchronized java.util.List<AnforaData> loadAllAnforas() {
        java.util.List<AnforaData> anforas = new java.util.ArrayList<>();
        for (String anforaId : anforasConfig.getKeys(false)) {
            AnforaData anforaData = loadAnfora(anforaId);
            if (anforaData != null) {
                anforas.add(anforaData);
            }
        }
        return anforas;
    }

    @Override
    public synchronized void saveTransaction(TransactionData transaction) {
        ensureTransactionsFileExists();

        String path = transaction.getId().toString();
        transactionsConfig.set(path + ".playerUUID", transaction.getPlayerUUID().toString());
        transactionsConfig.set(path + ".anforaId", transaction.getAnforaId());
        transactionsConfig.set(path + ".type", transaction.getType().name());
        transactionsConfig.set(path + ".amount", transaction.getAmount());
        transactionsConfig.set(path + ".timestamp", transaction.getTimestamp().toString());

        try {
            transactionsConfig.save(transactionsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving transaction to YAML", e);
        }
    }

    @Override
    public synchronized java.util.List<TransactionData> loadTransactionsByPlayer(UUID playerUUID) {
        ensureTransactionsFileExists();

        java.util.List<TransactionData> transactions = new java.util.ArrayList<>();
        String playerUUIDStr = playerUUID.toString();

        for (String txId : transactionsConfig.getKeys(false)) {
            String storedPlayerUUID = transactionsConfig.getString(txId + ".playerUUID");

            if (playerUUIDStr.equals(storedPlayerUUID)) {
                try {
                    UUID id = UUID.fromString(txId);
                    String anforaId = transactionsConfig.getString(txId + ".anforaId");
                    TransactionData.TransactionType type = TransactionData.TransactionType.valueOf(
                            transactionsConfig.getString(txId + ".type"));
                    long amount = transactionsConfig.getLong(txId + ".amount");
                    java.time.LocalDateTime timestamp = java.time.LocalDateTime.parse(
                            transactionsConfig.getString(txId + ".timestamp"));

                    transactions.add(new TransactionData(id, playerUUID, anforaId, type, amount, timestamp));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error loading transaction " + txId, e);
                }
            }
        }

        // Ordenar por fecha descendente
        transactions.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // Limitar a 100
        if (transactions.size() > 100) {
            transactions = transactions.subList(0, 100);
        }

        return transactions;
    }

    private File transactionsFile;
    private YamlConfiguration transactionsConfig;

    private void ensureTransactionsFileExists() {
        if (transactionsFile == null) {
            transactionsFile = new File(plugin.getDataFolder(), "transactions.yml");
            if (!transactionsFile.exists()) {
                try {
                    transactionsFile.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "No se pudo crear el archivo transactions.yml", e);
                }
            }
            transactionsConfig = YamlConfiguration.loadConfiguration(transactionsFile);
        }
    }

    @Override
    public void close() {
        // YamlStorage no requiere cerrar conexiones
        plugin.getLogger().info("✓ YamlStorage cerrado correctamente");
    }
}