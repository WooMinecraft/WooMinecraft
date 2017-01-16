package com.plugish.woominecraft.Util;

import com.plugish.woominecraft.WooMinecraft;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitRunner extends BukkitRunnable {

	public void run() {
		try {
			getPlugin().check();
		} catch ( Exception e ) {
			getPlugin().getLogger().warning( e.getMessage() );
			e.printStackTrace();
		}
	}

	private WooMinecraft getPlugin() {
		return WooMinecraft.getInstance();
	}

}
