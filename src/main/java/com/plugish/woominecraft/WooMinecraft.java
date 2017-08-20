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
import com.plugish.woominecraft.Pojo.OrderData;
import com.plugish.woominecraft.Pojo.OrderResponse;
import com.plugish.woominecraft.Util.BukkitRunner;
import com.plugish.woominecraft.Util.Orders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class WooMinecraft extends JavaPlugin {

	public static WooMinecraft instance;
	public String lang = "en";

	public YamlConfiguration l10n;
	public YamlConfiguration config;

	public static BukkitRunner scheduler;

	public final String restBase = "wp-json/woominecraft/v1/server/";

	public String serverEndpoint;

	@Override
	public void onEnable() {
		instance = this;
		this.config = ( YamlConfiguration ) getConfig();

		// Save the default config.yml
		try{
			saveDefaultConfig();
		} catch ( IllegalArgumentException e ) {
			getLogger().warning( e.getMessage() );
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}

		// Ensure we have a valid server URL.
		try {
			serverEndpoint = getServerUrl().toString();
		} catch ( Exception e ) {
			getLogger().severe( e.getMessage() );
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}

		// Make 100% sure the config has at least a key and url
		try {
			this.validateConfig();
		} catch ( Exception e ) {
			getLogger().severe( e.getMessage() );
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}

		this.lang = getConfig().getString( "lang" );
		if ( lang == null ) {
			getLogger().warning( "No default l10n set, setting to english." );
			this.lang = "en";
		}

		// Initialize commands
		initCommands();
		getLogger().info( this.getLang( "log.com_init" ));

		// Setup the scheduler
//		scheduler = new BukkitRunner( instance );
//		scheduler.runTaskTimerAsynchronously( Bukkit.getPluginManager().getPlugin("WooMinecraft"), config.getInt( "update_interval" ) * 20, config.getInt( "update_interval" ) * 20 );

		BukkitScheduler scheduler = getServer().getScheduler();
		Integer i = config.getInt( "update_interval" ) * 20;
		Long interval = Long.valueOf( i );
		scheduler.runTaskTimerAsynchronously( this, new Runnable() {
			@Override
			public void run() {
				try{
					check();
				} catch ( Exception e ) {
					getLogger().severe( e.getMessage() );
				}
			}
		}, interval, interval );

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
	 * @throws Exception
	 */
	public void validateConfig() throws Exception {

		if ( 1 > this.getConfig().getString( "url" ).length() ) {
			throw new Exception( "Server URL is empty, check config." );
		} else if ( this.getConfig().getString( "url" ).equals( "http://playground.dev" ) ) {
			throw new Exception( "URL is still the default URL, check config." );
		} else if ( 1 > this.getConfig().getString( "key" ).length() ) {
			throw new Exception( "Server Key is empty, this is insecure, check config." );
		}

		if ( ! urlIsValidJSON() ) {
			throw new Exception( "The URL does not have access to the /wp-json endpoint. Ensure your website URL is the WordPress URL." );
		}
	}

	/**
	 * Ensures the URL provided in the config has access to the WP-JSON endpoint.
	 *
	 * @return True if the app has access to wp-json/
	 * @throws Exception
	 */
	public Boolean urlIsValidJSON() throws Exception {
		Client client = ClientBuilder.newClient();
		Response response = client.target( getServerUrl().toString() ).request().get();
		MediaType contentType = response.getMediaType();

		Boolean isValid = false;

		if ( contentType.getType().equals( MediaType.APPLICATION_JSON_TYPE.getType() )
			&& contentType.getSubtype().equals( MediaType.APPLICATION_JSON_TYPE.getSubtype() )
		) {
			isValid = true;
		}
		client.close(); // gotta be nice and close.
		return isValid;
	}

	/**
	 * Builds a URI for REST requests.
	 *
	 * @return URI A full URL to the REST path.
	 * @throws Exception
	 */
	private URI getServerUrl() throws Exception {

		URI uri = new URI( getConfig().getString( "url" ) );
		String key = getConfig().getString( "key" );
		String path = uri.getPath();

		if ( null == path || path.length() == 0 ) {
			path = "/" + this.restBase;
		} else if ( path.charAt( path.length() - 1 ) == '/' ) {
			path = path + this.restBase;
		} else {
			path = path + "/" + this.restBase;
		}


		return uri.resolve( path + key );
	}

	/**
	 * Tries to get and execute commands that are pending.
	 *
	 * @throws Exception
	 */
	public void check() throws Exception {

		Orders orders = new Orders();
		ArrayList<Integer> processedOrders = new ArrayList<>();
		OrderResponse allOrders = orders.getAllOrders( serverEndpoint );

		for ( OrderData orderData : allOrders.getOrderData() ) {
			if ( orderData.getOnline() ) {
				Player player = Bukkit.getServer().getPlayerExact( orderData.getPlayer() );
				if ( getConfig().isSet( "whitelist-worlds" ) ) {
					List< String > whitelistWorlds = getConfig().getStringList( "whitelist-worlds" );
					String playerWorld = player.getWorld().getName();
					if ( !whitelistWorlds.contains( playerWorld ) ) {
						wmc_log( "Player " + player.getDisplayName() + " was in world " + playerWorld + " which is not in the white-list, no commands were ran." );
						continue;
					}
				}
				executeCommands( orderData.getCommands(), player.getName() );
				processedOrders.add( orderData.getOrderID() );
			} else {
				OfflinePlayer player = Bukkit.getServer().getOfflinePlayer( orderData.getPlayer() );
				executeCommands( orderData.getCommands(), player.getName() );
				processedOrders.add( orderData.getOrderID() );
			}
		}

		orders.updateOrders( serverEndpoint, processedOrders );
	}

	/**
	 * Executes the commands.
	 *
	 * @param commands A list of commands.
	 * @param playerName The player name, duh!
	 */
	private void executeCommands( ArrayList< String > commands, String playerName ) {
		for ( String command : commands ) {
			// some replacements on the command.
			final String _command = command.replace( "%s", playerName ).replace( "&quot;", "\"" ).replace( "&#039;", "'" );
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask( instance, () -> Bukkit.getServer().dispatchCommand( Bukkit.getServer().getConsoleSender(), _command ), 20L );
		}
	}

	public void wmc_log( String message ) {
		this.wmc_log( message, 1 );
	}

	public void wmc_log( Exception message ) {
		this.wmc_log( message.getMessage(), 3 );
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
