package gc.grivyzom.AnforaXP.utils;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.data.TransactionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de GUIs de administración premium
 * Maneja el menú principal y todos los submenús
 */
public class AdminGuiManager {

    private final AnforaMain plugin;

    // Títulos de menús (usados para identificar el menú en el listener)
    public static final String MAIN_MENU_TITLE = "✦ Banco Central de Ánforas ✦";
    public static final String LOCATIONS_MENU_TITLE = "🧭 Mis Ánforas";
    public static final String UPGRADES_MENU_TITLE = "⭐ Centro de Mejoras";
    public static final String TRANSACTIONS_MENU_TITLE = "📜 Historial de Transacciones";

    // Colores premium (HEX para Paper 1.16+)
    private static final TextColor GOLD_GRADIENT = TextColor.fromHexString("#FFD700");
    private static final TextColor PURPLE_ACCENT = TextColor.fromHexString("#9B59B6");
    private static final TextColor CYAN_ACCENT = TextColor.fromHexString("#00CED1");
    private static final TextColor GREEN_SUCCESS = TextColor.fromHexString("#2ECC71");

    public AdminGuiManager(AnforaMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Abre el menú principal de administración
     */
    public void openMainMenu(Player player) {
        // Reproducir sonido premium al abrir
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.2f);

        Inventory gui = Bukkit.createInventory(null, 36, MAIN_MENU_TITLE);

        // Obtener estadísticas del jugador
        List<AnforaData> playerAnforas = plugin.getAnforaDataManager().getAnforasByOwner(player.getUniqueId());
        long totalXp = playerAnforas.stream().mapToLong(AnforaData::getExperience).sum();
        long totalCapacity = playerAnforas.stream()
                .mapToLong(a -> LevelManager.getXpCapacityForLevel(a.getLevel()))
                .sum();

        // === FILA 1: Decoración + Info ===
        fillBorder(gui, Material.BLACK_STAINED_GLASS_PANE);

        // Ítem de información central (slot 4)
        ItemStack infoItem = createInfoItem(player, playerAnforas.size(), totalXp, totalCapacity);
        gui.setItem(4, infoItem);

        // === FILA 2: Opciones principales ===

        // 🧭 Brújula - Ubicaciones (slot 11)
        ItemStack compassItem = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compassItem.getItemMeta();
        if (compassMeta != null) {
            compassMeta.displayName(Component.text("🧭 Mis Ubicaciones")
                    .color(CYAN_ACCENT)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> compassLore = new ArrayList<>();
            compassLore.add(Component.empty());
            compassLore.add(Component.text("Ver todas tus ánforas colocadas")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            compassLore.add(Component.text("y teletransportarte a ellas.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            compassLore.add(Component.empty());
            compassLore.add(Component.text("▶ Ánforas: ")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(String.valueOf(playerAnforas.size()))
                            .color(NamedTextColor.WHITE)));
            compassLore.add(Component.empty());
            compassLore.add(Component.text("➤ Click para ver")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));

            compassMeta.lore(compassLore);
            compassItem.setItemMeta(compassMeta);
        }
        gui.setItem(11, compassItem);

        // ⭐ Nether Star - Mejoras (slot 13)
        ItemStack starItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = starItem.getItemMeta();
        if (starMeta != null) {
            starMeta.displayName(Component.text("⭐ Centro de Mejoras")
                    .color(GOLD_GRADIENT)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> starLore = new ArrayList<>();
            starLore.add(Component.empty());
            starLore.add(Component.text("Mejora tus ánforas y")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            starLore.add(Component.text("desbloquea nuevas habilidades.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            starLore.add(Component.empty());
            starLore.add(Component.text("⚠ Próximamente...")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, true));

            starMeta.lore(starLore);
            starItem.setItemMeta(starMeta);
        }
        gui.setItem(13, starItem);

        // 📜 Papel - Transacciones (slot 15)
        ItemStack paperItem = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paperItem.getItemMeta();
        if (paperMeta != null) {
            paperMeta.displayName(Component.text("📜 Historial de Transacciones")
                    .color(PURPLE_ACCENT)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> paperLore = new ArrayList<>();
            paperLore.add(Component.empty());
            paperLore.add(Component.text("Consulta el historial de")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            paperLore.add(Component.text("depósitos y retiros de XP.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            paperLore.add(Component.empty());
            paperLore.add(Component.text("➤ Click para ver")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));

            paperMeta.lore(paperLore);
            paperItem.setItemMeta(paperMeta);
        }
        gui.setItem(15, paperItem);

        // === FILA 3: Decoración ===
        // Ya cubierta por fillBorder

        // === FILA 4: Cerrar ===
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(Component.text("✖ Cerrar")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            closeItem.setItemMeta(closeMeta);
        }
        gui.setItem(31, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre el menú de ubicaciones (lista de ánforas)
     */
    public void openLocationsMenu(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

        List<AnforaData> playerAnforas = plugin.getAnforaDataManager().getAnforasByOwner(player.getUniqueId());

        int size = 54; // Cofre grande
        Inventory gui = Bukkit.createInventory(null, size, LOCATIONS_MENU_TITLE);

        // Ítem de información (slot 4)
        long totalXp = playerAnforas.stream().mapToLong(AnforaData::getExperience).sum();
        long totalCapacity = playerAnforas.stream()
                .mapToLong(a -> LevelManager.getXpCapacityForLevel(a.getLevel()))
                .sum();

        ItemStack infoItem = createInfoItem(player, playerAnforas.size(), totalXp, totalCapacity);
        gui.setItem(4, infoItem);

        // Listar ánforas (slots 9-44)
        int slot = 9;
        for (AnforaData data : playerAnforas) {
            if (slot >= 45)
                break;

            ItemStack anforaItem = new ItemStack(Material.DECORATED_POT);
            ItemMeta meta = anforaItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("Ánfora Nivel " + data.getLevel())
                        .color(GOLD_GRADIENT)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());

                int capacity = LevelManager.getXpCapacityForLevel(data.getLevel());
                int percentage = capacity > 0 ? (int) ((data.getExperience() * 100L) / capacity) : 0;

                lore.add(Component.text("⚡ XP: ")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(NumberFormatter.formatWithCommas(data.getExperience()))
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(" / " + NumberFormatter.formatWithCommas(capacity))
                                .color(NamedTextColor.GRAY)));

                lore.add(Component.text("📊 Llenado: ")
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(percentage + "%")
                                .color(getPercentageColor(percentage))));

                lore.add(Component.empty());
                lore.add(Component.text("📍 Ubicación:")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));

                if (data.getLocation() != null && data.getLocation().getWorld() != null) {
                    lore.add(Component.text("   " + data.getLocation().getWorld().getName())
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("   X: " + data.getLocation().getBlockX() +
                            " Y: " + data.getLocation().getBlockY() +
                            " Z: " + data.getLocation().getBlockZ())
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false));
                }

                lore.add(Component.empty());
                lore.add(Component.text("➤ Click para teletransportarse")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));

                meta.lore(lore);
                meta.getPersistentDataContainer().set(ItemFactory.ANFORA_UNIQUE_ID_KEY,
                        PersistentDataType.STRING, data.getUniqueId().toString());

                anforaItem.setItemMeta(meta);
            }
            gui.setItem(slot++, anforaItem);
        }

        // Botón volver (slot 49)
        gui.setItem(49, createBackButton());

        // Rellenar espacios vacíos
        fillEmpty(gui, Material.GRAY_STAINED_GLASS_PANE);

        player.openInventory(gui);
    }

    /**
     * Abre el menú de mejoras (placeholder por ahora)
     */
    public void openUpgradesMenu(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.5f);

        Inventory gui = Bukkit.createInventory(null, 27, UPGRADES_MENU_TITLE);

        // Mensaje de próximamente
        ItemStack comingSoon = new ItemStack(Material.CLOCK);
        ItemMeta meta = comingSoon.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("⏳ Próximamente...")
                    .color(GOLD_GRADIENT)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("El sistema de mejoras está")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("en desarrollo.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Incluirá:")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• Subir nivel manualmente")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• Transferir XP entre ánforas")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• Fusionar ánforas")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            comingSoon.setItemMeta(meta);
        }
        gui.setItem(13, comingSoon);

        // Botón volver
        gui.setItem(22, createBackButton());

        fillEmpty(gui, Material.GRAY_STAINED_GLASS_PANE);

        player.openInventory(gui);
    }

