/*
 * Woo Minecraft Donation plugin
 * Author:	Jerry Wood
 * Author URI: http://plugish.com
 * License:		Commercial
 * 
 * Copyright 2014 All rights Reserved
 * 
 * This is a commercially licensed product, if you downloaded this for free, please contact me via my
 * web site contact form, you will be rewarded.
 */
package com.plugish.WooMinecraft;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.JSONArray;
import org.json.JSONObject;

public final class WooMinecraft extends JavaPlugin {
	
	private static WooMinecraft plugin;
	private static String configPath = "WooMinecraft";
	private static String urlPath = configPath+".web";
	
	private BukkitRunner runnerNew;
	private FileConfiguration c;
	
	@Override
	public boolean onCommand(CommandSender s, Command command, String label, String[] args){
		if(command.getName().equalsIgnoreCase("woo")){
			if(args.length < 1) return false;
			
			if(s.hasPermission("woo.admin") || s.isOp()){
				if(args[0].equalsIgnoreCase("reload")){
					plugin.reloadConfig();
					s.sendMessage(ChatColor.RED+"Config Reloaded");
					return true;
				}
				if(args[0].equalsIgnoreCase("register")){
					SecureRandom random = new SecureRandom();
					String key = "";
					if(c.getString(urlPath+".key") == ""){
						key = new BigInteger(130,random).toString(32);
						c.set(urlPath+".key", key);
						saveConfig();
					}else{
						key = c.getString(urlPath+".key");
					}
					s.sendMessage(ChatColor.AQUA+"[WOO]"+ChatColor.RED+" KEY: "+key);
					s.sendMessage(ChatColor.RED+"Copy this key and put it in your WooMinecraft options panel in WordPress");
					return true;
				}
				if(args[0].equalsIgnoreCase("check")){
					// well we need to run the check then don't we?
					check();
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void onEnable(){
		plugin = this;
		c = getConfig();
		initalizePlugin();
		
		this.runnerNew = new BukkitRunner(plugin);
		this.runnerNew.runTaskTimerAsynchronously(plugin, c.getInt(urlPath+".time_delay") * 20, c.getInt(urlPath+".time_delay") * 20);
	}
	
	@Override
	public void onDisable(){
		saveConfig();
		getLogger().info("Stopping plugin.");
	}
	
	public boolean check(){

		ArrayList<Integer> rowUpdates = new ArrayList<Integer>();		
		try{
			String sPath = c.getString(urlPath+".url");
			String key = c.getString(urlPath+".key");
			URL url = new URL(sPath+"?woo_minecraft=check&key="+key);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			Player[] list = Bukkit.getOnlinePlayers();
			if(list.length < 1) return false;
			
			StringBuilder sb = new StringBuilder();
			for(Player player : Bukkit.getServer().getOnlinePlayers()) {
				sb.append(player.getName() + ", ");
			}
			String playerList = sb.toString();
			Pattern pattern = Pattern.compile(", $");
			Matcher matcher = pattern.matcher(playerList);
			playerList = matcher.replaceAll("");
			String urlParams = playerList;
			con.setDoInput(true);
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes("names="+urlParams);
			wr.flush();
			wr.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			// ENDNEW
			
			StringBuilder sb2 = new StringBuilder();
			String line;
			while((line = in.readLine()) != null){
				sb2.append(line);
			}
			in.close();
						
			JSONObject json = new JSONObject(sb2.toString());
			
			if(json.getString("status").equalsIgnoreCase("success")){
				JSONArray jsonArr = json.getJSONArray("data");
				for(int i = 0; i < jsonArr.length(); i++){
					JSONObject obj = jsonArr.getJSONObject(i);
					
					String playerName = obj.getString("player_name");
					String x = obj.getString("command");
					final String command = x.replace("%s", playerName);
					
					Player pN = Bukkit.getServer().getPlayer(playerName);
					
					if(x.substring(0, 3) == "give"){
						int count = 0;
						for(ItemStack iN : pN.getInventory()){
							if(iN==null)
								count++;
						}
						
						if(count == 0) return false;
					}
					
					int id = obj.getInt("id");
										
					BukkitScheduler sch = Bukkit.getServer().getScheduler();
					sch.scheduleSyncDelayedTask(plugin, new Runnable(){
						@Override
						public void run(){
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
						}
					}, 20L);
					rowUpdates.add(id);
				}
			}else{
				getLogger().info("Check: No donations for online users.");
			}
			remove(rowUpdates);
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	private void remove(ArrayList<Integer> ids){
		if(ids.isEmpty()) return;
		
		try{
			String sPath = c.getString(urlPath+".url");
			String key = c.getString(urlPath+".key");
			
			URL url = new URL(sPath+"?woo_minecraft=update&key="+key);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			String urlParams = StringUtils.join(ids, ',');
			con.setDoInput(true);
			con.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes("players="+urlParams);
			wr.flush();
			wr.close();
			
			BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response = input.readLine();
			if(!response.equalsIgnoreCase("true")){
				getLogger().severe("Could not update donations.");
				getLogger().info(response);		
			}else{
				getLogger().info("Donations updated");
			}
			input.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void initalizePlugin(){
		
		c.addDefault(urlPath+".time_delay", 1500);
		c.addDefault(urlPath+".url", "http://agedcraft.net/");
		
		c.options().copyDefaults(true);
		saveConfig();
		getLogger().info("Settings confirmed.");
	}
	
}
