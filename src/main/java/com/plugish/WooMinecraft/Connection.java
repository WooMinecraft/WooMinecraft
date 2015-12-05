package com.plugish.WooMinecraft;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {
	
	private static URL url;
	private static HttpURLConnection connection;
	private static DataOutputStream outputStream;
	
	/**
	 * 
	 * @param url The URL to the web site.
	 * @param path The Path to the web site.
	 */
	public Connection( String[] url_path, String[] key ) {
		
		try {
			url = new URL( url_path + "?woo_minecraft=check&key=" + key );
			
			// Type-cast to HTTPURLConnection since it extends URLConnection which is what's returned from openConnection()
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
		} catch( IOException e ) {
			WooMinecraft.log.severe( e.getMessage() );
			WooMinecraft.stopServer();
		}
		
	}
	
	/**
	 * Sets the DataOutputStream for accessability
	 */
	private static void setOutputStream() {
		try {
			OutputStream con = connection.getOutputStream();
			outputStream = new DataOutputStream( con );
		} catch ( IOException e ) {
			WooMinecraft.log.severe( e.getMessage() );
		}
	}
	
	/**
	 * Gets the data output stream, sets it if necessary.
	 * 
	 * @return DataOutputStream
	 */
	private static DataOutputStream GetOutputStream() {
		if ( null == outputStream ) {
			setOutputStream();
		}
		
		return outputStream;
	}
	
	/**
	 * Writes all Names to a DataOutputStream
	 * @param names
	 * @return False on failure, true otherwise.
	 */
	public static boolean processNames( String names ) {
		
		DataOutputStream stream = GetOutputStream();
		if ( null == stream ) {
			WooMinecraft.log.severe( "Cannot open Data output stream!" );
		}
		
		try {
			stream.writeBytes( "names=" + names );
			stream.flush();
			stream.close();
		} catch( IOException e ) {
			WooMinecraft.log.severe( e.getMessage() );
			return false;
		}
		
		return true;
	}

}
