package com.plugish.WooMinecraft.Commands;

import com.plugish.WooMinecraft.WooMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.security.SecureRandom;

public class WooCommand implements CommandExecutor {

    public static WooMinecraft plugin = WooMinecraft.instance;

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("woo") && args.length == 0) {
            if (s.hasPermission("woo.admin") || s.isOp()) {
                s.sendMessage("Available Commands: /woo [register , reload , check]");
            } else {
                s.sendMessage("You Don't Have Permission for that Command!");
            }
        } else if(command.getName().equalsIgnoreCase("woo") && args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                if(s.hasPermission("woo.admin") || s.isOp()) {
                    plugin.reloadConfig();
                    s.sendMessage("[Woo] Config Reloaded");
                } else {
                    s.sendMessage("You Don't Have Permission for that Command!");
                }
            } else if(args[0].equalsIgnoreCase("register")) {
                if(s.hasPermission("woo.admin") || s.isOp()) {
                    SecureRandom random = new SecureRandom();
                    byte[] randomBytes = new byte[32];
                    random.nextBytes(randomBytes);

                    String key = "";
                    if (plugin.c.getString(plugin.urlPath + ".key") == "") {
                        key = String.valueOf(randomBytes);
                        plugin.c.set(plugin.urlPath + ".key", key);
                        plugin.saveConfig();
                        s.sendMessage(ChatColor.AQUA + "[WOO]" + ChatColor.RED + " KEY: " + key);
                        s.sendMessage(ChatColor.RED + "Copy this key and put it in your WooMinecraft options panel in WordPress");
                    } else {
                        key = plugin.c.getString(plugin.urlPath + ".key");
                    }
                } else {
                    s.sendMessage("You Don't Have Permission for that Command!");
                }
            } else if(args[0].equalsIgnoreCase("check")) {
                if(s.hasPermission("woo.admin") || s.isOp()) {
                    // well we need to run the check then don't we?
                    plugin.check();
                } else {
                    s.sendMessage("You Don't Have Permission for that Command!");
                }
            }
        }
        return true;
    }
}
