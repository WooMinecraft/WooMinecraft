package com.plugish.woominecraft;

import com.sun.tools.jdeprscan.scan.Scan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WooCommand implements CommandExecutor {

	public static WooMinecraft plugin = WooMinecraft.instance;
	private static String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "WooMinecraft" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( command.getName().equalsIgnoreCase( "woo" ) && args.length == 0 ) {
			if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {
				sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.avail_commands" ) + ": /woo check" );
			} else {
				sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.not_authorized" ) );
			}
		} else if ( command.getName().equalsIgnoreCase( "woo" ) && args.length > 0 ) {
			//fixed some wonky perm issues
			if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {
				if ( args[ 0 ].equalsIgnoreCase( "check" ) ) {
					//force server to run url checks in async/not in main thread
					Bukkit.getScheduler().runTaskAsynchronously(WooMinecraft.instance, () -> {
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
					});

				//added support for on the fly debug enable/disable
			} else if ( args[ 0 ].equalsIgnoreCase( "debug" ) ) {
				if (plugin.getConfig().getBoolean("debug")) {
					plugin.getConfig().set("debug", false);
					sender.sendMessage(chatPrefix + "Debug Disabled");
					return true;
				} else {
					plugin.getConfig().set("debug", true);
					sender.sendMessage(chatPrefix + "Debug enabled");
				}
			}else if ( args[ 0 ].equalsIgnoreCase( "ping" ) ) {
				Bukkit.getScheduler().runTaskAsynchronously(WooMinecraft.instance, () -> {
					try {
						sender.sendMessage(chatPrefix+"Checking connection to server");
						HttpURLConnection ping = (HttpURLConnection) new URL(plugin.getConfig().getString("url")).openConnection();
						ping.setConnectTimeout(700);
						ping.setReadTimeout(700);
						ping.setRequestMethod("HEAD");
						int Rc = ping.getResponseCode();
						String rs = ping.getResponseMessage();
						ping.disconnect();
						sender.sendMessage(chatPrefix + "Server response: " + Rc + " "+ rs);
					} catch (IOException e) {
						WooMinecraft.instance.getLogger().severe(e.getMessage());
						sender.sendMessage(chatPrefix + "Server response: Failed");
						if (plugin.isDebug()) {
							WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("key"));
							WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("url"));
						}
					}
					try {
						sender.sendMessage(chatPrefix + "Checking Rest Api Url");
						HttpURLConnection ping = (HttpURLConnection) new URL(plugin.getConfig().getString("url")+"/index.php?rest_route=/wmc/v1/server/"+ plugin.getConfig().getString("key")).openConnection();
						ping.setConnectTimeout(700);
						ping.setReadTimeout(700);
						ping.setRequestMethod("HEAD");
						int Rc = ping.getResponseCode();
						String rs = ping.getResponseMessage();
						ping.disconnect();
						sender.sendMessage(chatPrefix + "Server response: " + Rc + " "+ rs);
					} catch (IOException e) {
						WooMinecraft.instance.getLogger().severe(e.getMessage());
						sender.sendMessage(chatPrefix + "Server response: Failed");
						if (plugin.isDebug()) {
							WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("key"));
							WooMinecraft.instance.getLogger().info(plugin.getConfig().getString("url"));
						}
					}
					});
				}
			} else {
				String msg = plugin.getLang( "general.not_authorized" ).replace( "&", "\u00A7" );
				sender.sendMessage( msg );
			}
		} else {
			sender.sendMessage( "Usage: /woo check" );
		}
		return true;
	}
}
