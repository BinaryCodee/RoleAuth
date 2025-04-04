package xyz.blacked.bungee.utils;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class ConfigFiles {

    private final Plugin plugin;
    private Configuration config;

    public ConfigFiles(Plugin plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    /**
     * Setup plugin files
     */
    private void setupFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
    
        File configFile = new File(plugin.getDataFolder(), "config.yml");
    
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                java.nio.file.Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create config.yml", e);
            }
        }
    
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load config.yml", e);
        }

        File dbDir = new File(plugin.getDataFolder(), "database");
        if (!dbDir.exists()) {
            dbDir.mkdir();
        }
    }

    /**
     * Save configuration to file
     */
    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    /**
     * Reload configuration from file
     */
    public void reloadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not reload config.yml", e);
        }
    }

    /**
     * Get configuration
     *
     * @return Configuration
     */
    public Configuration getConfig() {
        return config;
    }
}