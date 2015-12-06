package com.plugish.WooMinecraft.Commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.JSONException;

import com.plugish.WooMinecraft.WooMinecraft;

public class WooCommand implements CommandExecutor {

    public static WooMinecraft plugin = WooMinecraft.instance;
    public static String Theme = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "Woo" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("woo") && args.length == 0) {
            if (s.hasPermission("woo.admin") || s.isOp()) {
                s.sendMessage( Theme + " Available Commands: /woo [register , reload , check]");
            } else {
                s.sendMessage(Theme + " You Don't Have Permission for that Command!");
            }
        } else if(command.getName().equalsIgnoreCase("woo") && args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                if(s.hasPermission("woo.admin") || s.isOp()) {
                    plugin.reloadConfig();
                    s.sendMessage(Theme + " Config Reloaded");
                    String msg = plugin.english.getString("Reloaded").replace("&", "\u00A7");
                    s.sendMessage(msg);
                } else {
                    String msg = plugin.english.getString("NoPerms").replace("&", "\u00A7");
                    s.sendMessage(msg);
                }
            } else if(args[0].equalsIgnoreCase("register")) {
                if(s.hasPermission("woo.admin") || s.isOp()) {
                    /*
                    * Creating a random UUID (Universally unique identifier).
                    */
                    UUID uuid = UUID.randomUUID();
                    String key = "";
                    if ( plugin.config.getString( WooMinecraft.urlPath + ".key") == "changeme" ) {
                        key = uuid.toString().replaceAll("-", "");
                        plugin.config.set( WooMinecraft.urlPath + ".key", key);
                        plugin.saveConfig();
                        s.sendMessage(Theme + " Saved Config!");
                        s.sendMessage(Theme + " Key: " + key);
                        s.sendMessage(Theme + " Copy this key and put it in your WooMinecraft options panel in WordPress");
                    } else {
                        key = plugin.config.getString( WooMinecraft.urlPath + ".key");
                        s.sendMessage(Theme + " key already set!");
                    }
                } else {
                    String msg = plugin.english.getString("NoPerms").replace("&", "\u00A7");
                    s.sendMessage(msg);
                }
            } else if(args[0].equalsIgnoreCase("check")) {
                if(s.hasPermission("woo.admin") || s.isOp()) {
                	
                	boolean checkResults = false;
                	String msg = "";
                	
                	try {
                		checkResults = plugin.check();
                	} catch( JSONException e ) {
                		WooMinecraft.log.severe( e.getMessage() );
                	}
                	
                	if ( false == checkResults ) {
                		msg = Theme + " No purchases available.";
                	} else {
                		msg = Theme + " No purchases available.";
                	}
                	
                    s.sendMessage( msg );
                } else {
                    String msg = plugin.english.getString("NoPerms").replace("&", "\u00A7");
                    s.sendMessage(msg);
                }
            }
        }
        return true;
    }
}
