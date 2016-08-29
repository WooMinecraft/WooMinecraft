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

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public final class WooMinecraft extends JavaPlugin {

	public static WooMinecraft instance;
	public String lang = "en";

	public YamlConfiguration l10n;
	public YamlConfiguration config;

	public static BukkitRunner scheduler;

	@Override
	public void onEnable() {
		instance = this;
		this.config = ( YamlConfiguration ) getConfig();

		// Save the default config.yml
		try{
			saveDefaultConfig();
		} catch ( IllegalArgumentException e ) {
			getLogger().warning( e.getMessage() );
		}

		this.lang = getConfig().getString( "lang" );
		if ( lang == null ) {
			getLogger().warning( "No default l10n set, setting to english." );
			this.lang = "en";
		}

		initCommands();
		getLogger().info( this.getLang( "log.com_init" ));

		// Setup the scheduler
		scheduler = new BukkitRunner( instance );
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
	 * Generates a comma delimited list of player names
	 *
	 * @return String
	 */
	public String getPlayerList() {
		// Build post data based on player list
		StringBuilder sb = new StringBuilder();
		for ( Player player : Bukkit.getServer().getOnlinePlayers() ) {
			sb.append( player.getName() + ", " );
		}
		String playerList = sb.toString();

		// Remove the last and final comma
		Pattern pattern = Pattern.compile( ", $" );
		Matcher matcher = pattern.matcher( playerList );

		return matcher.replaceAll( "" );
	}

	/**
	 * Checks all online players against the
	 * webiste's database looking for pending donation deliveries
	 *
	 * @return boolean
	 * @throws Exception
	 */
	public boolean check() throws Exception {

		URIBuilder uriBuilder = new URIBuilder( getConfig().getString( "url" ) );
		uriBuilder.addParameter( "key", getConfig().getString( "key" ) );

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

		char character = httpResponse.charAt( 0 );
		if ( character != '{' ) {
			// Incoming exception, so pre-log the server response.
			this.wmc_log( "Logging an improper server response", 2 );
			this.wmc_log(  "/" + httpResponse + "/", 3 );
		}

		JSONObject pendingCommands = new JSONObject( httpResponse );
		if ( ! pendingCommands.getBoolean( "success" ) ) {
			// Failure on WP side, kill over here.
			this.wmc_log( "Success was false", 2 );
			return false;
		}

		Object dataCheck = pendingCommands.get( "data" );
		if ( !( dataCheck instanceof JSONObject ) ) {
			// Typically if the array is empty in WordPress, it stays as an array in the JSON
			this.wmc_log( "No data key", 2 );
			return false;
		}

		JSONObject data = pendingCommands.getJSONObject( "data" );
		Iterator<String> playerNames = data.keys();
		JSONArray processedData = new JSONArray();

		while ( playerNames.hasNext() ) {
			// Walk over players.
			String playerName = playerNames.next();

			@SuppressWarnings( "deprecation" )
			Player player = Bukkit.getServer().getPlayer( playerName );
			if ( ! player.isOnline() ) {
				continue;
			}

			// Get all orders for the current player.
			JSONObject playerOrders = data.getJSONObject( playerName );
			Iterator<String> orderIDs = playerOrders.keys();
			while ( orderIDs.hasNext() ) {
				String orderID = orderIDs.next();

				// Get all commands per order
				JSONArray commands = playerOrders.getJSONArray( orderID );

				// Walk over commands, executing them one by one.
				for ( Integer x = 0; x < commands.length(); x++ ) {
					String command = commands.getString( x ).replace( "%s", playerName );
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

		HashMap<String, String> postData = new HashMap<>();
		postData.put( "processedOrders", processedData.toString() );

		String updatedCommandSet = rcHttp.send( url, postData );
		JSONObject updatedResponse = new JSONObject( updatedCommandSet );
		return updatedResponse.getBoolean( "success" );
	}

	public void wmc_log( String message, Integer level ) {

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
	public void initCommands() {
		getCommand( "woo" ).setExecutor( new WooCommand() );
	}
}
