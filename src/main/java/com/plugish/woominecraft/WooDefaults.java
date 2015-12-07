package com.plugish.woominecraft;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ethan on 8/23/2015.
 *
 * TODO -filechanges- Config file doesn't really need to be nested like WooMinecraft.web.xxx  The config is just too small to warrant that.
 */
public class WooDefaults {
	public static WooMinecraft plugin = WooMinecraft.instance;

	public static void initDefaults() {
		try {
//			if ( !plugin.configFile.exists() ) {
//				plugin.configFile.getParentFile().mkdirs();
//				plugin.configFile.createNewFile();
//			}
			if ( !plugin.englishFile.exists() ) {
				plugin.englishFile.getParentFile();
				plugin.englishFile.createNewFile();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Load all necessary Yaml files
	 */
	public static void loadYamls() {
		try {
			plugin.config.load( plugin.configFile );
			plugin.english.load( plugin.englishFile );
			setDefaults();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves All our Yaml files
	 */
	public static void saveYamls() {
		try {
			plugin.config.save( plugin.configFile );

			// TODO -logic- Is this even needed?
			// It should be safe to assume that all language files are pre-configured OUTSIDE of the server
			// instance.  And therefore should not need to be 'saved' at all.
			plugin.english.save( plugin.englishFile );

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the config to defaults
	 * 
	 * TODO -logic- Rename this to a more logical name like setDefaults
	 * TODO -logic- Should use org.bukkit.ChatColor instead of hard-coding colors?
	 */
	private static void setDefaults() {
		addDefault( plugin.config, "WooMinecraft.messages.file", "english" );
		addDefault( plugin.config, "WooMinecraft.web.time_delay", 1500 );
		addDefault( plugin.config, "WooMinecraft.web.url", "www.example.com" );
		addDefault( plugin.config, "WooMinecraft.web.key", "" );

		// TODO -i18n- localize this string - excluding any [Woo] prefix
		addDefault( plugin.english, "NoPerms", "&cYou do not have permissions to do this!" );

		// TODO -i18n- localize this string - excluding any [Woo] prefix
		addDefault( plugin.english, "Reload", "&5[&fWoo&5] reloaded!" );
	}

	/**
	 * Helper Method used for setting values if they do not already exist.
	 *
	 * @param fileConfiguration The config file
	 * @param configItemPath Config Item path ie. "WooMinecraft.web.xxx"
	 * @param value The value to set
	 */
	private static void addDefault( FileConfiguration fileConfiguration, String configItemPath, Object value ) {
		// ONLY add a default if the config item doesn't exit.
		if ( fileConfiguration.getString( configItemPath ) == null ) {
			fileConfiguration.set( configItemPath, value );
		}
	}
}
