package gc.grivyzom.AnforaXP.data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una transacción de XP (depósito o retiro)
 */
public class TransactionData {

    public enum TransactionType {
        DEPOSIT,
        WITHDRAW
    }

    private final UUID id;
    private final UUID playerUUID;
    private final String anforaId;
    private final TransactionType type;
    private final long amount;
    private final LocalDateTime timestamp;

    public TransactionData(UUID playerUUID, String anforaId, TransactionType type, long amount) {
        this.id = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.anforaId = anforaId;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor para cargar desde DB
    public TransactionData(UUID id, UUID playerUUID, String anforaId, TransactionType type, long amount,
            LocalDateTime timestamp) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.anforaId = anforaId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getAnforaId() {
        return anforaId;
    }

    public TransactionType getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
