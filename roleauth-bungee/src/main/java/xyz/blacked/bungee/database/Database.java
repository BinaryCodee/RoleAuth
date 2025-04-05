package xyz.blacked.bungee.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.blacked.bungee.RoleLogin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class Database {

    private final RoleLogin plugin;
    private HikariDataSource dataSource;
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
     * Connect to SQLite database using HikariCP
     *
     * @return True if connection successful
     */
    public boolean connect() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(10);
            config.setConnectionTestQuery("SELECT 1");

            dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Database connection pool initialized!");

            try (Connection connection = dataSource.getConnection();
                 Statement stmt = connection.createStatement()) {
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
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

    /**
     * Execute SQL update
     *
     * @param sql SQL query
     */
    public void execute(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
            Connection connection = dataSource.getConnection();
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
            Connection connection = dataSource.getConnection();
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
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get connection from pool", e);
            return null;
        }
    }
}