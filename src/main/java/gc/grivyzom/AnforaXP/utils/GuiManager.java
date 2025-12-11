package gc.grivyzom.AnforaXP.utils;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor premium de GUI para Ánfora Resonante
 * Diseño mejorado con información detallada y visuales atractivos
 */
public class GuiManager {

    private final AnforaMain plugin;

    // Colores premium (HEX)
    private static final TextColor GOLD_PRIMARY = TextColor.fromHexString("#FFD700");
    private static final TextColor PURPLE_ACCENT = TextColor.fromHexString("#9B59B6");
    private static final TextColor CYAN_INFO = TextColor.fromHexString("#00CED1");
    private static final TextColor GREEN_DEPOSIT = TextColor.fromHexString("#2ECC71");
    private static final TextColor RED_WITHDRAW = TextColor.fromHexString("#E74C3C");
    private static final TextColor BLUE_PROGRESS = TextColor.fromHexString("#3498DB");

    public static final String ANFORA_GUI_TITLE = "✦ Ánfora Resonante ✦";

    public GuiManager(AnforaMain plugin) {
        this.plugin = plugin;
    }

    public void openAnforaGui(Player player, AnforaData anforaData) {
        // Sonido premium al abrir
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 1.2f);

        Inventory gui = Bukkit.createInventory(null, 45, ANFORA_GUI_TITLE);

        // Obtener datos del ánfora
        int currentLevel = anforaData.getLevel();
        int currentXp = anforaData.getExperience();
        int maxLevel = LevelManager.getMaxLevel();
        int xpCapacity = LevelManager.getXpCapacityForLevel(currentLevel);
        int playerTotalXp = ExperienceManager.getTotalExperience(player);

        // Calcular progreso
        int xpForCurrentLevel = LevelManager.getXpForLevel(currentLevel);
        int xpNeededForNextLevel = xpCapacity - xpForCurrentLevel;
        int xpProgressInLevel = currentXp - xpForCurrentLevel;
        double progressPercentage = xpNeededForNextLevel > 0
                ? ((double) xpProgressInLevel / xpNeededForNextLevel) * 100
                : 100;

        // === FILA 1: INFORMACIÓN PRINCIPAL ===
        fillRow(gui, 0, Material.BLACK_STAINED_GLASS_PANE);

        // Slot 4: Información General (LIBRO)
        gui.setItem(4, createInfoItem(anforaData, currentLevel, currentXp));

        // === FILA 2: NIVEL Y MEJORA ===
        fillRow(gui, 9, Material.GRAY_STAINED_GLASS_PANE);

        // Slot 11: Nivel actual con costo de mejora
        gui.setItem(11, createLevelItem(anforaData, currentLevel, maxLevel, currentXp, xpCapacity));

        // Slot 13: Botón de mejora (si está disponible)
        gui.setItem(13, createUpgradeButton(anforaData, currentLevel, maxLevel, currentXp, xpCapacity));

        // Slot 15: Tu experiencia actual
        gui.setItem(15, createPlayerXpItem(player, playerTotalXp));

        // === FILA 3: PROGRESO DE EXPERIENCIA ===
        fillRow(gui, 18, Material.GRAY_STAINED_GLASS_PANE);

        // Slots 19-25: Barra de progreso visual
        createVisualProgressBar(gui, xpProgressInLevel, xpNeededForNextLevel, progressPercentage, currentLevel,
                maxLevel);

        // === FILA 4: ACCIONES DEPOSITAR / RETIRAR ===
        fillRow(gui, 27, Material.GRAY_STAINED_GLASS_PANE);

        // Depositar (izquierda)
        gui.setItem(28, createActionItem(Material.LIME_DYE, "§a§l➕ Depositar 1 Nivel",
                "§7Deposita §e1 nivel§7 de experiencia", "§7en el ánfora.", "", "§a▸ Click para depositar"));
        gui.setItem(29, createActionItem(Material.LIME_DYE, "§a§l➕ Depositar 5 Niveles",
                "§7Deposita §e5 niveles§7 de experiencia", "§7en el ánfora.", "", "§a▸ Click para depositar"));
        gui.setItem(30, createActionItem(Material.LIME_DYE, "§a§l➕ Depositar 10 Niveles",
                "§7Deposita §e10 niveles§7 de experiencia", "§7en el ánfora.", "", "§a▸ Click para depositar"));

        // Divisor central
        gui.setItem(31, createDivider());

        // Retirar (derecha)
        gui.setItem(32, createActionItem(Material.RED_DYE, "§c§l➖ Retirar 1 Nivel",
                "§7Retira §e1 nivel§7 de experiencia", "§7del ánfora.", "", "§c▸ Click para retirar"));
        gui.setItem(33, createActionItem(Material.RED_DYE, "§c§l➖ Retirar 5 Niveles",
                "§7Retira §e5 niveles§7 de experiencia", "§7del ánfora.", "", "§c▸ Click para retirar"));
        gui.setItem(34, createActionItem(Material.RED_DYE, "§c§l➖ Retirar 10 Niveles",
                "§7Retira §e10 niveles§7 de experiencia", "§7del ánfora.", "", "§c▸ Click para retirar"));

