package com.plugish.woominecraft;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitRunner extends BukkitRunnable {

	public final WooMinecraft plugin;

	BukkitRunner(WooMinecraft plugin) {
		this.plugin = plugin;
	}

	public void run() {
		//force running in async// not main thread
		Bukkit.getScheduler().runTaskAsynchronously(WooMinecraft.instance, () -> {
			try {
				plugin.check();
			} catch (Exception e) {
				plugin.getLogger().warning(e.getMessage());
				e.printStackTrace();
			}
		});
	}

}
