package com.plugish.woominecraft;

import com.plugish.woominecraft.WooMinecraft;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitRunner extends BukkitRunnable {

	public final WooMinecraft plugin;

	public BukkitRunner( WooMinecraft plugin ) {
		this.plugin = plugin;
	}

	public void run() {
		try {
			plugin.check();
		} catch ( Exception e ) {
			plugin.getLogger().warning( e.getMessage() );
			e.printStackTrace();
		}
	}

}