        // === FILA 5: BORDE INFERIOR ===
        fillRow(gui, 36, Material.BLACK_STAINED_GLASS_PANE);

        // Slot 40: Cerrar
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(Component.text("✖ Cerrar")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            closeItem.setItemMeta(closeMeta);
        }
        gui.setItem(40, closeItem);

        player.openInventory(gui);
        plugin.getGuiListener().addPlayer(player.getUniqueId(), anforaData.getId());
    }

    /**
     * Crea el ítem de información general del ánfora
     */
    private ItemStack createInfoItem(AnforaData anforaData, int level, int totalXp) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("📋 Información del Ánfora")
                    .color(GOLD_PRIMARY)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());

            // Dueño
            lore.add(Component.text("  👤 Propietario: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(anforaData.getOwnerName())
                            .color(NamedTextColor.WHITE)));

            // Nivel
            lore.add(Component.text("  ⭐ Nivel: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(String.valueOf(level))
                            .color(GOLD_PRIMARY)
                            .decoration(TextDecoration.BOLD, true)));

            // XP Total almacenada
            lore.add(Component.text("  ⚡ XP Almacenada: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(NumberFormatter.formatWithCommas(totalXp))
                            .color(NamedTextColor.GREEN)));

            // Capacidad máxima
            int capacity = LevelManager.getXpCapacityForLevel(level);
            lore.add(Component.text("  📦 Capacidad: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(NumberFormatter.formatWithCommas(capacity))
                            .color(NamedTextColor.AQUA)));

            // Ubicación
            lore.add(Component.empty());
            if (anforaData.getLocation() != null && anforaData.getLocation().getWorld() != null) {
                lore.add(Component.text("  📍 Ubicación: ")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(anforaData.getLocation().getWorld().getName())
                                .color(NamedTextColor.WHITE)));
                lore.add(Component.text("     X: " + anforaData.getLocation().getBlockX() +
                        " Y: " + anforaData.getLocation().getBlockY() +
                        " Z: " + anforaData.getLocation().getBlockZ())
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }

            // ID único (resumido)
            lore.add(Component.empty());
            String shortId = anforaData.getUniqueId().toString().substring(0, 8) + "...";
            lore.add(Component.text("  🔑 ID: ")
                    .color(NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(shortId)
                            .color(NamedTextColor.DARK_GRAY)));

            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Crea el ítem que muestra el nivel actual y costo de mejora
     */
    private ItemStack createLevelItem(AnforaData anforaData, int currentLevel, int maxLevel, int currentXp,
            int xpCapacity) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("⭐ Nivel del Ánfora")
                    .color(GOLD_PRIMARY)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());

            // Nivel actual con estilo
            lore.add(Component.text("  Nivel Actual: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("★ " + currentLevel)
                            .color(GOLD_PRIMARY)
                            .decoration(TextDecoration.BOLD, true)));

            lore.add(Component.empty());

            if (currentLevel >= maxLevel) {
                // Nivel máximo alcanzado
                lore.add(Component.text("  ✦ ¡NIVEL MÁXIMO!")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true));
                lore.add(Component.text("  Has alcanzado el máximo")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, true));
            } else {
                // Mostrar costo de siguiente nivel
                int xpNeeded = xpCapacity - currentXp;

                lore.add(Component.text("  ─────────────────")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));

                lore.add(Component.text("  📈 Siguiente Nivel: ")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("★ " + (currentLevel + 1))
                                .color(NamedTextColor.GREEN)));

                lore.add(Component.empty());

                // XP necesaria para subir
                lore.add(Component.text("  ⚡ XP Necesaria: ")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(NumberFormatter.formatWithCommas(Math.max(0, xpNeeded)))
                                .color(xpNeeded <= 0 ? NamedTextColor.GREEN : NamedTextColor.RED)));

                // Calcular niveles de jugador aproximados
                int approxPlayerLevels = calculateApproxLevels(xpNeeded);
                lore.add(Component.text("  📊 Aprox. Niveles: ")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("~" + approxPlayerLevels + " niveles")
                                .color(NamedTextColor.YELLOW)));

                // Estado
                lore.add(Component.empty());
                if (currentXp >= xpCapacity) {
                    lore.add(Component.text("  ✓ ¡Lista para mejorar!")
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false)
                            .decoration(TextDecoration.BOLD, true));
                } else {
                    int percentage = (int) (((double) currentXp / xpCapacity) * 100);
                    lore.add(Component.text("  Progreso: " + percentage + "%")
                            .color(NamedTextColor.AQUA)
                            .decoration(TextDecoration.ITALIC, false));
                }
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Crea el botón de mejora
     */
    private ItemStack createUpgradeButton(AnforaData anforaData, int currentLevel, int maxLevel, int currentXp,
            int xpCapacity) {
        boolean canUpgrade = LevelManager.canLevelUp(anforaData);

        ItemStack item = new ItemStack(canUpgrade ? Material.NETHER_STAR : Material.COAL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (currentLevel >= maxLevel) {
                meta.displayName(Component.text("🏆 Nivel Máximo")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("  ¡Has alcanzado el nivel")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("  máximo de tu ánfora!")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            } else if (canUpgrade) {
                meta.displayName(Component.text("⬆ ¡MEJORAR ÁNFORA!")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("  Click para subir a")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("  ★ Nivel " + (currentLevel + 1))
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true));
                lore.add(Component.empty());
                lore.add(Component.text("  ▸ Click para mejorar")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            } else {
                meta.displayName(Component.text("⬆ Mejorar Ánfora")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));

                int xpNeeded = xpCapacity - currentXp;
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("  Necesitas más XP para")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("  subir de nivel.")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(Component.text("  Faltan: " + NumberFormatter.formatWithCommas(xpNeeded) + " XP")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Crea el ítem que muestra la XP del jugador
     */
    private ItemStack createPlayerXpItem(Player player, int playerXp) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("👤 Tu Experiencia")
                    .color(CYAN_INFO)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("  Jugador: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(player.getName())
                            .color(NamedTextColor.WHITE)));
            lore.add(Component.text("  Nivel: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(String.valueOf(player.getLevel()))
                            .color(NamedTextColor.GREEN)));
            lore.add(Component.text("  XP Total: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(NumberFormatter.formatWithCommas(playerXp))
                            .color(NamedTextColor.GREEN)));
            lore.add(Component.empty());
            lore.add(Component.text("  Esta es la XP que")
                    .color(NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("  puedes depositar")
                    .color(NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, true));

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Crea la barra de progreso visual en el GUI
     */
    private void createVisualProgressBar(Inventory gui, int xpProgress, int xpNeeded, double percentage,
            int currentLevel, int maxLevel) {
        // 7 slots para la barra (19-25)
        int filledSlots = (int) Math.ceil((percentage / 100.0) * 7);

        for (int i = 0; i < 7; i++) {
            int slot = 19 + i;
            boolean isFilled = i < filledSlots;

            Material material;
            if (currentLevel >= maxLevel) {
                material = Material.GOLD_BLOCK;
            } else if (isFilled) {
                material = Material.LIME_STAINED_GLASS_PANE;
            } else {
                material = Material.RED_STAINED_GLASS_PANE;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (currentLevel >= maxLevel) {
                    meta.displayName(Component.text("✦ ¡NIVEL MÁXIMO!")
                            .color(NamedTextColor.GOLD)
                            .decoration(TextDecoration.ITALIC, false)
                            .decoration(TextDecoration.BOLD, true));
                } else {
                    String progressPercent = String.format("%.1f%%", percentage);
                    meta.displayName(Component.text("📊 Progreso: " + progressPercent)
                            .color(isFilled ? NamedTextColor.GREEN : NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false));

                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(Component.text("  XP Actual: ")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text(NumberFormatter.formatWithCommas(xpProgress))
                                    .color(NamedTextColor.GREEN)));
                    lore.add(Component.text("  XP Necesaria: ")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text(NumberFormatter.formatWithCommas(xpNeeded))
                                    .color(NamedTextColor.AQUA)));
                    lore.add(Component.empty());

                    // Barra de texto
                    lore.add(buildTextProgressBar(percentage));

                    meta.lore(lore);
                }
                item.setItemMeta(meta);
            }
            gui.setItem(slot, item);
        }
    }

    /**
     * Construye una barra de progreso de texto
     */
    private Component buildTextProgressBar(double percentage) {
        StringBuilder bar = new StringBuilder();
        int progress = (int) (percentage / 5); // 20 caracteres

        for (int i = 0; i < 20; i++) {
            bar.append(i < progress ? "█" : "░");
        }

        return Component.text("  ")
                .append(Component.text(bar.toString())
                        .color(percentage >= 100 ? NamedTextColor.GREEN : BLUE_PROGRESS))
                .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Calcula niveles aproximados del jugador para una cantidad de XP
     */
    private int calculateApproxLevels(int xpAmount) {
        // Fórmula aproximada de Minecraft
        if (xpAmount <= 0)
            return 0;
        if (xpAmount <= 352)
            return (int) Math.sqrt(xpAmount / 7.0);
        if (xpAmount <= 1507)
            return (int) ((Math.sqrt(40 * xpAmount - 7839) + 81) / 10);
        return (int) ((Math.sqrt(72 * xpAmount - 54215) + 325) / 18);
    }

    /**
     * Rellena una fila con un material
     */
    private void fillRow(Inventory gui, int startSlot, Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < 9; i++) {
            gui.setItem(startSlot + i, filler.clone());
        }
    }

    /**
     * Crea un ítem de acción (depositar/retirar)
     */
    private ItemStack createActionItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Crea un divisor visual
     */
    private ItemStack createDivider() {
        ItemStack item = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("│")
                    .color(NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Método legacy para compatibilidad
    public ItemStack createGuiItem(Material material, String name, String... lore) {
        return createActionItem(material, name, lore);
    }
}