package com.plugish.woominecraft;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.JSONException;

public class WooCommand implements CommandExecutor {

	public static WooMinecraft plugin = WooMinecraft.instance;
	public static String Theme = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "Woo" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( command.getName().equalsIgnoreCase( "woo" ) && args.length == 0 ) {
			if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {
				sender.sendMessage( Theme + " " + plugin.getLang( "general.avail_commands" ) + ": /woo [register , reload , check]" );
			} else {
				sender.sendMessage( Theme + " " + plugin.getLang( "general.not_authorized" ) );
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
					/*
                    * Creating a random UUID (Universally unique identifier).
                    */
					UUID uuid = UUID.randomUUID();
					String key;
					if ( plugin.config.getString( "key" ).equals( "changeme" ) ) {
						key = uuid.toString().replaceAll( "-", "" );
						plugin.config.set( "key", key );
						plugin.saveConfig();
						sender.sendMessage( Theme + " " + plugin.getLang( "general.saved_conf" ) );
						sender.sendMessage( Theme + " " + plugin.getLang( "general.key" ) + ": " + key );
						sender.sendMessage( Theme + " " + plugin.getLang( "general.cpy_key" ) );
					} else {
						sender.sendMessage( Theme + " " + plugin.getLang( "general.key_set" ) );
					}
				} else {
					String msg = plugin.getLang( "general.not_authorized" ).replace( "&", "\u00A7" );
					sender.sendMessage( msg );
				}
			} else if ( args[ 0 ].equalsIgnoreCase( "check" ) ) {
				if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {

					boolean checkResults = false;
					String msg = "";

					try {
						checkResults = plugin.check();
					} catch ( JSONException e ) {
						WooMinecraft.log.warning( e.getMessage() );
					}

					if ( !checkResults ) {
						msg = Theme + " " + plugin.getLang( "general.none_avail" );
					} else {
						msg = Theme + " " + plugin.getLang( "general.processed" );
					}

					sender.sendMessage( msg );
				} else {
					String msg = plugin.getLang( "general.not_authorized" ).replace( "&", "\u00A7" );
					sender.sendMessage( msg );
				}
			}
		}
		return true;
	}
}
