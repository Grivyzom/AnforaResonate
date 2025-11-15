package gc.grivyzom.AnforaXP.commands;

import gc.grivyzom.AnforaXP.AnforaMain;
import gc.grivyzom.AnforaXP.utils.ItemFactory;
import gc.grivyzom.AnforaXP.utils.MessageManager;
import gc.grivyzom.AnforaXP.utils.Permissions; // Import the Permissions class
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

        // Registrar comandos aquí
        registeredCommands.add(new CommandInfo(
                "give",
                "Da una Anfora Resonante a un jugador.",
                "/anfora give <jugador> [cantidad]",
                Permissions.ADMIN_GIVE // Use the constant
        ));
        registeredCommands.add(new CommandInfo(
                "help",
                "Muestra la lista de comandos disponibles.",
                "/anfora help",
                Permissions.COMMAND_HELP // Use the constant
        ));
        registeredCommands.add(new CommandInfo(
                "list",
                "Muestra la ubicación de tus ánforas.",
                "/anfora list",
                Permissions.COMMAND_LIST
        ));
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

            case "list":
                handleListCommand(sender);
                break;
            default:
                sender.sendMessage(messageManager.getMessage("unknown_command"));
                break;
        }
        return true;
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
        List<gc.grivyzom.AnforaXP.data.AnforaData> anforas = plugin.getAnforaDataManager().getAnforasByOwner(player.getUniqueId());

        if (anforas.isEmpty()) {
            sender.sendMessage(messageManager.getMessage("list_no_anforas"));
            return;
        }

        sender.sendMessage(messageManager.getMessage("list_header"));
        int i = 1;
        for (gc.grivyzom.AnforaXP.data.AnforaData anfora : anforas) {
            if (anfora.getLocation() != null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("number", String.valueOf(i++));
                placeholders.put("world", anfora.getLocation().getWorld().getName());
                placeholders.put("x", String.valueOf(anfora.getLocation().getBlockX()));
                placeholders.put("y", String.valueOf(anfora.getLocation().getBlockY()));
                placeholders.put("z", String.valueOf(anfora.getLocation().getBlockZ()));
                placeholders.put("level", String.valueOf(anfora.getLevel()));
                placeholders.put("current_capacity", String.valueOf((int) anfora.getExperience()));
                placeholders.put("max_capacity", String.valueOf((int) anfora.getMaxExperience()));
                sender.sendMessage(messageManager.getMessage("list_item", placeholders));
            }
        }
        sender.sendMessage(messageManager.getMessage("list_footer"));
    }
    
    private void handleGiveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN_GIVE)) { // Use the constant
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
        
        ItemStack anforaItem = ItemFactory.createAnforaItem(plugin, amount);
        target.getInventory().addItem(anforaItem);
        
        Map<String, String> placeholdersSender = new HashMap<>();
        placeholdersSender.put("amount", String.valueOf(amount));
        placeholdersSender.put("player", target.getName());
        sender.sendMessage(messageManager.getMessage("give_success", placeholdersSender));
        
        Map<String, String> placeholdersTarget = new HashMap<>();
        placeholdersTarget.put("amount", String.valueOf(amount));
        target.sendMessage(messageManager.getMessage("receive_success", placeholdersTarget));
    }
    
    private void handleHelpCommand(CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_HELP)) { // Use the constant
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
            if (args[0].equalsIgnoreCase("give")) {
                commands.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                StringUtil.copyPartialMatches(args[1], commands, completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                commands.addAll(Arrays.asList("1", "16", "64"));
                StringUtil.copyPartialMatches(args[2], commands, completions);
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