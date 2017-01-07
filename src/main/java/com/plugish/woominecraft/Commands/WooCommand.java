package com.plugish.woominecraft.Commands;

import com.plugish.woominecraft.WooMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WooCommand implements CommandExecutor {

	private String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "WooMinecraft" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( command.getName().equalsIgnoreCase( "woo" ) && args.length == 0 ) {
			if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {
				sender.sendMessage( getChatPrefix() + " " + getPlugin().getMessage( "general.avail_commands" ) + ": /woo [register , reload , check]" );
			} else {
				sender.sendMessage( getChatPrefix() + " " + getPlugin().getMessage( "general.not_authorized" ) );
			}
		} else if ( command.getName().equalsIgnoreCase( "woo" ) && args.length == 1 ) {
			if ( args[ 0 ].equalsIgnoreCase( "check" ) ) {
				if ( sender.hasPermission( "woo.admin" ) || sender.isOp() ) {

					try {
						String msg;
						boolean checkResults = getPlugin().check();

						if ( !checkResults ) {
							msg = getChatPrefix() + " " + getPlugin().getMessage( "general.none_avail" );
						} else {
							msg = getChatPrefix() + " " + getPlugin().getMessage( "general.processed" );
						}

						sender.sendMessage( msg );
					} catch ( Exception e ) {
						getPlugin().getLogger().warning( e.getMessage() );
						e.printStackTrace();
					}
				} else {
					String msg = getPlugin().getMessage( "general.not_authorized" ).replace( "&", "\u00A7" );
					sender.sendMessage( msg );
				}
			} else {
				sender.sendMessage( "Usage: /woo check" );
			}
		}
		return true;
	}

	private String getChatPrefix() {
		return chatPrefix;
	}

	private WooMinecraft getPlugin() {
		return WooMinecraft.getInstance();
	}
}
