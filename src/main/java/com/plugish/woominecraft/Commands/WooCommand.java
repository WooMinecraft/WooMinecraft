package com.plugish.woominecraft.Commands;

import java.util.UUID;

import com.plugish.woominecraft.WooMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.JSONException;

public class WooCommand implements CommandExecutor {

	public static WooMinecraft plugin = WooMinecraft.instance;
	public static String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "WooMinecraft" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( command.getName().equalsIgnoreCase( "woo" ) && args.length == 0 ) {
			if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {
				sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.avail_commands" ) + ": /woo [register , reload , check]" );
			} else {
				sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.not_authorized" ) );
			}
		} else if ( command.getName().equalsIgnoreCase( "woo" ) && args.length == 1 ) {
			if ( args[ 0 ].equalsIgnoreCase( "reload" ) ) {
				String msg = plugin.getLang( "general.reloaded" ).replace( "&", "\u00A7" );
				if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {
					plugin.reloadConfig();
					sender.sendMessage( msg );
				} else {
					msg = plugin.getLang( "general.not_authorized" ).replace( "&", "\u00A7" );
					sender.sendMessage( msg );
				}
			} else if ( args[ 0 ].equalsIgnoreCase( "register" ) ) {
				if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {
					if ( plugin.getConfig().getString( "key" ).equals( "" ) ) {
						UUID uuid = UUID.randomUUID();
						String key = uuid.toString().replaceAll( "-", "" );
						plugin.getConfig().set( "key", key );

						// TODO: Need to figure out a way to save config WITH comments.
						plugin.saveConfig();
						sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.saved_conf" ) );
						sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.key" ) + ": " + key );
						sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.cpy_key" ) );
					} else {
						sender.sendMessage( chatPrefix + " " + plugin.getLang( "general.key_set" ) );
					}
				} else {
					String msg = plugin.getLang( "general.not_authorized" ).replace( "&", "\u00A7" );
					sender.sendMessage( msg );
				}
			} else if ( args[ 0 ].equalsIgnoreCase( "check" ) ) {
				if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {

					try {
						String msg;
						boolean checkResults = plugin.check();

						if ( !checkResults ) {
							msg = chatPrefix + " " + plugin.getLang( "general.none_avail" );
						} else {
							msg = chatPrefix + " " + plugin.getLang( "general.processed" );
						}

						sender.sendMessage( msg );
					} catch ( Exception e ) {
						plugin.getLogger().warning( e.getMessage() );
						e.printStackTrace();
					}
				} else {
					String msg = plugin.getLang( "general.not_authorized" ).replace( "&", "\u00A7" );
					sender.sendMessage( msg );
				}
			}
		}
		return true;
	}
}
