package com.plugish.woominecraft;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created by Jay on 12/12/2015.
 */
public class LangSetup {

	private JavaPlugin plugin;

	public static String resourcePath;

	public LangSetup( JavaPlugin plugin ) {
		this.plugin = plugin;
		FileConfiguration config = plugin.getConfig();

		String requestedLang = config.getString( "lang" );
		requestedLang += requestedLang.endsWith( ".yml" ) ? "" : ".yml";
		resourcePath = "lang" + File.separator + requestedLang;

		File langConfig = new File( plugin.getDataFolder(), resourcePath );

		if ( !langConfig.exists() ) {
			plugin.getLogger().warning( "Cannot find " + resourcePath + " in config directory." );

			// Now try the resources folder
			if ( null == plugin.getResource( resourcePath ) ) {
				plugin.getLogger().info( "We could not find " + resourcePath + " in jar file, using default english." );
				resourcePath = "lang" + File.separator + "en.yml";
			} else {
				plugin.getLogger().info( "Found " + resourcePath + " in resources directory, so we'll use that." );
			}

			copyJarResources();
		}
	}

	public YamlConfiguration loadConfig() {
		return YamlConfiguration.loadConfiguration( new File( plugin.getDataFolder(), resourcePath ) );
	}

	/**
	 * Attempts to save the resource from our config directory.
	 */
	private void copyJarResources() {
		try {
			plugin.saveResource( resourcePath, false );
		} catch ( Exception e ) {
			plugin.getLogger().warning( e.getMessage() );
		}
	}



}
