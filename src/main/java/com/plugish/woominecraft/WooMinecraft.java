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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.plugish.woominecraft.Commands.WooCommand;
import com.plugish.woominecraft.Connection.Connection;
import com.plugish.woominecraft.Lang.LangSetup;
import com.plugish.woominecraft.Util.BukkitRunner;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.JSONArray;
import org.json.JSONException;
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
		getLogger().info( this.getLang( "getLogger().com_init" ));

		// Setup the scheduler
		scheduler = new BukkitRunner( instance );
		scheduler.runTaskTimerAsynchronously( instance, config.getInt( "update_interval" ) * 20, config.getInt( "update_interval" ) * 20 );

		getLogger().info( this.getLang( "getLogger().enabled" ) );
	}

	@Override
	public void onDisable() {
		getLogger().info( this.getLang( "getLogger().com_init" ) );
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
	 * @throws JSONException
	 */
	public boolean check() throws JSONException {

		String namesResults;
		JSONObject json = null;

		String key = config.getString( "key" );
		String url = config.getString( "url" );

		// Check for player counts first
		Collection< ? extends Player > list = Bukkit.getOnlinePlayers();
		if ( list.size() < 1 ) return false;

		// Must match main object method.
		Connection urlConnection = new Connection( this, url, key );

		if ( urlConnection.connection == null ) {
			return false;
		}

		ArrayList< Integer > rowUpdates = new ArrayList< Integer >();
		String playerList = getPlayerList();

		namesResults = urlConnection.getPlayerResults( wmcDebug( "playerList", playerList ) );

		wmcDebug( "Result from names check", namesResults );

		// If the server says there are no results for the sent names
		// just return, no need to continue.
		if ( namesResults.equals( "" ) ) {
			return false;
		}

		try {
			json = new JSONObject( namesResults );
		} catch ( JSONException e ) {
			getLogger().severe( e.getMessage() );
		}

		// Must have json data to continue.
		if ( null == json ) {
			return false;
		}

		if ( json.getBoolean( "success" ) ) {
			JSONObject jsonObject = json.getJSONObject( "data" );
			Iterator<?> keys = jsonObject.keys();
			while( keys.hasNext() ) {
				String cur_key = ( String ) keys.next();
				JSONArray obj = ( JSONArray ) jsonObject.get( cur_key );

				for( int i = 0; i < obj.length(); i++ ) {
					JSONObject cur_object = obj.getJSONObject( i );
					String playerName = cur_object.getString( "player_name" );
					String x = cur_object.getString( "command" );
					final String command = x.replace( "%s", playerName );

					// @TODO: Update to getUUID()
					@SuppressWarnings( "deprecation" )
					Player pN = Bukkit.getServer().getPlayer( playerName );

					if ( x.substring( 0, 3 ).equalsIgnoreCase( "give" ) ) {
						int count = 0;
						for ( ItemStack iN : pN.getInventory() ) {
							if ( iN == null )
								count++;
						}

						if ( count == 0 ) return false;
					}

					int id = cur_object.getInt( "id" );

					BukkitScheduler sch = Bukkit.getServer().getScheduler();

					// TODO: Make this better... nesting a 'new' class while not a bad idea is bad practice.
					sch.scheduleSyncDelayedTask( instance, new Runnable() {
						@Override
						public void run() {
							Bukkit.getServer().dispatchCommand( Bukkit.getServer().getConsoleSender(), command );
						}
					}, 20L );
					rowUpdates.add( id );
				}
			}
			remove( rowUpdates );
			return true;
		} else {
			getLogger().info( this.getLang( "getLogger().no_donations" ) );
			if ( json.has( "data" ) ) {
				JSONObject data = json.getJSONObject( "data" );
				if ( data.has( "msg" ) ) {
					getLogger().severe( data.getString( "msg" ) );
				}
			}
			if ( json.has( "debug_info" ) ) {
				getLogger().info( json.getString( "debug_info" ) );
			}
		}

		return false;
	}

	/**
	 * Removes IDs from
	 *
	 * @param ids Ids to remove
	 */
	private void remove( ArrayList< Integer > ids ) {
		if ( ids.isEmpty() ) return;

		try {
			String sPath = this.config.getString( "url" );
			String key = this.config.getString( "key" );

//			TODO update this and the Connection class
			String theURL = wmcDebug( "RemoveURL", sPath + "?woo_minecraft=update&key=" + key );
			URL url = new URL( theURL );
			HttpURLConnection con = ( HttpURLConnection ) url.openConnection();
			con.setRequestMethod( "POST" );
			con.setRequestProperty( "User-Agent", "Mozilla/5.0" );
			String urlParams = StringUtils.join( ids, ',' );
			con.setDoInput( true );
			con.setDoOutput( true );

			DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
			wr.writeBytes( "players=" + urlParams );
			wr.flush();
			wr.close();

			BufferedReader input = new BufferedReader( new InputStreamReader( con.getInputStream() ) );

			String response = input.readLine();
			try{
				JSONObject json = new JSONObject( response );
				if ( ! json.getBoolean( "success" ) ) {
					getLogger().warning( this.getLang( "getLogger().cannot_update" ) );
					getLogger().info( response );
					input.close();
				} else {
					getLogger().info( this.getLang( "getLogger().don_updated" ) );
				}
			} catch ( JSONException e ) {
				getLogger().warning( e.getMessage() );
			}

		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize Commands
	 */
	public void initCommands() {
		getCommand( "woo" ).setExecutor( new WooCommand() );
	}

	/**
	 * Helper for debugging data if the config is set
	 *
	 * @param msg The message string
	 * @return String
	 */
	public String wmcDebug( String prefix, String msg ) {

		if ( config.getBoolean( "debug" ) ) {
			getLogger().info( prefix + ": " + msg );
		}

		return msg;
	}
}
