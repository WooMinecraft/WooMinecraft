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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class WooMinecraft extends JavaPlugin {

	static WooMinecraft instance;

	private YamlConfiguration l10n;
	private List<String> Players = new ArrayList<>();

	@Override
	public void onEnable() {
		instance = this;
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
		return new URL( getConfig().getString( "url" ) + "/index.php?rest_route=/wmc/v1/server/" + getConfig().getString( "key" ) );
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

		// Server returned an empty response, bail here.
		if ( pendingOrders.isEmpty() ) {
			return false;
		}

		// Create new object from JSON response.
		Gson gson = new GsonBuilder().create();
		WMCPojo wmcPojo = gson.fromJson( pendingOrders, WMCPojo.class );
		List<Order> orderList = wmcPojo.getOrders();

		// Log if debugging is enabled.
		wmc_log( pendingOrders );

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
				if (AuCh(player)) {
					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(instance, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command), 20L);
				} else {

				}

			}

			wmc_log( "Adding item to list - " + order.getOrderId() );
			processedOrders.add( order.getOrderId() );
			wmc_log( "Processed length is " + processedOrders.size() );
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
	private boolean isDebug() {
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
		// java was yelling about this var
		BufferedReader in = null;
		try {
			try {
				in = new BufferedReader(new InputStreamReader(baseURL.openStream()));
			} catch (IOException e) {
				// this can throw either exception depending on the setup, this should fix that
				throw new FileNotFoundException(e.toString());
			}
		} catch (FileNotFoundException e) {
			String msg = e.getMessage().replace(getConfig().getString("key"), "privateKey");
			WooMinecraft.instance.wmc_log(msg);
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
	//Mojang api check
	public boolean AuCh(Player p) {
		if (Bukkit.getServer().getOnlineMode()) {
			return true;
		} else if (WooMinecraft.instance.getConfig().getBoolean("BungeeMode")) {
			if (!Players.contains(p.getName() + ':' + p.getUniqueId() + ':' + true)) {
				if (Players.contains(p.getName() + ':' + p.getUniqueId() + ':' + false)) {
					p.sendMessage("Mojang Auth: Please Speak with a admin about your purchase");
					wmc_log("Offline mode not supported");

					return false;
				}
				Bukkit.getScheduler().runTaskAsynchronously(WooMinecraft.instance, () -> {
					try (InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + p.getName()).openStream(); Scanner scanner = new Scanner(inputStream)) {
						String a = scanner.next();
						if (isDebug()) {
							wmc_log(inputStream.toString());
							wmc_log(a);
						}
						if (a.contains(p.getName())) {
							if (a.contains(p.getUniqueId().toString())) {
								Players.add(p.getName() + ':' + p.getUniqueId() + ':' + true);
							} else {
								Players.add(p.getName() + ':' + p.getUniqueId() + ':' + false);
								throw new IOException("Mojang Auth: PlayerName doesn't match uuid for account");
							}
						} else {
							Players.add(p.getName() + ':' + p.getUniqueId() + ':' + false);
							throw new IOException(" Mojang Auth: PlayerName doesn't exist");
						}
					} catch (IOException e) {
						wmc_log(e.getMessage(), 3);
						p.sendMessage("Mojang Auth:Please Speak with a admin about your purchase");
						if (isDebug()) {
							wmc_log(Players.toString());
						}
					}
				});
			}
		} else {
			wmc_log("Server in offline Mode");
			return false;
		}
		return true;
	}
}
