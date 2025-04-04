package xyz.blacked.bungee.data;

import xyz.blacked.bungee.RoleLogin;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public class Database {

    private final RoleLogin plugin;
    private Connection connection;
    private final String dbPath;

    public Database(RoleLogin plugin) {
        this.plugin = plugin;
        File dbDir = new File(plugin.getDataFolder(), "database");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        this.dbPath = new File(dbDir, "roleauth.db").getAbsolutePath();
    }

    /**
     * Connect to SQLite database
     *
     * @return True if connection successful
     */
    public boolean connect() {
        try {
            if (!plugin.getSqliteBridge().isDriverLoaded()) {
                plugin.getLogger().severe("SQLite JDBC driver not loaded by bridge");
                return false;
            }

            String jdbcUrl = "jdbc:sqlite:" + dbPath;
            plugin.getLogger().info("Connecting to database at: " + jdbcUrl);
            connection = DriverManager.getConnection(jdbcUrl);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SELECT 1");
                plugin.getLogger().info("Database connection test successful!");
            }

            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQL error while connecting to database at " + dbPath, e);
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unexpected error connecting to database", e);
            return false;
        }
    }

    /**
     * Disconnect from SQLite database
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not disconnect from SQLite database", e);
        }
    }

    /**
     * Execute SQL update
     *
     * @param sql SQL query
     */
    public void execute(String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not execute SQL: " + sql, e);
        }
    }

    /**
     * Execute SQL update with parameters
     *
     * @param sql SQL query
     * @param params Parameters
     */
    public void execute(String sql, Object... params) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.execute();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not execute SQL: " + sql, e);
        }
    }

    /**
     * Execute SQL query
     *
     * @param sql SQL query
     * @return ResultSet of query
     */
    public ResultSet query(String sql) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            return statement.executeQuery();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not execute SQL query: " + sql, e);
            return null;
        }
    }

    /**
     * Execute SQL query with parameters
     *
     * @param sql SQL query
     * @param params Parameters
     * @return ResultSet of query
     */
    public ResultSet query(String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not execute SQL query: " + sql, e);
            return null;
        }
    }

    /**
     * Get database connection
     *
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }
}