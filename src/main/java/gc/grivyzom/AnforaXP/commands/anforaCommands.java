package gc.grivyzom.AnforaXP.commands;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.data.AnforaData;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import gc.grivyzom.AnforaXP.utils.LevelManager;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import gc.grivyzom.AnforaXP.utils.Permissions; // Import the Permissions class
import gc.grivyzom.AnforaXP.utils.RewardManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class AnforaCommands implements CommandExecutor, TabCompleter {

    private final AnforaMain plugin;
    private final MessageManager messageManager;
    private final List<CommandInfo> registeredCommands;

    public AnforaCommands(AnforaMain plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.registeredCommands = new ArrayList<>();

        registeredCommands.add(new CommandInfo("help", "Muestra la lista de comandos disponibles.", "/anfora help", Permissions.COMMAND_HELP));
        registeredCommands.add(new CommandInfo("list", "Muestra la ubicación de tus ánforas.", "/anfora list", Permissions.COMMAND_LIST));
        registeredCommands.add(new CommandInfo("give", "Da una Anfora Resonante a un jugador.", "/anfora give <jugador> [cantidad] [nivel]", Permissions.ADMIN_GIVE));
        registeredCommands.add(new CommandInfo("admintable", "Da una Mesa de Administración a un jugador.", "/anfora admintable [jugador]", Permissions.ADMIN_GIVE));
        registeredCommands.add(new CommandInfo("reload", "Recarga la configuración del plugin.", "/anfora reload", Permissions.ADMIN_RELOAD));
        registeredCommands.add(new CommandInfo("setlevel", "Establece el nivel de un ánfora.", "/anfora setlevel <nivel> [uuid]", Permissions.ADMIN_SETLEVEL));
        registeredCommands.add(new CommandInfo("setxp", "Establece la experiencia de un ánfora.", "/anfora setxp <xp> [uuid]", Permissions.ADMIN_SETXP));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            handleHelpCommand(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                handleHelpCommand(sender);
                break;
            case "give":
                handleGiveCommand(sender, args);
                break;
            case "admintable":
                handleAdminTableCommand(sender, args);
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            case "list":
                handleListCommand(sender);
                break;
            case "setlevel":
                handleSetLevelCommand(sender, args);
                break;
            case "setxp":
                handleSetXpCommand(sender, args);
                break;
            default:
                sender.sendMessage(messageManager.getMessage("unknown_command"));
                break;
        }
        return true;
    }

    private void handleSetLevelCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN_SETLEVEL)) {
            sender.sendMessage(messageManager.getMessage("no_permission"));
            return;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(messageManager.getMessage("setlevel_usage"));
            return;
        }

        int newLevel;
        try {
            newLevel = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("number", args[1]);
            sender.sendMessage(messageManager.getMessage("invalid_number", placeholders));
            return;
        }

        if (newLevel < 1 || newLevel > LevelManager.getMaxLevel()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_level", String.valueOf(LevelManager.getMaxLevel()));
            sender.sendMessage(messageManager.getMessage("invalid_level", placeholders));
            return;
        }

        AnforaData anforaToUpdate = null;
        if (args.length == 2) { // Set level for anfora in hand
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("must_be_player"));
                return;
            }
            Player player = (Player) sender;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (!ItemFactory.isAnforaResonante(itemInHand)) {
                sender.sendMessage(messageManager.getMessage("not_anfora_in_hand"));
                return;
            }

            UUID anforaUUID = ItemFactory.getAnforaUniqueId(itemInHand);
            if (anforaUUID == null) {
                sender.sendMessage(messageManager.getMessage("anfora_data_corrupted"));
                return;
            }
            anforaToUpdate = plugin.getAnforaDataManager().getAnforaByUUID(anforaUUID);

        } else { // Set level by UUID
            try {
                UUID anforaUUID = UUID.fromString(args[2]);
                anforaToUpdate = plugin.getAnforaDataManager().getAnforaByUUID(anforaUUID);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(messageManager.getMessage("invalid_uuid"));
                return;
            }
        }

        if (anforaToUpdate == null) {
            sender.sendMessage(messageManager.getMessage("anfora_not_found"));
            return;
        }

        int newXp = LevelManager.getXpForLevel(newLevel);
        anforaToUpdate.setLevel(newLevel);
        anforaToUpdate.setExperience(newXp);
        plugin.getAnforaDataManager().saveAnfora(anforaToUpdate);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("uuid", anforaToUpdate.getUniqueId().toString());
        placeholders.put("level", String.valueOf(newLevel));
        sender.sendMessage(messageManager.getMessage("setlevel_success", placeholders));
    }

    private void handleSetXpCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN_SETXP)) {
            sender.sendMessage(messageManager.getMessage("no_permission"));
            return;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(messageManager.getMessage("setxp_usage"));
            return;
        }

        int newXp;
        try {
            newXp = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("number", args[1]);
            sender.sendMessage(messageManager.getMessage("invalid_number", placeholders));
            return;
        }

        int maxCap = LevelManager.getXpForLevel(LevelManager.getMaxLevel());
        if (newXp < 0 || newXp > maxCap) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_capacity", String.valueOf(maxCap));
            sender.sendMessage(messageManager.getMessage("invalid_xp", placeholders));
            return;
        }

        AnforaData anforaToUpdate = null;
        if (args.length == 2) { // Set xp for anfora in hand
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("must_be_player"));
                return;
            }
            Player player = (Player) sender;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (!ItemFactory.isAnforaResonante(itemInHand)) {
                sender.sendMessage(messageManager.getMessage("not_anfora_in_hand"));
                return;
            }

            UUID anforaUUID = ItemFactory.getAnforaUniqueId(itemInHand);
            if (anforaUUID == null) {
                sender.sendMessage(messageManager.getMessage("anfora_data_corrupted"));
                return;
            }
            anforaToUpdate = plugin.getAnforaDataManager().getAnforaByUUID(anforaUUID);

        } else { // Set xp by UUID
            try {
                UUID anforaUUID = UUID.fromString(args[2]);
                anforaToUpdate = plugin.getAnforaDataManager().getAnforaByUUID(anforaUUID);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(messageManager.getMessage("invalid_uuid"));
                return;
            }
        }

        if (anforaToUpdate == null) {
            sender.sendMessage(messageManager.getMessage("anfora_not_found"));
            return;
        }

        int newLevel = LevelManager.getLevelFromXp(newXp);
        anforaToUpdate.setExperience(newXp);
        anforaToUpdate.setLevel(newLevel);
        plugin.getAnforaDataManager().saveAnfora(anforaToUpdate);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("uuid", anforaToUpdate.getUniqueId().toString());
        placeholders.put("xp", String.valueOf(newXp));
        placeholders.put("level", String.valueOf(newLevel));
        sender.sendMessage(messageManager.getMessage("setxp_success", placeholders));
    }

    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission(Permissions.ADMIN_RELOAD)) {
            sender.sendMessage(messageManager.getMessage("no_permission"));
            return;
        }
        plugin.reloadConfig();
        LevelManager.loadLevels();
        RewardManager.loadRewards();
        plugin.getMessageManager().reloadMessages();
        sender.sendMessage(messageManager.getMessage("reload_success"));
    }

    private void handleListCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por un jugador.");
            return;
        }

        if (!sender.hasPermission(Permissions.COMMAND_LIST)) {
            sender.sendMessage(messageManager.getMessage("no_permission"));
            return;
        }

        Player player = (Player) sender;
        List<AnforaData> anforas = plugin.getAnforaDataManager().getAnforasByOwner(player.getUniqueId());

        if (anforas.isEmpty()) {
            sender.sendMessage(messageManager.getMessage("list_no_anforas"));
            return;
        }

        sender.sendMessage(messageManager.getMessage("list_header"));
        int i = 1;
        for (AnforaData anfora : anforas) {
            if (anfora.getLocation() != null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("number", String.valueOf(i++));
                placeholders.put("world", anfora.getLocation().getWorld().getName());
                placeholders.put("x", String.valueOf(anfora.getLocation().getBlockX()));
                placeholders.put("y", String.valueOf(anfora.getLocation().getBlockY()));
                placeholders.put("z", String.valueOf(anfora.getLocation().getBlockZ()));
                placeholders.put("level", String.valueOf(anfora.getLevel()));
                placeholders.put("current_capacity", String.valueOf(anfora.getExperience()));
                placeholders.put("max_capacity", String.valueOf(LevelManager.getXpCapacityForLevel(anfora.getLevel())));
                sender.sendMessage(messageManager.getMessage("list_item", placeholders));
            }
        }
        sender.sendMessage(messageManager.getMessage("list_footer"));
    }
    
    private void handleGiveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN_GIVE)) {
            sender.sendMessage(messageManager.getMessage("no_permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(messageManager.getMessage("give_usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[1]);
            sender.sendMessage(messageManager.getMessage("player_not_found", placeholders));
            return;
        }

        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) {
                    sender.sendMessage(messageManager.getMessage("invalid_amount"));
                    return;
                }
            } catch (NumberFormatException e) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("number", args[2]);
                sender.sendMessage(messageManager.getMessage("invalid_number", placeholders));
                return;
            }
        }

        int level = 1;
        if (args.length > 3) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("number", args[3]);
                sender.sendMessage(messageManager.getMessage("invalid_number", placeholders));
                return;
            }
        }

        if (level < 1 || level > LevelManager.getMaxLevel()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_level", String.valueOf(LevelManager.getMaxLevel()));
            sender.sendMessage(messageManager.getMessage("invalid_level", placeholders));
            return;
        }

        int experience = LevelManager.getXpForLevel(level);
        ItemStack anforaItem = ItemFactory.createAnforaItem(plugin, amount, null, level, experience, null);
        target.getInventory().addItem(anforaItem);

        Map<String, String> placeholdersSender = new HashMap<>();
        placeholdersSender.put("amount", String.valueOf(amount));
        placeholdersSender.put("player", target.getName());
        placeholdersSender.put("level", String.valueOf(level));
        sender.sendMessage(messageManager.getMessage("give_success", placeholdersSender));

        Map<String, String> placeholdersTarget = new HashMap<>();
        placeholdersTarget.put("amount", String.valueOf(amount));
        placeholdersTarget.put("level", String.valueOf(level));
        target.sendMessage(messageManager.getMessage("receive_success", placeholdersTarget));
    }

    private void handleAdminTableCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN_GIVE)) {
            sender.sendMessage(messageManager.getMessage("no_permission"));
            return;
        }

        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[1]);
                sender.sendMessage(messageManager.getMessage("player_not_found", placeholders));
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("must_be_player"));
                return;
            }
            target = (Player) sender;
        }

        ItemStack adminTable = ItemFactory.createAdminTableItem(plugin);
        target.getInventory().addItem(adminTable);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        sender.sendMessage("§aHas dado una Mesa de Administración a " + target.getName());
        target.sendMessage("§aHas recibido una Mesa de Administración.");
    }

    private void handleHelpCommand(CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_HELP)) {
            sender.sendMessage(messageManager.getMessage("no_permission"));
            return;
        }

        sender.sendMessage(messageManager.getMessage("help_header"));

        for (CommandInfo cmd : registeredCommands) {
            if (sender.hasPermission(cmd.permission)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("command", cmd.usage);
                placeholders.put("description", cmd.description);
                sender.sendMessage(messageManager.getMessage("help_line", placeholders));
            }
        }
        sender.sendMessage(messageManager.getMessage("help_footer"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            for (CommandInfo cmd : registeredCommands) {
                if (sender.hasPermission(cmd.permission)) {
                    commands.add(cmd.name);
                }
            }
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("give")) {
                commands.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                StringUtil.copyPartialMatches(args[1], commands, completions);
            }
        } else if (args.length == 3) {
             if (args[0].equalsIgnoreCase("give")) {
                commands.addAll(Arrays.asList("1", "16", "32", "64"));
                StringUtil.copyPartialMatches(args[2], commands, completions);
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("give")) {
                // Sugerir niveles del 1 al máximo
                int maxLevel = LevelManager.getMaxLevel();
                for (int i = 1; i <= maxLevel; i++) {
                    commands.add(String.valueOf(i));
                }
                StringUtil.copyPartialMatches(args[3], commands, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
    
    private static class CommandInfo {
        String name;
        String description;
        String usage;
        String permission;
        
        public CommandInfo(String name, String description, String usage, String permission) {
            this.name = name;
            this.description = description;
            this.usage = usage;
            this.permission = permission;
        }
    }
}