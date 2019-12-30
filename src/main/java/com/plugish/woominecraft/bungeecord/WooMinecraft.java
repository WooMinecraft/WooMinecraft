/*
 * Woo Minecraft Donation plugin
 * Author:	   Jerry Wood
 * Author URI: http://plugish.com
 * License:	   GPLv2
 *
 * Copyright 2014 All rights Reserved
 *
 */
package com.plugish.woominecraft.bungeecord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plugish.woominecraft.pojo.Order;
import com.plugish.woominecraft.pojo.WMCPojo;
import com.plugish.woominecraft.pojo.WMCProcessedOrders;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import okhttp3.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public final class WooMinecraft extends Plugin {

    static WooMinecraft instance;

    private Configuration l10n;

    private Configuration config;


    public Configuration getConfig() {
        return config;
    }


    public void reloadConfig(){
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void onEnable() {
        instance = this;
        reloadConfig();

        String lang = config.getString("lang");
        if (lang == null) {
            getLogger().warning("No default l10n set, setting to english.");
        }

        // Load the commands.
		getProxy().getPluginManager().registerCommand(this, new WooCommand("bwoo","woo.admin"));

        // Log when plugin is initialized.
        getLogger().info(this.getLang("log.com_init"));

        // Setup the scheduler
        getProxy().getScheduler().schedule(this, () -> {
                    try {
                        check();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, config.getInt("update_interval"),
                config.getInt("update_interval"),
                TimeUnit.SECONDS);

        // Log when plugin is fully enabled ( setup complete ).
        getLogger().info(this.getLang("log.enabled"));
    }

    @Override
    public void onDisable() {
        // Log when the plugin is fully shut down.
        getLogger().info(this.getLang("log.com_init"));
    }

    /**
     * Helper method to get localized strings
     * <p>
     * Much better than typing this.l10n.getString...
     *
     * @param path Path to the config var
     * @return String
     */
    String getLang(String path) {
        if (null == this.l10n) {

            LangSetup lang = new LangSetup(instance);
            l10n = lang.loadConfig();
        }

        return this.l10n.getString(path);
    }

    /**
     * Validates the basics needed in the config.yml file.
     * <p>
     * Multiple reports of user configs not having keys etc... so this will ensure they know of this
     * and will not allow checks to continue if the required data isn't set in the config.
     *
     * @throws Exception Reason for failing to validate the config.
     */
    private void validateConfig() throws Exception {

        if (1 > this.getConfig().getString("url").length()) {
            throw new Exception("Server URL is empty, check config.");
        } else if (this.getConfig().getString("url").equals("http://playground.dev")) {
            throw new Exception("URL is still the default URL, check config.");
        } else if (1 > this.getConfig().getString("key").length()) {
            throw new Exception("Server Key is empty, this is insecure, check config.");
        }
    }

    /**
     * Gets the site URL
     *
     * @return URL
     * @throws Exception Why the URL failed.
     */
    private URL getSiteURL() throws Exception {
        return new URL(getConfig().getString("url") + "/wp-json/wmc/v1/server/" + getConfig().getString("key"));
    }

    /**
     * Checks all online players against the
     * website's database looking for pending donation deliveries
     *
     * @return boolean
     * @throws Exception Why the operation failed.
     */
    boolean check() throws Exception {

        // Make 100% sure the config has at least a key and url
        this.validateConfig();

        // Contact the server.
        String pendingOrders = getPendingOrders();

        // Server returned an empty response, bail here.
        if (pendingOrders.isEmpty()) {
            return false;
        }

        // Create new object from JSON response.
        Gson gson = new GsonBuilder().create();
        WMCPojo wmcPojo = gson.fromJson(pendingOrders, WMCPojo.class);
        List<Order> orderList = wmcPojo.getOrders();

        // Log if debugging is enabled.
        wmc_log(pendingOrders);

        // Validate we can indeed process what we need to.
        if (wmcPojo.getData() != null) {
            // We have an error, so we need to bail.
            wmc_log("Code:" + wmcPojo.getCode(), 3);
            throw new Exception(wmcPojo.getMessage());
        }

        if (orderList == null || orderList.isEmpty()) {
            wmc_log("No orders to process.", 2);
            return false;
        }

        // foreach ORDERS in JSON feed
        List<Integer> processedOrders = new ArrayList<>();
        for (Order order : orderList) {

			ProxiedPlayer player = getProxy().getPlayer(order.getPlayer());
            if (null == player) {
                continue;
            }


            // Walk over all commands and run them at the next available tick.
            for (String command : order.getCommands()) {
            	getProxy().getScheduler().schedule(this, () ->  {
					ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
				}, 1,TimeUnit.SECONDS);
            }

            wmc_log("Adding item to list - " + order.getOrderId());
            processedOrders.add(order.getOrderId());
            wmc_log("Processed length is " + processedOrders.size());
        }

        // If it's empty, we skip it.
        if (processedOrders.isEmpty()) {
            return false;
        }

        // Send/update processed orders.
        return sendProcessedOrders(processedOrders);
    }

    /**
     * Sends the processed orders to the site.
     *
     * @param processedOrders A list of order IDs which were processed.
     * @return boolean
     */
    private boolean sendProcessedOrders(List<Integer> processedOrders) throws Exception {
        // Build the GSON data to send.
        Gson gson = new Gson();
        WMCProcessedOrders wmcProcessedOrders = new WMCProcessedOrders();
        wmcProcessedOrders.setProcessedOrders(processedOrders);
        String orders = gson.toJson(wmcProcessedOrders);

        // Setup the client.
        OkHttpClient client = new OkHttpClient();

        // Process stuffs now.
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), orders);
        Request request = new Request.Builder().url(getSiteURL()).post(body).build();
        Response response = client.newCall(request).execute();

        // If the body is empty we can do nothing.
        if (null == response.body()) {
            throw new Exception("Received empty response from your server, check connections.");
        }

        // Get the JSON reply from the endpoint.
        WMCPojo wmcPojo = gson.fromJson(response.body().string(), WMCPojo.class);
        if (null != wmcPojo.getCode()) {
            wmc_log("Received error when trying to send post data:" + wmcPojo.getCode(), 3);
            throw new Exception(wmcPojo.getMessage());
        }

        return true;
    }

    /**
     * If debugging is enabled.
     *
     * @return boolean
     */
    private boolean isDebug() {
        return getConfig().getBoolean("debug");
    }

    /**
     * Gets pending orders from the WordPress JSON endpoint.
     *
     * @return String
     * @throws Exception On failure.
     */
    private String getPendingOrders() throws Exception {
        URL baseURL = getSiteURL();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(baseURL.openStream()));
        } catch (FileNotFoundException e) {
            String msg = e.getMessage().replace(getConfig().getString("key"), "privateKey");
            throw new FileNotFoundException(msg);
        }

        StringBuilder buffer = new StringBuilder();

        // Walk over each line of the response.
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }

        in.close();

        return buffer.toString();
    }

    /**
     * Log stuffs.
     *
     * @param message The message to log.
     */
    private void wmc_log(String message) {
        this.wmc_log(message, 1);
    }

    /**
     * Log stuffs.
     *
     * @param message The message to log.
     * @param level   The level to log it at.
     */
    private void wmc_log(String message, Integer level) {

        if (!isDebug()) {
            return;
        }

        switch (level) {
            case 1:
                this.getLogger().info(message);
                break;
            case 2:
                this.getLogger().warning(message);
                break;
            case 3:
                this.getLogger().severe(message);
                break;
        }
    }
}
