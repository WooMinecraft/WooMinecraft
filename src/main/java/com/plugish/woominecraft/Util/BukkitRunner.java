package com.plugish.woominecraft.Util;

import com.plugish.woominecraft.WooMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;

public class BukkitRunner extends BukkitRunnable {

	public final WooMinecraft plugin;

	public BukkitRunner( WooMinecraft plugin ) {
		this.plugin = plugin;
	}

	public void run() {
//		plugin.getServer().broadcastMessage( ChatColor.RED + "Task Ran" );
		try {
			plugin.check();
		} catch ( JSONException e ) {
			WooMinecraft.log.warning( e.getMessage() );
		}
	}

}
