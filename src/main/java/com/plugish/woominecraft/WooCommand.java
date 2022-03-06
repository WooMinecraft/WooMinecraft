package com.plugish.woominecraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
        } else if (args.length <= 1 && !args[0].equalsIgnoreCase("ping")) {
            if (args[0].equalsIgnoreCase("check")) {
                checkSubcommand(sender);
            } else if (args[0].equalsIgnoreCase("debug")) {
                debugSubcommand(sender);
            } else if (args[0].equalsIgnoreCase("help")) {
                helpSubcommand(sender);
            }
            else {
                sender.sendMessage(chatPrefix + "Usage: /woo help");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("ping")) {
            try {
                if (!args[1].isEmpty()) {
                    pingSubcommand(sender, args.length, args[1]);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                pingSubcommand(sender, args.length,"");
            }
        }
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
                if( e.getMessage().contains( "Expected BEGIN_OBJECT but was STRING" ) ) {
                    sender.sendMessage( chatPrefix+ChatColor.RED+"REST endpoint is not accessible, check logs." );
                    return;
                }

                sender.sendMessage(chatPrefix+ChatColor.RED+e.getMessage() );
                e.printStackTrace();
            }
        });
    }

    /**
     * Pings the user's server they have set in the config.
     * @param sender Who sent the message.
     */
    private void pingSubcommand(CommandSender sender, int length, String Url) {
        if (!sender.hasPermission("woo.admin")) {
            String msg = chatPrefix + ChatColor.translateAlternateColorCodes('&', plugin.getLang("general.not_authorized"));
            sender.sendMessage(msg);
            return;
        }

        // Run check off the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String msg = "";
            // allow ability to ping any website to test outgoing connections from the mc server
            // example /woo ping https://www.google.com/
            if (length == 2) {
                try {
                    URL U = new URL(Url);
                    HttpURLConnection ping = (HttpURLConnection) U.openConnection();
                    ping.setConnectTimeout(1000);
                    ping.setReadTimeout(1000);
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
                }

            } else {
                try {
                    sender.sendMessage(chatPrefix + "Checking connection to server");
                    HttpURLConnection ping = (HttpURLConnection) new URL(plugin.getConfig().getString("url")).openConnection();
                    ping.setConnectTimeout(700);
                    ping.setReadTimeout(700);
                    ping.setRequestMethod("HEAD");
                    int Rc = ping.getResponseCode();
                    String rs = ping.getResponseMessage();
                    ping.disconnect();
                    if (Rc < 199) {
                        msg = chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 200 && Rc <= 299) {
                        msg = chatPrefix + ChatColor.GREEN + " Status: Good, " + Rc;
                    } else if (Rc >= 300 && Rc <= 399) {
                        msg = chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 400 && Rc <= 599) {
                        msg = chatPrefix + ChatColor.DARK_RED + " Status: Bad, " + Rc + " " + rs;
                    }
                    sender.sendMessage(msg);
                } catch (IOException e) {
                    // send feedback for the sender
                    WooMinecraft.instance.getLogger().severe(e.getMessage());
                    sender.sendMessage(chatPrefix + ChatColor.DARK_RED + "Server Status: Failed");
                    if (plugin.isDebug()) {
                        WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("key"));
                        WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("url"));
                    }
                }
                try {
                    sender.sendMessage(chatPrefix + "Checking Rest Api Url");
                    HttpURLConnection ping = (HttpURLConnection) WooMinecraft.instance.getSiteURL().openConnection();
                    ping.setConnectTimeout(700);
                    ping.setReadTimeout(700);
                    ping.setRequestMethod("HEAD");
                    int Rc = ping.getResponseCode();
                    String rs = ping.getResponseMessage();
                    ping.disconnect();
                    if (Rc < 199) {
                        msg = chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 200 && Rc <= 299) {
                        msg = chatPrefix + ChatColor.GREEN + " Status: Good, " + Rc;
                    } else if (Rc >= 300 && Rc <= 399) {
                        msg = chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 400 && Rc <= 599) {
                        msg = chatPrefix + ChatColor.DARK_RED + " Status: Bad, " + Rc + " " + rs;
                    }
                    sender.sendMessage(msg);
                } catch (IOException e) {
                    WooMinecraft.instance.getLogger().severe(e.getMessage());
                    sender.sendMessage(chatPrefix + ChatColor.DARK_RED + "Server Status: Failed");
                    if (plugin.isDebug()) {
                        WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("key"));
                        WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("url"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Sets the debug setting from a command.
     * @param sender Who sent the message.
     */
    private void debugSubcommand(CommandSender sender) {
        FileConfiguration pluginConfig = plugin.getConfig();

        if (pluginConfig.getBoolean("debug")) {
            pluginConfig.set("debug", false);
            sender.sendMessage(chatPrefix + "Set debug to: " + ChatColor.DARK_RED + "False");
            return;
        }

        pluginConfig.set("debug", true);
        sender.sendMessage(chatPrefix + "Set debug to: " + ChatColor.GREEN + "True");
    }

    /**
     * Shows the sender all the available commands.
     * @param sender Who sent the message.
     */
    private void helpSubcommand(CommandSender sender) {
        PluginDescriptionFile descriptionFile = plugin.getDescription();

        sender.sendMessage(chatPrefix +" Ver"+ descriptionFile.getVersion());
        sender.sendMessage(ChatColor.DARK_PURPLE + "By " + String.join( ",", descriptionFile.getAuthors() ) );
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo help" +ChatColor.WHITE+ " Shows this Helpsite");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo check" +ChatColor.WHITE+ " Check for donations/orders");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo ping" +ChatColor.WHITE+ " Test server connection");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/woo debug" +ChatColor.WHITE+ " Enable/disable debugging");
    }
}