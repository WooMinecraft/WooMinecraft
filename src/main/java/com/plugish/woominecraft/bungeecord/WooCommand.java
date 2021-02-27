package com.plugish.woominecraft.bungeecord;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class WooCommand extends Command {

	public static WooMinecraft plugin = WooMinecraft.instance;
	private static String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.WHITE + "WooMinecraft" + ChatColor.DARK_PURPLE + "] " + ChatColor.DARK_PURPLE + "";

	public WooCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}


	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(strings.length == 1 && strings[0].equals("reload")){
			plugin.reloadConfig();
			commandSender.sendMessage( new TextComponent(chatPrefix + " " + plugin.getLang( "general.reloaded" )) );
			return;
		}

		try {
			BaseComponent msg;
			boolean checkResults = plugin.check();

			if ( !checkResults ) {
				msg = new TextComponent(chatPrefix + " " + plugin.getLang( "general.none_avail" ));
			} else {
				msg = new TextComponent(chatPrefix + " " + plugin.getLang( "general.processed" ));
			}

			commandSender.sendMessage( msg );
		} catch ( Exception e ) {
			plugin.getLogger().warning( e.getMessage() );
			e.printStackTrace();
		}
	}
}
