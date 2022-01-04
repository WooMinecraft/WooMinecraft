package com.plugish.woominecraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
        subCommands.put("help", "woo.admin");
        subCommands.put("check", "woo.admin");
        subCommands.put("ping", "woo.admin");
        subCommands.put("debug", "woo.admin");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("woo.admin")) {
                sender.sendMessage(chatPrefix + plugin.getLang("general.avail_commands") + ": /woo help");
            } else {
                sender.sendMessage(chatPrefix + plugin.getLang("general.not_authorized"));
            }
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("check")) {
                checkSubcommand(sender);
            } else if (args[0].equalsIgnoreCase("ping")) {
                pingSubcommand(sender);
            } else if (args[0].equalsIgnoreCase("debug")) {
                debugSubcommand(sender);
            } else if (args[0].equalsIgnoreCase("help")) {
                helpSubcommand(sender);
            }
            else {
                sender.sendMessage(chatPrefix + "Usage: /woo help");
            }
            return true;
        }
        sender.sendMessage(chatPrefix +"Usage: /woo help");
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
    private void pingSubcommand(CommandSender sender) {
        if (!sender.hasPermission("woo.admin")) {
            String msg = chatPrefix + ChatColor.translateAlternateColorCodes('&', plugin.getLang("general.not_authorized"));
            sender.sendMessage(msg);
            return;
        }
        // Run check off the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String msg = chatPrefix+" ";
                sender.sendMessage(chatPrefix+"Checking connection to server");
                HttpURLConnection ping = (HttpURLConnection) new URL(plugin.getConfig().getString("url")).openConnection();
                ping.setConnectTimeout(700);
                ping.setReadTimeout(700);
                ping.setRequestMethod("HEAD");
                int Rc = ping.getResponseCode();
                String rs = ping.getResponseMessage();
                ping.disconnect();
                if (Rc < 199) {
                    msg = chatPrefix+ChatColor.YELLOW+" Status: Ok, but possible issues, "+ Rc+" "+rs;
                } else if (Rc >= 200 && Rc <= 299) {
                    msg = chatPrefix+ChatColor.GREEN+" Status: Good, "+ Rc;
                } else if (Rc >=300 && Rc <= 399) {
                    msg = chatPrefix+ChatColor.YELLOW+" Status: Ok, but possible issues, "+ Rc+" "+rs;
                } else if ( Rc >= 400 && Rc <=599) {
                    msg = chatPrefix+ChatColor.DARK_RED+" Status: Bad, "+ Rc+" "+rs;
                }
                sender.sendMessage(msg);
            } catch (IOException e) {
                // send feedback for the sender
                WooMinecraft.instance.getLogger().severe(e.getMessage());
                sender.sendMessage(chatPrefix +ChatColor.DARK_RED+"Server Status: Failed");
                if (plugin.isDebug()) {
                    //Do not output store keys to console, can open up potential issues
                    WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("url"));
                }
            }
            try {
                String msg = chatPrefix+" ";
                sender.sendMessage(chatPrefix + "Checking Rest Api Url");
                HttpURLConnection ping = (HttpURLConnection) new URL(plugin.getConfig().getString("url")+"/index.php?rest_route=/wmc/v1/server/"+ plugin.getConfig().getString("key")).openConnection();
                ping.setConnectTimeout(700);
                ping.setReadTimeout(700);
                ping.setRequestMethod("HEAD");
                int Rc = ping.getResponseCode();
                String rs = ping.getResponseMessage();
                ping.disconnect();
                if (Rc < 199) {
                    msg = chatPrefix+ChatColor.YELLOW+" Status: Ok, but possible issues, "+ Rc+" "+rs;
                } else if (Rc >= 200 && Rc <= 299) {
                    msg = chatPrefix+ChatColor.GREEN+" Status: Good, "+ Rc;
                } else if (Rc >=300 && Rc <= 399) {
                    msg = chatPrefix+ChatColor.YELLOW+" Status: Ok, but possible issues, "+ Rc+" "+rs;
                } else if ( Rc >= 400 && Rc <=599) {
                    msg = chatPrefix+ChatColor.DARK_RED+" Status: Bad, "+ Rc+" "+rs;
                }
                sender.sendMessage(msg);
            } catch (IOException e) {
                WooMinecraft.instance.getLogger().severe(e.getMessage());
                sender.sendMessage(chatPrefix +ChatColor.DARK_RED+"Server Status: Failed");
                if (plugin.isDebug()) {
                    //Do not output store keys to console, can open up potential issues
                    WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("url"));
                }
            }
        });
    }
    private void debugSubcommand(CommandSender sender) {
        if (!sender.hasPermission("woo.admin")) {
            String msg = chatPrefix + ChatColor.translateAlternateColorCodes('&', plugin.getLang("general.not_authorized"));
            sender.sendMessage(msg);
            return;
        }
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getConfig().set("debug", false);
            sender.sendMessage(chatPrefix + "Debug "+ChatColor.DARK_RED+"False");
        } else {
            plugin.getConfig().set("debug", true);
            sender.sendMessage(chatPrefix + "Debug "+ChatColor.GREEN+"True");
        }
    }
    private void helpSubcommand(CommandSender sender) {
        String auth = "";
        for (int i = 0;i < plugin.getDescription().getAuthors().size()-1; i++) {
            auth = auth+plugin.getDescription().getAuthors().get(i)+", ";
        }
        auth = auth+plugin.getDescription().getAuthors().get(plugin.getDescription().getAuthors().size()-1);
        sender.sendMessage(chatPrefix +" Ver"+ plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.DARK_PURPLE + "By " + auth);
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo help" +ChatColor.WHITE+ " Shows this Helpsite");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo check" +ChatColor.WHITE+ " Check for donations/orders");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo ping" +ChatColor.WHITE+ " Test server connection");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo debug" +ChatColor.WHITE+ " Enable/disable debugging");
    }
}
