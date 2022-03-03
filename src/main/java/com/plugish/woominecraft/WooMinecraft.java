/*
 * Woo Minecraft Donation plugin
 * Author:	   Jerry Wood
 * Author URI: http://plugish.com
 * License:	   GPLv2
 * 
 * Copyright 2014 All rights Reserved
 * 
 */
package com.plugish.woominecraft;

import com.google.common.io.ByteSink;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plugish.woominecraft.pojo.Order;
import com.plugish.woominecraft.pojo.WMCPojo;
import com.plugish.woominecraft.pojo.WMCProcessedOrders;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class WooMinecraft extends JavaPlugin {

	static WooMinecraft instance;

	private YamlConfiguration l10n;

	public static final String NL = System.getProperty("line.separator");

	/**
	 * Stores the player data to prevent double checks.
	 *
	 * i.e. name:uuid:true|false
	 */
	private List<String> PlayersMap = new ArrayList<>();

	@Override
	public void onEnable() {
		instance = this;

		if (
			!Bukkit.getOnlineMode() &&
			!Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")
		) {
			getLogger().severe(String.valueOf(Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")));
			getLogger().severe("WooMinecraft doesn't support offLine mode");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		YamlConfiguration config = (YamlConfiguration) getConfig();
		// Save the default config.yml
		try{
			saveDefaultConfig();
		} catch ( IllegalArgumentException e ) {
			getLogger().warning( e.getMessage() );
		}

		String lang = getConfig().getString("lang");
		if ( lang == null ) {
			getLogger().warning( "No default l10n set, setting to english." );
		}

		// Load the commands.
		getCommand( "woo" ).setExecutor( new WooCommand() );

		// Log when plugin is initialized.
		getLogger().info( this.getLang( "log.com_init" ));

		// Setup the scheduler
		BukkitRunner scheduler = new BukkitRunner(instance);
		scheduler.runTaskTimerAsynchronously( instance, config.getInt( "update_interval" ) * 20, config.getInt( "update_interval" ) * 20 );

		// Log when plugin is fully enabled ( setup complete ).
		getLogger().info( this.getLang( "log.enabled" ) );
	}

	@Override
	public void onDisable() {
		// Log when the plugin is fully shut down.
		getLogger().info( this.getLang( "log.com_init" ) );
	}

	/**
	 * Helper method to get localized strings
	 *
	 * Much better than typing this.l10n.getString...
	 * @param path Path to the config var
	 * @return String
	 */
	String getLang(String path) {
		if ( null == this.l10n ) {

			LangSetup lang = new LangSetup( instance );
			l10n = lang.loadConfig();
		}

		return this.l10n.getString( path );
	}

	/**
	 * Validates the basics needed in the config.yml file.
	 *
	 * Multiple reports of user configs not having keys etc... so this will ensure they know of this
	 * and will not allow checks to continue if the required data isn't set in the config.
	 *
	 * @throws Exception Reason for failing to validate the config.
	 */
	private void validateConfig() throws Exception {

		if ( 1 > this.getConfig().getString( "url" ).length() ) {
			throw new Exception( "Server URL is empty, check config." );
		} else if ( this.getConfig().getString( "url" ).equals( "http://playground.dev" ) ) {
			throw new Exception( "URL is still the default URL, check config." );
		} else if ( 1 > this.getConfig().getString( "key" ).length() ) {
			throw new Exception( "Server Key is empty, this is insecure, check config." );
		}
	}

	/**
	 * Gets the site URL
	 *
	 * @return URL
	 * @throws Exception Why the URL failed.
	 */
	private URL getSiteURL() throws Exception {
		//Enable use of non pretty permlink support / custom post url / should also help with debugging other users issues
		String baseUrl = getConfig().getString( "url" ) + "/index.php?rest_route=/wmc/v1/server/";
		debug_log( "Checking base URL: " + baseUrl );
		return new URL( baseUrl + getConfig().getString( "key" ) );
	}
	private URL getSiteURLBackup() throws Exception {
		//some users were having issues with said custom post url support
		String baseUrl = getConfig().getString( "url" ) + "/wp-json/wp/v2/";
		debug_log( "Checking base URL: " + baseUrl );
		return new URL( baseUrl + getConfig().getString( "key" ) );
	}

	/**
	 * Checks all online players against the
	 * website's database looking for pending donation deliveries
	 *
	 * @return boolean
	 * @throws Exception Why the operation failed.
	 */
	boolean check() throws Exception {

		// Make 100% sure the config has at least a key and url
		this.validateConfig();

		// Contact the server.
		String pendingOrders = getPendingOrders();
		debug_log( "Logging website reply" + NL + pendingOrders.substring( 0, Math.min(pendingOrders.length(), 64) ) + "..." );

		// Server returned an empty response, bail here.
		if ( pendingOrders.isEmpty() ) {
			debug_log( "Pending orders is empty completely", 2 );
			return false;
		}

		// Create new object from JSON response.
		Gson gson = new GsonBuilder().create();
		WMCPojo wmcPojo = gson.fromJson( pendingOrders, WMCPojo.class );
		List<Order> orderList = wmcPojo.getOrders();

		// Validate we can indeed process what we need to.
		if ( wmcPojo.getData() != null ) {
			// We have an error, so we need to bail.
			wmc_log( "Code:" + wmcPojo.getCode(), 3 );
			throw new Exception( wmcPojo.getMessage() );
		}

		if ( orderList == null || orderList.isEmpty() ) {
			wmc_log( "No orders to process.", 2 );
			return false;
		}

		// foreach ORDERS in JSON feed
		List<Integer> processedOrders = new ArrayList<>();
		for ( Order order : orderList ) {
			Player player = getServer().getPlayerExact( order.getPlayer() );
			if ( null == player ) {
				debug_log( "Player was null for an order", 2 );
				continue;
			}

			// World whitelisting.
			if ( getConfig().isSet( "whitelist-worlds" ) ) {
				List<String> whitelistWorlds = getConfig().getStringList( "whitelist-worlds" );
				String playerWorld = player.getWorld().getName();
				if ( ! whitelistWorlds.contains( playerWorld ) ) {
					wmc_log( "Player " + player.getDisplayName() + " was in world " + playerWorld + " which is not in the white-list, no commands were ran." );
					continue;
				}
			}

			// Walk over all commands and run them at the next available tick.
			for ( String command : order.getCommands() ) {
				//Auth player against Mojang api
				if ( ! isPaidUser( player ) ) {
					debug_log( "User is not a paid player " + player.getDisplayName() );
					return false;
				}

				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(instance, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command), 20L);
			}

			debug_log( "Adding item to list - " + order.getOrderId() );
			processedOrders.add( order.getOrderId() );
			debug_log( "Processed length is " + processedOrders.size() );
		}

		// If it's empty, we skip it.
		if ( processedOrders.isEmpty() ) {
			return false;
		}

		// Send/update processed orders.
		return sendProcessedOrders( processedOrders );
	}

	/**
	 * Sends the processed orders to the site.
	 *
	 * @param processedOrders A list of order IDs which were processed.
	 * @return boolean
	 */
	private boolean sendProcessedOrders( List<Integer> processedOrders ) throws Exception {
		// Build the GSON data to send.
		Gson gson = new Gson();
		WMCProcessedOrders wmcProcessedOrders = new WMCProcessedOrders();
		wmcProcessedOrders.setProcessedOrders( processedOrders );
		String orders = gson.toJson( wmcProcessedOrders );

		// Setup the client.
		OkHttpClient client = new OkHttpClient();

		// Process stuffs now.
		RequestBody body = RequestBody.create( MediaType.parse( "application/json; charset=utf-8" ), orders );
		Request request = new Request.Builder().url( getSiteURL() ).post( body ).build();
		Response response = client.newCall( request ).execute();

		// If the body is empty we can do nothing.
		if ( null == response.body() ) {
			throw new Exception( "Received empty response from your server, check connections." );
		}

		// Get the JSON reply from the endpoint.
		WMCPojo wmcPojo = gson.fromJson( response.body().string(), WMCPojo.class );
		if ( null != wmcPojo.getCode() ) {
			wmc_log( "Received error when trying to send post data:" + wmcPojo.getCode(), 3 );
			throw new Exception( wmcPojo.getMessage() );
		}

		return true;
	}

	/**
	 * If debugging is enabled.
	 *
	 * @return boolean
	 */
	public boolean isDebug() {
		return getConfig().getBoolean( "debug" );
	}

	/**
	 * Gets pending orders from the WordPress JSON endpoint.
	 *
	 * @return String
	 * @throws Exception On failure.
	 */
	private String getPendingOrders() throws Exception {
		URL baseURL = getSiteURL();
		URL baseURL2 = getSiteURLBackup();
		BufferedReader in = null;
		try {
			try {
				in = new BufferedReader(new InputStreamReader(baseURL.openStream()));
			} catch (IOException e) {
				// this can throw either exception depending on the setup, this should fix that
				throw new FileNotFoundException(e.toString());
			}
		} catch (FileNotFoundException e) {
			try {
				in = new BufferedReader(new InputStreamReader(baseURL2.openStream()));
			} catch (IOException t) {
				e.getMessage();
			}
			String key = getConfig().getString("key");
			e.getMessage().replace( key == null ? "" : key, "privateKey");
		}


		StringBuilder buffer = new StringBuilder();

		// Walk over each line of the response.
		String line;
		while ( ( line = in.readLine() ) != null ) {
			buffer.append( line );
		}

		in.close();

		return buffer.toString();
	}

	/**
	 * Log stuffs.
	 *
	 * @param message The message to log.
	 */
	private void wmc_log(String message) {
		this.wmc_log( message, 1 );
	}

	/**
	 * Logs to the debug log.
	 * @param message The message
	 */
	private void debug_log( String message ) {
		if ( isDebug() ) {
			this.wmc_log( message, 1 );
		}
	}
	/**
	 * Logs to the debug log.
	 * @param message The message
	 * @param level The log leve.
	 */
	private void debug_log( String message, Integer level ) {
		if ( isDebug() ) {
			this.wmc_log( message, level );
		}
	}

	/**
	 * Log stuffs.
	 *
	 * @param message The message to log.
	 * @param level The level to log it at.
	 */
	private void wmc_log(String message, Integer level) {

		if ( ! isDebug() ) {
			return;
		}

		switch ( level ) {
			case 1:
				this.getLogger().info( message );
				break;
			case 2:
				this.getLogger().warning( message );
				break;
			case 3:
				this.getLogger().severe( message );
				break;
		}
	}

	/**
	 * Determines if the user is a paid user or not.
	 *
	 * @param player A player object
	 * @return If the user is a paid player.
	 */
	private boolean isPaidUser(Player player) {
		String playerName = player.getName();
		String playerUUID = player.getUniqueId().toString().replace( "-", "" );
		String playerKeyBase = playerName + ':' + playerUUID + ':';
		String validPlayerKey = playerKeyBase + true;
		String invalidPlayerKey = playerKeyBase + false;

		// Check if server is in online mode.
		if (Bukkit.getServer().getOnlineMode()) {
			wmc_log( "Server is in online mode.", 3 );
			return true;
		}

		if ( ! Bukkit.spigot().getConfig().getBoolean("settings.bungeecord") ) {
			wmc_log( "Server in offline Mode", 3 );
			return false;
		}

		// Check the base pattern, if it exists, return if the player is valid or not.
		// Doing so should save on many if/else statements
		if ( PlayersMap.toString().contains( playerKeyBase ) ) {
			boolean valid = PlayersMap.contains( validPlayerKey );
			if ( ! valid ) {
				player.sendMessage( "Mojang Auth: Please Speak with a admin about your purchase" );
				wmc_log("Offline mode not supported", 3);
			}

			return valid;
		}

		debug_log( "Player was not in the key set " + NL + PlayersMap.toString() );

		try {
			URL mojangUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" +  playerName);
			InputStream inputStream = mojangUrl.openStream();
			Scanner scanner = new Scanner(inputStream);
			String apiResponse = scanner.next();

			debug_log(
				"Logging stream data:" + NL +
				inputStream.toString() + NL +
				apiResponse + NL +
				playerName + NL +
				playerUUID
			);

			if ( ! apiResponse.contains( playerName ) ) {
				PlayersMap.add( invalidPlayerKey );
				throw new IOException("Mojang Auth: PlayerName doesn't exist");
			}

			if ( ! apiResponse.contains( playerUUID ) ) {
				//if Username exists but is using the offline uuid(doesn't match mojang records) throw IOException and add player to the list as cracked
				PlayersMap.add( invalidPlayerKey );
				throw new IOException("Mojang Auth: PlayerName doesn't match uuid for account");
			}

			PlayersMap.add( validPlayerKey );
			debug_log( PlayersMap.toString() );
			return true;
		} catch ( MalformedURLException urlException ) {
			debug_log("Malformed URL: " + urlException.getMessage(), 3 );
			player.sendMessage( "Mojang API Error: Please try again later or contact an admin about your purchase." );
		} catch ( IOException e ) {
			debug_log( "Map is " + PlayersMap.toString() );
			debug_log( "Message when getting URL data " + e.getMessage(), 3 );
			player.sendMessage("Mojang Auth: Please Speak with a admin about your purchase");
		}

		// Default to false, worst case they have to run this twice.
		return false;
	}
}
