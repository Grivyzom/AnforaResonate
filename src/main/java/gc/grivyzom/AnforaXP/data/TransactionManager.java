package gc.grivyzom.AnforaXP.data;

import gc.grivyzom.AnforaXP.AnforaMain;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de transacciones de XP
 * Almacena en memoria y persiste según el StorageEngine
 */
public class TransactionManager {

    private final AnforaMain plugin;
    private final StorageEngine storage;

    // Cache de transacciones por jugador (últimas 100 por jugador)
    private final Map<UUID, List<TransactionData>> transactionCache = new ConcurrentHashMap<>();
    private static final int MAX_TRANSACTIONS_PER_PLAYER = 100;

    public TransactionManager(AnforaMain plugin, StorageEngine storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /**
     * Registra una nueva transacción de depósito
     */
    public void logDeposit(UUID playerUUID, String anforaId, long amount) {
        if (amount <= 0)
            return;

        TransactionData transaction = new TransactionData(
                playerUUID,
                anforaId,
                TransactionData.TransactionType.DEPOSIT,
                amount);

        addTransaction(transaction);
        saveTransactionAsync(transaction);
    }

    /**
     * Registra una nueva transacción de retiro
     */
    public void logWithdraw(UUID playerUUID, String anforaId, long amount) {
        if (amount <= 0)
            return;

        TransactionData transaction = new TransactionData(
                playerUUID,
                anforaId,
                TransactionData.TransactionType.WITHDRAW,
                amount);

        addTransaction(transaction);
        saveTransactionAsync(transaction);
    }

    /**
     * Obtiene las transacciones de un jugador (ordenadas por fecha descendente)
     */
    public List<TransactionData> getTransactionsByPlayer(UUID playerUUID) {
        // Intentar cargar desde cache
        List<TransactionData> cached = transactionCache.get(playerUUID);
        if (cached != null) {
            return new ArrayList<>(cached);
        }

        // Cargar desde storage
        List<TransactionData> transactions = storage.loadTransactionsByPlayer(playerUUID);

        // Ordenar por fecha descendente
        transactions.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // Limitar y cachear
        if (transactions.size() > MAX_TRANSACTIONS_PER_PLAYER) {
            transactions = transactions.subList(0, MAX_TRANSACTIONS_PER_PLAYER);
        }

        transactionCache.put(playerUUID, new ArrayList<>(transactions));

        return transactions;
    }

    /**
     * Añade una transacción al cache
     */
    private void addTransaction(TransactionData transaction) {
        List<TransactionData> playerTransactions = transactionCache.computeIfAbsent(
                transaction.getPlayerUUID(),
                k -> new ArrayList<>());

        // Añadir al principio (más reciente primero)
        playerTransactions.add(0, transaction);

        // Limitar tamaño
        while (playerTransactions.size() > MAX_TRANSACTIONS_PER_PLAYER) {
            playerTransactions.remove(playerTransactions.size() - 1);
        }
    }

    /**
     * Guarda una transacción de forma asíncrona
     */
    private void saveTransactionAsync(TransactionData transaction) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                storage.saveTransaction(transaction);
            } catch (Exception e) {
                plugin.getLogger().severe("Error guardando transacción: " + e.getMessage());
            }
        });
    }

    /**
     * Limpia el cache de un jugador
     */
    public void clearCache(UUID playerUUID) {
        transactionCache.remove(playerUUID);
    }

    /**
     * Limpia todo el cache
     */
    public void clearAllCache() {
        transactionCache.clear();
    }
}
