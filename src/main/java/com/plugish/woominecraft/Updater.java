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

    public Updater(){}
    public Updater(String name, String URL) {
        this.name = name;
        try {
            downloadURL = new URL("http://github.com/WooMinecraft/WooMinecraft/releases/tag");
        } catch (MalformedURLException e) {
            plugin.getLang("log.update_bad_link");
        }
        this.byteSize = (int) this.getSizeFromSite();
    }

    public long getSizeFromSite() {
        long size = -1;
        if(downloadURL == null) return size;
        try {
            URLConnection conn = downloadURL.openConnection();
            size = conn.getContentLengthLong();
            conn.getInputStream().close();
            } catch(IOException e) {
            System.out.println(e);
            }
        return size;
    }
}