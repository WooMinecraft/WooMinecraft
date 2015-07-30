package com.plugish.WooMinecraft.Commands;

import com.plugish.WooMinecraft.WooMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * Created by ethan on 7/30/2015.
 */
public class WooCommand implements CommandExecutor {

    private Logger log;

    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
        Player player = (Player) sender;
        if(cmd.getName().equalsIgnoreCase("Woo")) {
            if(args.length == 0) {
                if(sender tanceof Player) {
                    player.sendMessage(ChatColor.BLUE + "[Woo]" + ChatColor.AQUA + " Available Commands: Woo [Register | Reload | Check]");
                } else {
                    log.info("[Woo] Available Commands: Woo [Register | Reload | Check]");
                }
            } else if(sender.hasPermission("woo.reload") || sender.isOp()) {
                if(args[0].equalsIgnoreCase("reload")) {
                    // This will be fixed up soon, This is only or temporary use
                    WooMinecraft.plugin.reloadConfig();
                    if(sender instanceof Player) {
                        player.sendMessage(ChatColor.BLUE + "[Woo]" + ChatColor.AQUA + " Config Reloaded!");
                    } else {
                        log.info("[Woo] Config Reloaded!");
                    }
                }
            } else if(sender.hasPermission("woo.register") || sender.isOp()) {
                if(args[0].equalsIgnoreCase("register")) {
                    SecureRandom random = new SecureRandom();
                    String key = "";
                    if(WooMinecraft.c.getString(WooMinecraft.urlPath+",key") == "") {
                        key = new BigInteger(130,random).toString(32);
                        WooMinecraft.c.set(WooMinecraft.urlPath+".key", key);
                        // this is gonna give an error ;)
                        saveConfig();
                    }
                }
            }
        }
        return true;
    }

}
