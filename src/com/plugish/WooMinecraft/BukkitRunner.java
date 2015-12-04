package com.plugish.WooMinecraft;

import org.bukkit.scheduler.BukkitRunnable;

public class BukkitRunner extends BukkitRunnable {

	public static WooMinecraft plugin = WooMinecraft.instance;
	
	public BukkitRunner(WooMinecraft plugin){
		BukkitRunner.plugin = plugin;
	}
	
	public void run(){
		BukkitRunner.plugin.check();
	}

}
