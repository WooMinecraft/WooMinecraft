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

import com.plugish.woominecraft.Commands.WooCommand;
import com.plugish.woominecraft.Lang.LangSetup;
import com.plugish.woominecraft.Util.BukkitRunner;
import com.plugish.woominecraft.Util.RcHttp;
import org.apache.http.client.utils.URIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class WooMinecraft extends JavaPlugin {

	public static WooMinecraft instance;

	private YamlConfiguration l10n;

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

		initCommands();
		getLogger().info( this.getLang( "log.com_init" ));

		// Setup the scheduler
		BukkitRunner scheduler = new BukkitRunner(instance);
		scheduler.runTaskTimerAsynchronously( instance, config.getInt( "update_interval" ) * 20, config.getInt( "update_interval" ) * 20 );

		getLogger().info( this.getLang( "log.enabled" ) );
	}

	@Override
	public void onDisable() {
		getLogger().info( this.getLang( "log.com_init" ) );
	}

	/**
	 * Helper method to get localized strings
	 *
	 * Much better than typing this.l10n.getString...
	 * @param path Path to the config var
	 * @return String
	 */
	public String getLang( String path ) {
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
	 * Checks all online players against the
	 * website's database looking for pending donation deliveries
	 *
	 * @return boolean
	 * @throws Exception Why the operation failed.
	 */
	public boolean check() throws Exception {

		// Make 100% sure the config has at least a key and url
		this.validateConfig();

		URIBuilder uriBuilder = new URIBuilder( getConfig().getString( "url" ) );
		uriBuilder.addParameter( "wmc_key", getConfig().getString( "key" ) );

		String url = uriBuilder.toString();
		if ( url.equals( "" ) ) {
			throw new Exception( "WMC URL is empty for some reason" );
		}

		RcHttp rcHttp = new RcHttp( this );
		String httpResponse = rcHttp.request( url );

		// No response, kill out here.
		if ( httpResponse.equals( "" ) ) {
			return false;
		}

		// Grab the pending commands
		JSONObject pendingCommands = new JSONObject( httpResponse );

		// If the request was not a WordPress success, we may have a message
		if ( ! pendingCommands.getBoolean( "success" ) ) {

			wmc_log( "Server response was false, checking for message and bailing.", 2 );

			// See if we have a data object.
			Object dataCheck = pendingCommands.get( "data" );
			if ( dataCheck instanceof JSONObject ) {
				JSONObject errors = null;
				try {
					errors = pendingCommands.getJSONObject( "data" );
				} catch (IllegalStateException e) {
					getLogger().info(String.valueOf(e));
					String msg = errors.getString( "msg" );
					// Throw the message as an exception.
					throw new Exception( msg );
				}


			}

			return false;
		}

		Object dataCheck = pendingCommands.get( "data" );
		if ( !( dataCheck instanceof JSONObject ) ) {
			wmc_log( "No data to process, or data is invalid." );
			return false;
		}

		JSONObject data = pendingCommands.getJSONObject( "data" );
		Iterator<String> playerNames = data.keys();
		JSONArray processedData = new JSONArray();

		wmc_log( "Player names acquired -- walking over them now." );
		while ( playerNames.hasNext() ) {
			// Walk over players.
			String playerName = playerNames.next();
			
			/*
			 * Check if players are online if require-player-be-online is true
			 */
			if (!getConfig().isSet("require-player-be-online") || getConfig().getBoolean("require-player-be-online")) {
				wmc_log( "Checking for player: " + playerName );
	
				@SuppressWarnings( "deprecation" )
				Player player = Bukkit.getServer().getPlayerExact( playerName );
				if ( player == null ) {
					wmc_log( "Player not found.", 2 );
					continue;
				}

				/*
				 * Use white-list worlds check, if it's set.
				 */
				if ( getConfig().isSet( "whitelist-worlds" ) ) {	
					List<String> whitelistWorlds = getConfig().getStringList( "whitelist-worlds" );
					String playerWorld = player.getWorld().getName();
					if ( ! whitelistWorlds.contains( playerWorld ) ) {
						wmc_log( "Player " + player.getDisplayName() + " was in world " + playerWorld + " which is not in the white-list, no commands were ran." );
						continue;
					}
				}
			}
			
			// Get all orders for the current player.
			JSONObject playerOrders = data.getJSONObject( playerName );
			Iterator<String> orderIDs = playerOrders.keys();

			wmc_log( "Walking over orders for player.", 1 );
			while ( orderIDs.hasNext() ) {
				String orderID = orderIDs.next();
				wmc_log( "===========================" );

				// Get all commands per order
				JSONArray commands = playerOrders.getJSONArray( orderID );

				wmc_log( "Processing command for order: " + orderID );
                wmc_log( "===========================" );
				wmc_log( "Command Set: " + commands.toString() );

				// Walk over commands, executing them one by one.
				for ( Integer x = 0; x < commands.length(); x++ ) {
					String baseCommand = commands.getString( x );

					wmc_log( "Dirty Command: " + baseCommand );

					final String command = baseCommand.replace( "%s", playerName ).replace( "&quot;", "\"" ).replace( "&#039;", "'" );

					wmc_log( "Clean Command: " + command );

					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

					// TODO: Make this better... nesting a 'new' class while not a bad idea is bad practice.
					scheduler.scheduleSyncDelayedTask( instance, new Runnable() {
						@Override
						public void run() {
							Bukkit.getServer().dispatchCommand( Bukkit.getServer().getConsoleSender(), command );
						}
					}, 20L );
				}
				processedData.put( Integer.parseInt( orderID ) );
			}
		}

		if ( 1 > processedData.length() ) {
			wmc_log( "Processed zero data, exiting.", 2 );
			return false;
		}

		wmc_log( "Sending data to the website." );

		HashMap< String, String > postData = new HashMap<>();
		postData.put( "processedOrders", processedData.toString() );

		String updatedCommandSet = rcHttp.send( url, postData );
		JSONObject updatedResponse = new JSONObject( updatedCommandSet );
		boolean status = updatedResponse.getBoolean( "success" );

		if ( ! status ) {
			Object dataSet = updatedResponse.get( "data" );
			if ( dataSet instanceof JSONObject ) {
				String message = ( ( JSONObject ) dataSet ).getString( "msg" );
				throw new Exception( message );
			}
			throw new Exception( "Failed sending updated orders to the server, got this instead:" + updatedCommandSet );
		}

		wmc_log( "All order data processed." );

		return true;
	}

	public void wmc_log( String message ) {
		this.wmc_log( message, 1 );
	}

	private void wmc_log(String message, Integer level) {

		if ( ! this.getConfig().getBoolean( "debug" ) ) {
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
	 * Initialize Commands
	 */
	private void initCommands() {
		getCommand( "woo" ).setExecutor( new WooCommand() );
	}
}
