package gc.grivyzom.AnforaXP.data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface StorageEngine {
    // Methods for player data
    void savePlayerData(UUID uuid, PlayerData data);

    PlayerData loadPlayerData(UUID uuid);

    // Methods for anfora data
    void saveAnfora(AnforaData data);

    AnforaData loadAnfora(String anforaId);

    void deleteAnfora(String anforaId);

    Set<String> getAllPlacedAnforaUUIDs();

    Set<String> getAllAnforaIds(); // New method to get all location-based anfora IDs

    Map<String, String> getUniqueIdToAnforaIdMap(); // New method to get uniqueId -> anforaId mapping

    List<AnforaData> getAnforasByOwner(UUID ownerUUID);

    List<AnforaData> loadAllAnforas();

    // Methods for transaction data
    void saveTransaction(TransactionData transaction);

    List<TransactionData> loadTransactionsByPlayer(UUID playerUUID);

    // Cleanup method - must be called on plugin disable
    void close();
}
