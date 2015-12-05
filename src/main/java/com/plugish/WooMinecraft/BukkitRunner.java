package com.plugish.WooMinecraft;

import org.bukkit.scheduler.BukkitRunnable;

public class BukkitRunner extends BukkitRunnable {

	public static WooMinecraft plugin = WooMinecraft.instance;
	
	public BukkitRunner(WooMinecraft plugin){
		this.plugin = plugin;
	}
	
	public void run(){
		this.plugin.check();
	}

}
