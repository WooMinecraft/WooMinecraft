package com.plugish.woominecraft;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigMaker extends YamlConfiguration {
	private WooMinecraft plugin;
	private String fileName;
	private String dir;

	public ConfigMaker( WooMinecraft plugin, String fileName, String dir ) {
		this.plugin = plugin;
		this.dir = dir;
		String file;

		// Normalizes the directory, adds a slash at the beginning IF it doesn't exist.
		if ( !this.dir.equals( "" ) ){
			int index = this.dir.indexOf( File.separator );
			if ( 0 > index ) {
				this.dir = File.separator + this.dir + File.separator;
			}
		}

		this.fileName = fileName + ( fileName.endsWith( ".yml" ) ? "" : ".yml" );

		if ( isLangFile( dir ) ) {

			file = plugin.getDataFolder().getPath() + dir;
			plugin.getLogger().info( "File: " + file );

			File langFile = new File( file, fileName );
			if ( ! langFile.exists() ) {
				// Set the default l10n.
				plugin.getLogger().info( "Requested l10n file does not exist, loading default en.yml" );
				this.fileName = "en.yml";
			}
		}

		createFile();
	}

	/**
	 * Determines if a filename in path is a l10n file
	 * @param dir The filename we're looking at.
	 * @return true|false
	 */
	private boolean isLangFile( String dir) {
		String sep = File.separator;
		int index = dir.indexOf( sep + "lang" + sep );
		return index >= 0;
	}

	/**
	 * Creates the config file & directory if necessary
	 * If the file does not exist, loads & saves it from resources.
	 */
	private void createFile() {
		try {
			File file = new File( plugin.getDataFolder().getPath() + dir, fileName );
			if ( !file.exists() ) {
				fileName = dir + fileName;
				if ( plugin.getResource( fileName ) != null ) {
					plugin.saveResource( fileName, false );
				} else {
					save( file );
				}
			} else {
				load( file );
				save( file );
			}
		} catch ( Exception ex ) {
			plugin.getLogger().warning( ex.getMessage() );
		}
	}

	public void save() {
		try {
			save( new File( plugin.getDataFolder() + dir, fileName ) );
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}
}