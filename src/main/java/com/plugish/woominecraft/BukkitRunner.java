package com.plugish.woominecraft;

import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;

public class BukkitRunner extends BukkitRunnable {

	public static WooMinecraft plugin = WooMinecraft.instance;
	
	public void run(){
		try {
			plugin.check();
		} catch( JSONException e ) {
			WooMinecraft.log.severe( e.getMessage() );
		}
	}

}
