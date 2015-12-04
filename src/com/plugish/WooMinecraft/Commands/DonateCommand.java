package com.plugish.WooMinecraft.Commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.plugish.WooMinecraft.WooMinecraft;

public class DonateCommand implements CommandExecutor {
	
	public static WooMinecraft plugin = WooMinecraft.instance;

	public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("donate")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command!");
			} else {
				Player player = (Player) sender;
				
				Inventory inv = plugin.getServer().createInventory(null, 9, "WooMinecraft Store");
				inv.setItem(1, new ItemStack(Material.GOLD_NUGGET));
				
			    player.openInventory(inv);
			}
		}
		return true;
	}

}
