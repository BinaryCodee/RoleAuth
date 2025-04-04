package xyz.blacked.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import xyz.blacked.bungee.api.SQLiteBridge;
import xyz.blacked.bungee.cmds.*;
import xyz.blacked.bungee.data.Database;
import xyz.blacked.bungee.data.DatabaseManager;
import xyz.blacked.bungee.listener.PlayerListener;
import xyz.blacked.bungee.manager.AuthenticationManager;
import xyz.blacked.bungee.manager.ChannelManager;
import xyz.blacked.bungee.utils.ConfigFiles;
import xyz.blacked.bungee.utils.Utils;

import java.util.logging.Level;

public class RoleLogin extends Plugin {

    private static RoleLogin instance;
    private ConfigFiles configFiles;
    private Configuration config;
    private Utils utils;
    private Database database;
    private DatabaseManager databaseManager;
    private AuthenticationManager authManager;
    private ChannelManager channelManager;
    private SQLiteBridge sqliteBridge;
    private boolean enabled = false;

    @Override
    public void onEnable() {
        try {
            instance = this;
            configFiles = new ConfigFiles(this);
            config = configFiles.getConfig();
            utils = new Utils(this);
            registerEssentialCommands();
            if (!initSQLite()) return;
            if (!initDatabase()) return;
            initManagers();
            registerListeners();
            databaseManager.setupTables();
            enabled = true;
            getLogger().info("§aRoleAuth enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§cCritical error during initialization", e);
            enabled = false;
        }
    }

    private void registerEssentialCommands() {
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        getProxy().getPluginManager().registerCommand(this, new LoginCMD(this));
        getProxy().getPluginManager().registerCommand(this, new RegisterCMD(this));
        getProxy().getPluginManager().registerCommand(this, new PremiumCMD(this));
        getProxy().getPluginManager().registerCommand(this, new CrackedCMD(this));
        getProxy().getPluginManager().registerCommand(this, new ChangePasswordCMD(this));
        getProxy().getPluginManager().registerCommand(this, new RoleAuthCMD(this));
        getProxy().getPluginManager().registerCommand(this, new RoleAuthAdminCMD(this));
        System.out.println("Commands Registered!");
    }

    private boolean initSQLite() {
        try {
            sqliteBridge = new SQLiteBridge(this);
            System.out.println("Initializing SQLite!");
            if (!sqliteBridge.initialize()) {
                getLogger().log(Level.SEVERE, "§cFailed to initialize SQLite bridge");
                return false;
            }
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§cSQLite initialization failed", e);
            return false;
        }
    }

    private boolean initDatabase() {
        try {
            database = new Database(this);
            if (!database.connect()) {
                getLogger().log(Level.SEVERE, "§cDatabase connection failed");
                return false;
            }
            databaseManager = new DatabaseManager(this);
            System.out.println("DatabaseManager initialized!");
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§cDatabase initialization failed", e);
            return false;
        }
    }

    private void initManagers() {
        try {
            channelManager = new ChannelManager(this);
            authManager = new AuthenticationManager(this);
            System.out.println("Managers initialized!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§cManager initialization failed", e);
        }
    }

    private void registerListeners() {
        try {
            ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerListener(this));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§cFailed to register listeners", e);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (database != null) {
                database.disconnect();
            }
            getLogger().info("§cRoleAuth disabled");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§cError during shutdown", e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static RoleLogin getInstance() { return instance; }
    public ConfigFiles getFiles() { return configFiles; }
    public Configuration getConfiguration() { return config; }
    public Utils getUtils() { return utils; }
    public Database getDatabase() { return database; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public AuthenticationManager getAuthManager() { return authManager; }
    public ChannelManager getChannelManager() { return channelManager; }
    public SQLiteBridge getSqliteBridge() { return sqliteBridge; }
}