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

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public final class WooMinecraft extends JavaPlugin {

	static WooMinecraft instance;

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
		return new URL( getConfig().getString( "url" ) + "/wp-json/wmc/v1/server/" + getConfig().getString( "key" ) );
	}

	/**
	 * Checks all online players against the
	 * website's database looking for pending donation deliveries
	 *
	 * @return boolean
	 * @throws Exception Why the operation failed.
	 */
	boolean check() throws Exception {

		//
		// The process:
		// Contact SERVER
		String pendingOrders = getPendingOrders();

		wmc_log( pendingOrders );

		// -- if DATA is empty
		// do nothing
		// -- else
		// foreach PLAYERS in JSON feed
		// -- if PLAYER is online
		// -- foreach orders
		// -- -- run commands for PLAYER
		// -- else
		// do nothing
		//

		// Make 100% sure the config has at least a key and url
		this.validateConfig();



		return true;
	}


	private String getPendingOrders() throws Exception {
		URL baseURL = getSiteURL();
		BufferedReader in = new BufferedReader( new InputStreamReader( baseURL.openStream() ) );
		StringBuilder buffer = new StringBuilder();

		// Walk over each line of the response.
		String line;
		while ( ( line = in.readLine() ) != null ) {
			buffer.append( line );
		}

		in.close();

		return buffer.toString();
	}

	private void wmc_log(String message) {
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
}
