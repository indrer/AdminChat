package com.rogue.adminchat.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Handles config loading.
 *
 * @author MD678685
 * @version 1.5.0
 * @since 1.5.0
 */
public class Configuration {

    private Plugin plugin;
    private String fileName;
    private String defaultFileName;
    private YamlConfiguration yamlConfig = null;

    public Configuration(Plugin plugin) throws IOException {
        this(plugin, "config.yml");
    }

    public Configuration(Plugin plugin, String fileName) throws IOException {
        this(plugin, fileName, false);
    }

    public Configuration(Plugin plugin, String fileName, boolean autoLoad) throws IOException {
        this(plugin, fileName, fileName, autoLoad);
    }

    public Configuration(Plugin plugin, String fileName, String defaultFileName, boolean autoLoad) throws IOException {
        this.plugin = plugin;
        this.fileName = fileName;
        this.defaultFileName = defaultFileName;
        if (autoLoad) this.loadConfig();
    }

    public void loadConfig() throws IOException {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!(file.exists())) {
            yamlConfig = YamlConfiguration.loadConfiguration(plugin.getResource(fileName));
            saveConfig();
        } else {
            yamlConfig = YamlConfiguration.loadConfiguration(file);
        }
    }

    public synchronized boolean updateConfig() throws IOException {
        if (yamlConfig == null) this.loadConfig();
        boolean changed = false;
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(plugin.getResource(defaultFileName));
        Map<String, Object> values = defaultConfig.getValues(true);
        for (String key : values.keySet()) {
            if (!(yamlConfig.contains(key))) {
                yamlConfig.set(key, values.get(key));
                changed = true;
            }
        }
        return changed;
    }

    public synchronized void saveConfig() throws IOException {
        if (yamlConfig == null) return;
        yamlConfig.save(fileName);
    }

    public synchronized YamlConfiguration getConfig() {
        try {
            if (yamlConfig == null) this.loadConfig();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get the requested config: " + fileName, e);
        }
        return yamlConfig;
    }

}
