/*
 * Woo Minecraft Donation plugin
 * Author:	   Jerry Wood
 * Author URI: http://plugish.com
 * License:	   GPLv2
 * 
 * Copyright 2014 All rights Reserved
 * 
 */
package com.plugish.WooMinecraft;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class WooMinecraft extends JavaPlugin {

	public static Logger log;
	public static WooMinecraft instance;
	public static String configPath = "WooMinecraft";
	public static String urlPath = configPath+".web";
	
	public File englishFile;
	public FileConfiguration english;
	
	public File configFile;
	public FileConfiguration config;
	public static BukkitRunner runnerNew;
	
	// TODO -logic- Is this even needed?
	public static FileConfiguration c;

	public void initalizePlugin() {
		configFile = new File(getDataFolder(), "config.yml");
		
		// TODO -filechanges- Load lang file from a /lang/ folder based on language preference in main config
		englishFile = new File(getDataFolder(), "english.yml");
		config = new YamlConfiguration();
		english = new YamlConfiguration();
		WooDefaults.initDefaults();
		WooDefaults.loadYamls();
		
		// TODO -i18n- localize this string - excluding any [Woo] prefix
		log.info("[Woo] Initialized Config and Messages System.");
	}
	
	@Override
	public void onEnable(){
		log = getLogger();
		instance = this;
		// TODO -i18n- localize this string - excluding any [Woo] prefix
		log.info("[Woo] Initializing Config and Messages System.");
		
		initalizePlugin();
		// TODO -i18n- localize this string - excluding any [Woo] prefix
		log.info("[Woo] Initializing Commands");
		
		initCommands();
		// TODO -i18n- localize this string - excluding any [Woo] prefix
		log.info("[Woo] Commands Initialized");
		
		runnerNew = new BukkitRunner();
		runnerNew.runTaskTimerAsynchronously(instance, c.getInt(urlPath+".time_delay") * 20, c.getInt(urlPath+".time_delay") * 20);

		// TODO -i18n- localize this string - excluding any [Woo] prefix
		log.info("[Woo] Donation System Enabled!");
	}
	
	@Override
	public void onDisable(){
		saveConfig();

		// TODO -i18n- localize this string - excluding any [Woo] prefix
		log.info("[Woo] Donation System Disabled!");
	}
	
	/**
	 * Generates a comma delimited list of player names
	 * 
	 * @return String
	 */
	public String getPlayerList() {
		// Build post data based on player list
		StringBuilder sb = new StringBuilder();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			sb.append(player.getName() + ", ");
		}
		String playerList = sb.toString();
		
		// Remove the last and final comma
		Pattern pattern = Pattern.compile(", $");
		Matcher matcher = pattern.matcher(playerList);
		
		return matcher.replaceAll("");
	}

	/**
	 * Checks all online players against the
	 * webiste's database looking for pending donation deliveries
	 * 
	 * @return boolean
	 * @throws JSONException 
	 */
	public boolean check() throws JSONException {
		
		String namesResults = "";
		JSONObject json = null;
	
		String key = config.getString( "WooMinecraft.web.key" );
		String url = config.getString( "WooMinecraft.web.url" );

		// Check for player counts first
		Collection<? extends Player> list = Bukkit.getOnlinePlayers();
		
		// Must match main object method.
		Connection connection = new Connection( url, key );
		
		if (list.size() < 1) return false;
		
		ArrayList<Integer> rowUpdates = new ArrayList<Integer>();
		String playerlist = getPlayerList();
		
		try {
			namesResults = connection.getPlayerResults( playerlist );
		} catch( IOException e ) {
			log.severe( e.getMessage() );
		}
		
		// If the server says there are no results for the sent names
		// just return, no need to continue.
		if ( "" == namesResults ) {
			return false;
		}
		
		try {
			json = new JSONObject( namesResults );
		} catch( JSONException e ) {
			log.severe( e.getMessage() );
		}
		
		// Must have json data to continue.
		if ( null == json ) {
			return false;
		}

		if ( json.getString("status").equalsIgnoreCase("success") ) {
			JSONArray jsonArr = json.getJSONArray("data");
			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject obj = jsonArr.getJSONObject(i);

				String playerName = obj.getString("player_name");
				String x = obj.getString("command");
				final String command = x.replace("%s", playerName);
				
				// @TODO: Update to getUUID()
				Player pN = Bukkit.getServer().getPlayer(playerName);

				if (x.substring(0, 3) == "give") {
					int count = 0;
					for (ItemStack iN : pN.getInventory()) {
						if (iN == null)
							count++;
					}

					if (count == 0) return false;
				}

				int id = obj.getInt("id");

				BukkitScheduler sch = Bukkit.getServer().getScheduler();
				sch.scheduleSyncDelayedTask(instance, new Runnable() {
					@Override
					public void run() {
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
					}
				}, 20L);
				rowUpdates.add(id);
			}
		} else {
			log.info("Check: No donations for online users. STATUS: " + json.getString("status"));
			if (json.has("debug_info")) {
				log.info(json.getString("debug_info"));
			}
		}
		remove(rowUpdates);
		
		return false;
	}

	/**
	 * Removes IDs from 
	 * @param ids
	 */
	private void remove(ArrayList<Integer> ids) {
		if (ids.isEmpty()) return;

		try {
			String sPath = c.getString(urlPath + ".url");
			String key = c.getString(urlPath + ".key");

			URL url = new URL(sPath + "?woo_minecraft=update&key=" + key);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			String urlParams = StringUtils.join(ids, ',');
			con.setDoInput(true);
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes("players=" + urlParams);
			wr.flush();
			wr.close();

			BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response = input.readLine();
			if (!response.equalsIgnoreCase("true")) {
				// TODO -i18n- localize this string
				log.severe("Could not update donations.");
				log.info(response);
			} else {
				// TODO -i18n- localize this string
				log.info("Donations updated");
			}
			input.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize Commands
	 */
	public void initCommands() {
		getCommand("woo").setExecutor(new WooCommand());
	}
	
	/**
	 * A helper function to safely stop the server in the event
	 * something went wrong in the initial setup.
	 */
	public static void stopServer() {
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		Bukkit.dispatchCommand( console, "save-all" );
		Bukkit.dispatchCommand(console, "stop" );
	}
}
