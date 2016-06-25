package com.plugish.woominecraft.Gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.plugish.woominecraft.WooMinecraft;

public class GUI implements Listener {
	
	public static WooMinecraft plugin = WooMinecraft.instance;
	
	/**
	 * TODO: Make this use GET requests to the WooCommerce API.
	 * Just an example for now.
	 * @param player Player opening the GUI
	 */
	public static void openDonationGui(Player player) {
		
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Donation GUI");

		/*
		 * We have to get the number of products the server owner
		 * has setup then add a GUI item for every product. Preferably
		 * the server owner should be able to set the icon in a field
		 * when configuring the product. Not sure how to do this yet
		 * because I am still looking at the WooCommerce API.
		 */
		ItemStack tier1 = new ItemStack(Material.WOOD_SWORD);
		ItemMeta tier1Meta = tier1.getItemMeta();
		ItemStack tier2 = new ItemStack(Material.GOLD_SWORD);
		ItemMeta tier2Meta = tier2.getItemMeta();
		ItemStack tier3 = new ItemStack(Material.STONE_SWORD);
		ItemMeta tier3Meta = tier3.getItemMeta();

		/*
		 * We need to get the name of the product and set it to a string.
		 * We should be able to do this with a pretty extensive but clean
		 * for loop.
		 */
		tier1Meta.setDisplayName(ChatColor.BLUE + plugin.getConfig().getString("exampleProduct1"));
		tier1.setItemMeta(tier1Meta);
		tier2Meta.setDisplayName(ChatColor.BLUE + plugin.getConfig().getString("exampleProduct2"));
		tier2.setItemMeta(tier2Meta);
		tier3Meta.setDisplayName(ChatColor.BLUE + plugin.getConfig().getString("exampleProduct3"));
		tier3.setItemMeta(tier3Meta);

		inv.setItem(11, tier1);
		inv.setItem(12, tier2);
		inv.setItem(13, tier3);

		player.openInventory(inv);
	}
	
	/*
	 * This is the event handler for when they click an icon in the GUI
	 * this code would work if every single products name was defined in the
	 * config and ever product was hard coded in but we need to use the WooCommerce API.
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase("Donation GUI")) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		event.setCancelled(true);

		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR
				|| !event.getCurrentItem().hasItemMeta()) {
			player.closeInventory();
			return;
		}

		switch (event.getCurrentItem().getType()) {
		case WOOD_SWORD:
			player.sendMessage("You can download this product at " + plugin.getConfig().getString("url") + "/product/" 
		+ plugin.getConfig().getString("testProduct1").replace(" ", "-"));
			player.closeInventory();
			break;
		case GOLD_SWORD:
			// send link here.
			player.closeInventory();
			break;
		case STONE_SWORD:
			// send link here.
			player.closeInventory();
			break;
		default:
			player.closeInventory();
			break;
		}
	}
}
