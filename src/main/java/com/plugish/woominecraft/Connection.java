package com.plugish.woominecraft;

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
	private static DataOutputStream outputStream = null;
	
	/**
	 * Connect to a URL
	 * 
	 * @param url_path The URL to the web site.
	 * @param key The Key needed to access the website.
	 */
	public Connection( String url_path, String key ) {
		
		try {
			URL url = new URL( url_path + "?woo_minecraft=check&key=" + key );
			
			// Type-cast to HTTPURLConnection since it extends URLConnection which is what's returned from openConnection()
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setDoInput(true);
			connection.setDoOutput(true);

		} catch( IOException e ) {
			WooMinecraft.log.config( e.getMessage() );
//			WooMinecraft.stopServer();
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
			WooMinecraft.log.severe( e.getMessage() );
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
	 * @param names
	 * @return False on failure, true otherwise.
	 */
	private boolean processNames( String names ) {
		
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
	
	public String getPlayerResults( String names ) throws IOException {
		
		InputStream stream = null;
		BufferedReader reader = null;
		String output = null;
		
		boolean namesResult = processNames( names );
		
		if ( !namesResult ) {
			return "";
		}
		
		try {
			stream = connection.getInputStream();
		} catch( IOException e ) {
			WooMinecraft.log.severe( e.getMessage() );
		}
		
		if( null != stream ) {
			InputStreamReader inReader = new InputStreamReader( stream );
			reader = new BufferedReader( inReader );
	
			StringBuilder stringResponse = new StringBuilder();
			String line;
			while ( ( line = reader.readLine() ) != null ) {
				stringResponse.append(line);
			}
			reader.close();
			
			output = stringResponse.toString();
		}
		
		return output;
	}

}
