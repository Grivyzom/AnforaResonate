package gc.grivyzom.AnforaXP.utils;

import gc.grivyzom.AnforaXP.AnforaMain;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ItemFactory {

    public static NamespacedKey ANFORA_RESONANTE_KEY;
    public static NamespacedKey ANFORA_UNIQUE_ID_KEY;
    public static NamespacedKey ANFORA_LEVEL_KEY;
    public static NamespacedKey ANFORA_EXP_KEY;
    public static NamespacedKey ANFORA_OWNER_NAME_KEY; // Nueva clave

    public static void initKeys(AnforaMain plugin) {
        ANFORA_RESONANTE_KEY = new NamespacedKey(plugin, "anfora_resonante");
        ANFORA_UNIQUE_ID_KEY = new NamespacedKey(plugin, "anfora_unique_id");
        ANFORA_LEVEL_KEY = new NamespacedKey(plugin, "anfora_level");
        ANFORA_EXP_KEY = new NamespacedKey(plugin, "anfora_experience");
        ANFORA_OWNER_NAME_KEY = new NamespacedKey(plugin, "anfora_owner_name"); // Inicializar clave
    }

    public static ItemStack createAnforaItem(AnforaMain plugin, int amount) {
        return createAnforaItem(plugin, amount, null, 1, 0, null);
    }

    public static ItemStack createAnforaItem(AnforaMain plugin, int amount, UUID existingUniqueId, int level, double experience, String ownerName) {
        FileConfiguration config = plugin.getConfig();

        String name = ChatColor.translateAlternateColorCodes('&', config.getString("anfora-item.name", "&6Anfora Resonante"));
        List<String> loreTemplate = config.getStringList("anfora-item.lore");

        List<String> finalLore = new ArrayList<>();
        if (ownerName != null && !ownerName.isEmpty()) {
            finalLore.add(ChatColor.translateAlternateColorCodes('&', "&7Propietario: &e" + ownerName));
        }
        finalLore.add(ChatColor.translateAlternateColorCodes('&', "&7Nivel: &e" + level));
        finalLore.add(ChatColor.translateAlternateColorCodes('&', "&7Experiencia: &a" + String.format("%.0f", experience)));
        finalLore.add(""); // Línea en blanco
        for (String line : loreTemplate) {
            finalLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        boolean hideEnchant = config.getBoolean("anfora-item.hide-enchant", true);
        String enchantTypeString = config.getString("anfora-item.enchantment", "UNBREAKING");
        int enchantLevel = config.getInt("anfora-item.enchantment-level", 1);

        ItemStack item = new ItemStack(Material.DECORATED_POT, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(finalLore);

            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantTypeString.toLowerCase()));
            if (enchantment != null) {
                meta.addEnchant(enchantment, enchantLevel, true);
                if (hideEnchant) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            } else {
                plugin.getLogger().log(Level.WARNING, "El encantamiento '" + enchantTypeString + "' configurado para la Anfora Resonante no es válido.");
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(ANFORA_RESONANTE_KEY, PersistentDataType.STRING, "true");

            UUID finalUniqueId = (existingUniqueId != null) ? existingUniqueId : UUID.randomUUID();
            container.set(ANFORA_UNIQUE_ID_KEY, PersistentDataType.STRING, finalUniqueId.toString());
            container.set(ANFORA_LEVEL_KEY, PersistentDataType.INTEGER, level);
            container.set(ANFORA_EXP_KEY, PersistentDataType.DOUBLE, experience);
            if (ownerName != null) {
                container.set(ANFORA_OWNER_NAME_KEY, PersistentDataType.STRING, ownerName);
            }

            if (!hideEnchant) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static UUID getAnforaUniqueId(ItemStack item) {
        if (!isAnforaResonante(item)) return null;
        try {
            return UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(ANFORA_UNIQUE_ID_KEY, PersistentDataType.STRING));
        } catch (Exception e) {
            return null;
        }
    }

    public static int getAnforaLevel(ItemStack item) {
        if (!isAnforaResonante(item)) return 1;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(ANFORA_LEVEL_KEY, PersistentDataType.INTEGER, 1);
    }

    public static double getAnforaExperience(ItemStack item) {
        if (!isAnforaResonante(item)) return 0.0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(ANFORA_EXP_KEY, PersistentDataType.DOUBLE, 0.0);
    }

    public static String getAnforaOwnerName(ItemStack item) {
        if (!isAnforaResonante(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ANFORA_OWNER_NAME_KEY, PersistentDataType.STRING);
    }

    public static boolean isAnforaResonante(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(ANFORA_RESONANTE_KEY, PersistentDataType.STRING);
    }
}
