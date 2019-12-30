package com.plugish.woominecraft.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class LangSetup {

	private WooMinecraft plugin;

	private static String resourcePath;

	LangSetup(WooMinecraft plugin) {
		this.plugin = plugin;
		Configuration config = plugin.getConfig();

		String requestedLang = config.getString( "lang" );
		requestedLang += requestedLang.endsWith( ".yml" ) ? "" : ".yml";
		resourcePath = "lang" + File.separator + requestedLang;

		if ( resourcePath.contains( "\\" ) ) {
			resourcePath = resourcePath.replace( "\\", "/" );
		}


		File langConfig = new File( plugin.getDataFolder(), resourcePath );

		if ( !langConfig.exists() ) {
			InputStream in = plugin.getResourceAsStream(resourcePath );
			plugin.getLogger().warning( "Cannot find " + resourcePath + " in config directory." );

			// Now try the resources folder
			if ( null == in ) {
				plugin.getLogger().info( "We could not find " + resourcePath + " in jar file, using default english." );
				resourcePath = "lang" + File.separator + "en.yml";
			} else {
				plugin.getLogger().info( "Found " + resourcePath + " in resources directory, so we'll use that." );
			}
		}
	}

	Configuration loadConfig() {
		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File( plugin.getDataFolder(), resourcePath));
		} catch (IOException e) {
			e.printStackTrace();
			//TODO error handling
			return null;
		}
	}

}