    /**
     * Abre el menú de historial de transacciones
     */
    public void openTransactionsMenu(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

        Inventory gui = Bukkit.createInventory(null, 54, TRANSACTIONS_MENU_TITLE);

        // Obtener transacciones del jugador
        List<TransactionData> transactions = plugin.getTransactionManager()
                .getTransactionsByPlayer(player.getUniqueId());

        // Ítem de resumen (slot 4)
        ItemStack summaryItem = new ItemStack(Material.BOOK);
        ItemMeta summaryMeta = summaryItem.getItemMeta();
        if (summaryMeta != null) {
            summaryMeta.displayName(Component.text("📊 Resumen de Actividad")
                    .color(PURPLE_ACCENT)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            // Calcular totales
            long totalDeposits = transactions.stream()
                    .filter(t -> t.getType() == TransactionData.TransactionType.DEPOSIT)
                    .mapToLong(TransactionData::getAmount)
                    .sum();
            long totalWithdrawals = transactions.stream()
                    .filter(t -> t.getType() == TransactionData.TransactionType.WITHDRAW)
                    .mapToLong(TransactionData::getAmount)
                    .sum();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Total depositado: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("+" + NumberFormatter.formatWithCommas(totalDeposits) + " XP")
                            .color(NamedTextColor.GREEN)));
            lore.add(Component.text("Total retirado: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("-" + NumberFormatter.formatWithCommas(totalWithdrawals) + " XP")
                            .color(NamedTextColor.RED)));
            lore.add(Component.empty());
            lore.add(Component.text("Transacciones: " + transactions.size())
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            summaryMeta.lore(lore);
            summaryItem.setItemMeta(summaryMeta);
        }
        gui.setItem(4, summaryItem);

        // Mostrar últimas transacciones (slots 9-44)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        int slot = 9;
        int maxTransactions = Math.min(transactions.size(), 36);

        for (int i = 0; i < maxTransactions; i++) {
            TransactionData tx = transactions.get(i);

            Material material = tx.getType() == TransactionData.TransactionType.DEPOSIT
                    ? Material.LIME_STAINED_GLASS_PANE
                    : Material.RED_STAINED_GLASS_PANE;

            ItemStack txItem = new ItemStack(material);
            ItemMeta txMeta = txItem.getItemMeta();
            if (txMeta != null) {
                String prefix = tx.getType() == TransactionData.TransactionType.DEPOSIT ? "➕" : "➖";
                TextColor color = tx.getType() == TransactionData.TransactionType.DEPOSIT
                        ? NamedTextColor.GREEN
                        : NamedTextColor.RED;

                txMeta.displayName(
                        Component.text(prefix + " " + NumberFormatter.formatWithCommas(tx.getAmount()) + " XP")
                                .color(color)
                                .decoration(TextDecoration.ITALIC, false)
                                .decoration(TextDecoration.BOLD, true));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("📅 " + tx.getTimestamp().format(formatter))
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component
                        .text("🏺 Ánfora: " + tx.getAnforaId().substring(0, Math.min(20, tx.getAnforaId().length()))
                                + "...")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));

                txMeta.lore(lore);
                txItem.setItemMeta(txMeta);
            }
            gui.setItem(slot++, txItem);
        }

        // Si no hay transacciones
        if (transactions.isEmpty()) {
            ItemStack noTxItem = new ItemStack(Material.STRUCTURE_VOID);
            ItemMeta noTxMeta = noTxItem.getItemMeta();
            if (noTxMeta != null) {
                noTxMeta.displayName(Component.text("No hay transacciones")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, true));
                noTxItem.setItemMeta(noTxMeta);
            }
            gui.setItem(22, noTxItem);
        }

        // Botón volver
        gui.setItem(49, createBackButton());

        fillEmpty(gui, Material.BLACK_STAINED_GLASS_PANE);

        player.openInventory(gui);
    }

    // === MÉTODOS AUXILIARES ===

    private ItemStack createInfoItem(Player player, int anforaCount, long totalXp, long totalCapacity) {
        ItemStack infoItem = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("📊 Tu Cuenta")
                    .color(GOLD_GRADIENT)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("👤 Propietario: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(player.getName())
                            .color(NamedTextColor.WHITE)));
            lore.add(Component.text("🏺 Ánforas: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(String.valueOf(anforaCount))
                            .color(NamedTextColor.AQUA)));
            lore.add(Component.text("⚡ XP Total: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(NumberFormatter.format(totalXp))
                            .color(NamedTextColor.GREEN)));
            lore.add(Component.text("📦 Capacidad: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(NumberFormatter.format(totalCapacity))
                            .color(NamedTextColor.BLUE)));

            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        return infoItem;
    }

    private ItemStack createBackButton() {
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(Component.text("◀ Volver al Menú Principal")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            backButton.setItemMeta(backMeta);
        }
        return backButton;
    }

    private void fillBorder(Inventory gui, Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            filler.setItemMeta(meta);
        }

        int size = gui.getSize();
        int rows = size / 9;

        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;

            // Primera y última fila, o primera y última columna
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                gui.setItem(i, filler.clone());
            }
        }
    }

    private void fillEmpty(Inventory gui, Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler.clone());
            }
        }
    }

    private TextColor getPercentageColor(int percentage) {
        if (percentage >= 100)
            return NamedTextColor.RED;
        if (percentage >= 75)
            return NamedTextColor.GOLD;
        if (percentage >= 50)
            return NamedTextColor.YELLOW;
        return NamedTextColor.GREEN;
    }

    // Método legacy para compatibilidad (usado por AdminTableListener)
    public void openAdminGui(Player player) {
        openMainMenu(player);
    }
}
