package com.plugish.WooMinecraft;

import com.plugish.WooMinecraft.Commands.WooCommand;
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
            if (!plugin.englishFile.exists()) {
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
            plugin.english.load(plugin.englishFile);
            updateconfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveYamls() {
        try {
            plugin.config.save(plugin.configFile);
            plugin.english.save(plugin.englishFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateconfig()
    {
        addDefault(plugin.config, "WooMinecraft.messages.file", "english");
        addDefault(plugin.config, "WooMinecraft.web.time_delay", 1500);
        addDefault(plugin.config, "WooMinecraft.web.url", "www.example.com");
        addDefault(plugin.config, "WooMinecraft.web.key", "changeme");

        addDefault(plugin.english, "NoPerms", "&cYou do not have permissions to do this!");
        addDefault(plugin.english, "Reload", "&5[&fWoo&5] reloaded!");
    }

    private static void addDefault(FileConfiguration f, String path, Object v)
    {
        if(f.getString(path) == null)
        {
            f.set(path, v);
        }
    }
}
