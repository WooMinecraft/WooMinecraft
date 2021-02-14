package com.plugish.woominecraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WooCommand implements TabExecutor {

    public WooMinecraft plugin = WooMinecraft.instance;
    private final String chatPrefix = ChatColor.translateAlternateColorCodes('&', "&5[&fWooMinecraft&5] ");

    // Hashmap for storing subcommand names and permissions for them
    private final HashMap<String, String> subCommands = new HashMap<>();

    public WooCommand() {
        // add future subcommands here for automatic tab completion
        subCommands.put("check", "woo.admin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("woo.admin")) {
                sender.sendMessage(chatPrefix + plugin.getLang("general.avail_commands") + ": /woo check");
            } else {
                sender.sendMessage(chatPrefix + plugin.getLang("general.not_authorized"));
            }
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("check")) {
                checkSubcommand(sender);
            } else {
                sender.sendMessage(chatPrefix + "Usage: /woo check");
            }
            return true;
        }
        sender.sendMessage(chatPrefix +"Usage: /woo check");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> completions = new ArrayList<>();

        // only care about first argument
        if (args.length != 1)
            return null;

        for (Map.Entry<String, String> subCommand : subCommands.entrySet()) {
            if (subCommand.getKey().startsWith(args[0]) && sender.hasPermission(subCommand.getValue()))
                completions.add(subCommand.getKey());
        }
        return completions;
    }

    private void checkSubcommand(CommandSender sender) {
        if (!sender.hasPermission("woo.admin")) {
            String msg = chatPrefix + ChatColor.translateAlternateColorCodes('&', plugin.getLang("general.not_authorized"));
            sender.sendMessage(msg);
            return;
        }
        // Run check off the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String msg = chatPrefix+" ";
                if (plugin.check()) {
                    msg = msg + plugin.getLang("general.processed");
                } else {
                    msg = msg + plugin.getLang("general.none_avail");
                }
                sender.sendMessage(msg);
            } catch (Exception e) {
                // send feedback for the sender
                sender.sendMessage(chatPrefix+ChatColor.RED+e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
