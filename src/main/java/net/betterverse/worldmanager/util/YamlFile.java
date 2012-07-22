package net.betterverse.worldmanager.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import net.betterverse.worldmanager.WorldManager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlFile {
    private final File file;
    private final FileConfiguration config;

    public YamlFile(WorldManager plugin, File file, String name) {
        this.file = file;
        config = new YamlConfiguration();
        name += ".yml";

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
                plugin.log("Created new file '" + name + "'.");
            } catch (IOException e) {
                plugin.log(Level.SEVERE, "Could not create file '" + name + "'!");
                e.printStackTrace();
            }
        }

        load();
    }

    public boolean containsKey(String key) {
        return config.contains(key);
    }

    public double getDouble(String key) {
        return config.getDouble(key);
    }

    public Set<String> getKeys(String key) {
        return config.getConfigurationSection(key).getKeys(false);
    }

    public long getLong(String key) {
        return config.getLong(key);
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public void load() {
        try {
            config.load(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void set(String key, Object value) {
        config.set(key, value);
    }
}
