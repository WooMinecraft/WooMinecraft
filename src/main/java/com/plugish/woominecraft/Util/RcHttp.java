package com.plugish.woominecraft.Util;

import com.plugish.woominecraft.WooMinecraft;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RcHttp {

	private CloseableHttpResponse response;
	private String resultString;
	private int Length;

	/**
	 * Just a helper method to return the user agent, so it's easily replaced.
	 * @return String
	 */
	public String get_user_agent() {
		return "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
	}

	/**
	 * Sends a GET request to a URL.
	 * @param url String
	 * @return String
	 * @throws Exception
	 */
	public String request( String url ) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet( url );

		request.addHeader( "User-Agent", get_user_agent() );

		response = client.execute( request );

		if ( 200 != response.getStatusLine().getStatusCode() ) {
			throw new Exception( "Status code is not 200 got: " + response.getStatusLine().getStatusCode() );
		}

		BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );

		StringBuilder result = new StringBuilder();
		String line;
		while ( ( line = rd.readLine() ) != null ) {
			result.append( line );
		}

		resultString = result.toString();
		Length = resultString.length();

		if ( resultString.length() > 128 ) {
			Length = 128;
		}

		// Some debug logging.
		if ( getPlugin().getConfig().getBoolean( "debug", false ) ) {
			debug();
		}

		client.close();
		return resultString;
	}

	/**
	 * Sends a POST request to a URL.
	 *
	 * @param url String
	 * @param hashMap HashMap of k/v pairs
	 * @return String
	 * @throws Exception
	 */
	public String send( String url, HashMap<String, String> hashMap ) throws Exception {

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost( url );

		post.setHeader( "User-Agent", get_user_agent() );
		post.addHeader( "content-type", "application/x-www-form-urlencoded" );

		List<NameValuePair> urlParameters = new ArrayList<>();
		for ( HashMap.Entry<String, String> entry : hashMap.entrySet() ) {
			urlParameters.add( new BasicNameValuePair( entry.getKey(), entry.getValue() ) );
		}

		post.setEntity( new UrlEncodedFormEntity( urlParameters ) );

		CloseableHttpResponse response = client.execute( post );
		if ( 200 != response.getStatusLine().getStatusCode() ) {
			throw new Exception( "Status code is not 200 got: " + response.getStatusLine().getStatusCode() );
		}

		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );

		StringBuilder result = new StringBuilder();
		String line;
		while ( ( line = bufferedReader.readLine() ) != null ) {
			result.append( line );
		}

		String resultString = result.toString();
		int Length = resultString.length();

		if ( resultString.length() > 128 ) {
			Length = 128;
		}

		// Some debug logging.
		if ( getPlugin().getConfig().getBoolean( "debug", false ) ) {
			debug();
		}

		client.close();
		return resultString;
	}

	private void debug() {
		getPlugin().wmc_log( "Sending Request" );
		getPlugin().wmc_log( "URL Config Field: " + getPlugin().getConfig().getString( "url", "empty" ) );
		getPlugin().wmc_log( "Headers: " + Arrays.toString( response.getAllHeaders() ) );
		getPlugin().wmc_log( "HTTP Response Code: " + response.getStatusLine().getStatusCode() );
		getPlugin().wmc_log( "Content Body Snippet (128 chars): " + resultString.substring( 0, Length ) );
	}

	private WooMinecraft getPlugin() {
	    return WooMinecraft.getInstance();
    }
}
