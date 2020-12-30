package com.plugish.woominecraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import java.util.ArrayList;
import java.util.List;

public class WooCommand implements TabExecutor {

    public static WooMinecraft plugin = WooMinecraft.instance;
    private static final String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "WooMinecraft" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("woo.admin") || sender.isOp()) {
                sender.sendMessage(chatPrefix + " " + plugin.getLang("general.avail_commands") + ": /woo check");
            } else {
                sender.sendMessage(chatPrefix + " " + plugin.getLang("general.not_authorized"));
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("check")) {
                if (sender.hasPermission("woo.admin") || sender.isOp()) {

                    try {
                        String msg;
                        boolean checkResults = plugin.check();

                        if (!checkResults) {
                            msg = chatPrefix + " " + plugin.getLang("general.none_avail");
                        } else {
                            msg = chatPrefix + " " + plugin.getLang("general.processed");
                        }

                        sender.sendMessage(msg);
                    } catch (Exception e) {
                        plugin.getLogger().warning(e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    String msg = plugin.getLang("general.not_authorized").replace("&", "\u00A7");
                    sender.sendMessage(msg);
                }
            } else {
                sender.sendMessage("Usage: /woo check");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1 && "check".startsWith(strings[0])) {
            List<String> completions = new ArrayList<>();
            if (commandSender.hasPermission("woo.admin") || commandSender.isOp()) {
                completions.add("check");
            }
            return completions;
        }
        return null;
    }
}
