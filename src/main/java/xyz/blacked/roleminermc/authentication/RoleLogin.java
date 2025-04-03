package xyz.blacked.roleminermc.authentication;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import xyz.blacked.api.SQLiteBridge;
import xyz.blacked.roleminermc.authentication.cmds.*;
import xyz.blacked.roleminermc.authentication.data.Database;
import xyz.blacked.roleminermc.authentication.listener.PlayerListener;
import xyz.blacked.roleminermc.authentication.manager.AuthenticationManager;
import xyz.blacked.roleminermc.authentication.data.DatabaseManager;
import xyz.blacked.roleminermc.authentication.utils.ConfigFiles;
import xyz.blacked.roleminermc.authentication.utils.Utils;

import java.util.logging.Level;

public class RoleLogin extends Plugin {

    private static RoleLogin instance;
    private ConfigFiles configFiles;
    private Configuration config;
    private Utils utils;
    private Database database;
    private DatabaseManager databaseManager;
    private AuthenticationManager authManager;
    private SQLiteBridge sqliteBridge;

    @Override
    public void onEnable() {
        instance = this;
        configFiles = new ConfigFiles(this);
        config = configFiles.getConfig();
        utils = new Utils(this);
        sqliteBridge = new SQLiteBridge(this);

        if (!sqliteBridge.initialize()) {
            getLogger().log(Level.SEVERE, "Failed to initialize SQLite bridge. Disabling plugin...");
            return;
        }

        database = new Database(this);
        if (!database.connect()) {
            getLogger().log(Level.SEVERE, "Failed to connect to database. Disabling plugin...");
            return;
        }

        databaseManager = new DatabaseManager(this);
        authManager = new AuthenticationManager(this);
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        getProxy().getPluginManager().registerCommand(this, new LoginCMD(this));
        getProxy().getPluginManager().registerCommand(this, new RegisterCMD(this));
        getProxy().getPluginManager().registerCommand(this, new PremiumCMD(this));
        getProxy().getPluginManager().registerCommand(this, new CrackedCMD(this));
        getProxy().getPluginManager().registerCommand(this, new ChangePasswordCMD(this));
        getProxy().getPluginManager().registerCommand(this, new RoleAuthCMD(this));
        getProxy().getPluginManager().registerCommand(this, new RoleAuthAdminCMD(this));
        databaseManager.setupTables();

        getLogger().info("§aRoleAuth has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.disconnect();
        }
        getLogger().info("§cRoleAuth has been disabled.");
    }

    public static RoleLogin getInstance() {
        return instance;
    }

    public ConfigFiles getFiles() {
        return configFiles;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public Utils getUtils() {
        return utils;
    }

    public Database getDatabase() {
        return database;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AuthenticationManager getAuthManager() {
        return authManager;
    }

    public SQLiteBridge getSqliteBridge() {
        return sqliteBridge;
    }
}