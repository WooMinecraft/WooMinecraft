package com.plugish.WooMinecraft;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ethan on 8/23/2015.
 */
public class WooDefaults {
    public static WooMinecraft plugin = WooMinecraft.instance;

    public static void initDefaults() {
        try {
            if (!plugin.configFile.exists()) {
                plugin.configFile.getParentFile().mkdirs();
                plugin.configFile.createNewFile();
            }
            if (!plugin.messagesFile.exists()) {
                plugin.configFile.getParentFile();
                plugin.configFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadYamls() {
        try {
            plugin.config.load(plugin.configFile);
            plugin.messages.load(plugin.messagesFile);
            updateconfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveYamls() {
        try {
            plugin.config.save(plugin.configFile);
            plugin.messages.save(plugin.messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateconfig()
    {
        addDefault(plugin.config, "Woo", "Temp");

        addDefault(plugin.messages, "NoPermissions", "&cYou do not have permissions to do this!");
        addDefault(plugin.messages, "Reload", "&a[Woo] reloaded!");
    }

    private static void addDefault(FileConfiguration f, String path, Object v)
    {
        if(f.getString(path) == null)
        {
            f.set(path, v);
        }
    }
}
