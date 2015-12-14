package com.plugish.woominecraft;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Check for updates on GitHub or Dropbox for an Update, and download the updates if needed.
 * VERY, VERY IMPORTANT: Because there are no standards for adding auto-update toggles in your plugin's config, this system does not provide an auto-update toggle system.
 * However an example of a good configuration option would be something similar to 'auto-update: true' - if this value is set to false the auto-updater doesn't run.
 *
 * @author TekkitCommando
 * @version 1.0
 * Inspired by Atynine
 */

public class Updater {
	public static WooMinecraft plugin = WooMinecraft.instance;

	private String name = "Woo Minecraft Update";
	private int byteSize = 0;

	private byte data[];
	private int downloadBytes = 0;
	private boolean startedDownload = false;
	private URL downloadURL;

	private long startTime;

	public Updater() {
	}

	public Updater( String name, String URL ) {
		this.name = name;
		try {
			downloadURL = new URL( "http://github.com/WooMinecraft/WooMinecraft/releases/tag" );
		} catch ( MalformedURLException e ) {
			plugin.getLang( "log.update_bad_link" );
		}
		this.byteSize = ( int ) this.getSizeFromSite();
		data = new byte[(int) byteSize+1];
	}

	public void run() {
		updater();
		try {
			saveFile(System.getProperty("user.dir"));
		} catch(IOException e) {
			System.out.println("Failed to find current path.");
			e.printStackTrace();
		}
	}

	public void updater() {
		BufferedInputStream update = null;
		if(downloadURL == null) return;
		try {
			update = new BufferedInputStream(downloadURL.openStream());
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		if(update != null) {
			try {
				System.out.println("Starting " + name + " download.");
				startTime = System.currentTimeMillis();
				while((update.read(data, downloadBytes, 1)) != -1) {
					startedDownload = true;
					downloadBytes++;
				}
				update.close();
				plugin.getLang("log.update_successful" + name);
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	public void saveFile(String path) throws IOException {
		if(downloadURL == null) return;
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir")));
			bos.write(data);
			bos.flush();
			bos.close();
			System.out.println("Successfully saved to Plugins folder");
		} catch (IOException e){
			System.out.println("Failed to download file: " + name);
			System.out.println(e);
		}
	}

	public long getSizeFromSite() {
		long size = -1;
		if ( downloadURL == null ) return size;
		try {
			URLConnection conn = downloadURL.openConnection();
			size = conn.getContentLengthLong();
			conn.getInputStream().close();
		} catch ( IOException e ) {
			System.out.println( e );
		}
		return size;
	}
}