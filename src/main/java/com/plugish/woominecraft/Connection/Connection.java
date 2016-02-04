package com.plugish.woominecraft.Connection;

import com.plugish.woominecraft.WooMinecraft;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {
	
	public HttpURLConnection connection = null;
	public WooMinecraft plugin;
	private static DataOutputStream outputStream = null;
	
	/**
	 * Connect to a URL
	 *
	 * @param plugin Instance of WooMinecraft
	 * @param url_path The URL to the web site.
	 * @param key The Key needed to access the website.
	 */
	public Connection( WooMinecraft plugin, String url_path, String key ) {

		this.plugin = plugin;
		
		try {
			URL url = new URL( url_path + "?woo_minecraft=check&key=" + key );
			
			// Type-cast to HTTPURLConnection since it extends URLConnection which is what's returned from openConnection()
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setDoInput(true);
			connection.setDoOutput(true);

		} catch( IOException e ) {
			plugin.getLogger().warning( e.getMessage() );
		}
		
	}
	
	/**
	 * Sets the DataOutputStream for accessability
	 */
	private void setOutputStream() {
		try {
			OutputStream con = this.connection.getOutputStream();
			outputStream = new DataOutputStream( con );
		} catch ( IOException e ) {
			plugin.getLogger().severe( e.getMessage() );
		}
	}
	
	/**
	 * Gets the data output stream, sets it if necessary.
	 * 
	 * @return DataOutputStream
	 */
	private DataOutputStream GetOutputStream() {
		if ( null == outputStream ) {
			setOutputStream();
		}
		
		return outputStream;
	}
	
	/**
	 * Writes all Names to a DataOutputStream
	 * @param names A list of player names.
	 * @return False on failure, true otherwise.
	 */
	private boolean processNames( String names ) {
		
		DataOutputStream stream = GetOutputStream();
		if ( null == stream ) {
			plugin.getLogger().warning( plugin.getLang( "log.fail_dos" ) );
			return false;
		}
		
		try {
			stream.writeBytes( "names=" + names );
			stream.flush();
			stream.close();
		} catch( IOException e ) {
			plugin.getLogger().warning( e.getMessage() );
			return false;
		}
		
		return true;
	}
	
	public String getPlayerResults( String names ) {
		
		InputStream stream = null;
		BufferedReader reader;
		String output = null;
		
		boolean namesResult = this.processNames( names );
		
		if ( !namesResult ) {
			return "";
		}
		
		try {
			stream = connection.getInputStream();
		} catch( IOException e ) {
			plugin.getLogger().severe( e.getMessage() );
		}
		
		if( null != stream ) {
			InputStreamReader inReader = new InputStreamReader( stream );
			reader = new BufferedReader( inReader );
	
			StringBuilder stringResponse = new StringBuilder();
			String line;
			try {

				while ( ( line = reader.readLine() ) != null ) {
					stringResponse.append(line);
				}

				reader.close();
				output = stringResponse.toString();

			} catch ( IOException e ) {
				plugin.getLogger().severe( e.getMessage() );
			}
		}
		
		return output;
	}

}
