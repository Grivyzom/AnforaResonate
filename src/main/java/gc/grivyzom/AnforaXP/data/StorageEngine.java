package gc.grivyzom.AnforaXP.data;

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
}
