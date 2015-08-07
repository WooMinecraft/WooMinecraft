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
                s.sendMessage("[Woo] You have used the register command!");
                if(s.hasPermission("woo.admin") || s.isOp()) {
                    s.sendMessage("[Woo] You Have Permission to use this command");
                    SecureRandom random = new SecureRandom();
                    s.sendMessage("[Woo] You created (random)");
                    byte[] randomBytes = new byte[32];
                    s.sendMessage("[Woo] You created 32 randomBytes!");
                    random.nextBytes(randomBytes);
                    s.sendMessage("[Woo] You got this far!");
                    String key = "";
                    s.sendMessage("[Woo] Created key string == nothing");
                    if (plugin.c.getString(plugin.urlPath + ".key") == "") {
                        s.sendMessage("[Woo] Getting empty config path");
                        key = String.valueOf(random);
                        s.sendMessage("[Woo] set key = to randomBytes");
                        plugin.c.set(plugin.urlPath + ".key", key);
                        s.sendMessage("[Woo] Almost There!");
                        plugin.saveConfig();
                        s.sendMessage("[Woo] Saved Config!");
                        s.sendMessage(ChatColor.AQUA + "[WOO]" + ChatColor.RED + " KEY: " + key);
                        s.sendMessage(ChatColor.RED + "Copy this key and put it in your WooMinecraft options panel in WordPress");
                    } else {
                        key = plugin.c.getString(plugin.urlPath + ".key");
                        s.sendMessage("[Woo] key already set");
                    }
                } else {
                    s.sendMessage("You Don't Have Permission for that Command!");
                }
            } else if(args[0].equalsIgnoreCase("check")) {
                if(s.hasPermission("woo.admin") || s.isOp()) {
                    // well we need to run the check then don't we?
                    plugin.check();
                    s.sendMessage("[Woo] Checked Purchases!");
                } else {
                    s.sendMessage("You Don't Have Permission for that Command!");
                }
            }
        }
        return true;
    }
}
