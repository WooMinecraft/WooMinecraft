package com.plugish.woominecraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WooCommand implements CommandExecutor {

    public static WooMinecraft plugin = WooMinecraft.instance;
    private static final String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "WooMinecraft" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("woo") && args.length == 0) {
            if (sender.hasPermission("woo.admin") || sender.isOp()) {
                sender.sendMessage(chatPrefix + " " + plugin.getLang("general.avail_commands") + ": /woo check");
            } else {
                sender.sendMessage(chatPrefix + " " + plugin.getLang("general.not_authorized"));
            }
        } else if (command.getName().equalsIgnoreCase("woo") && args.length == 1) {
            if (args[0].equalsIgnoreCase("check")) {
                if (sender.hasPermission("woo.admin") || sender.isOp()) {

                    plugin.check().thenAccept((result) -> {
                        String msg;
                        if (!result) {
                            msg = chatPrefix + " " + plugin.getLang("general.none_avail");
                        } else {
                            msg = chatPrefix + " " + plugin.getLang("general.processed");
                        }

                        // We need to do it in sync
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> sender.sendMessage(msg));
                    });
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
}
