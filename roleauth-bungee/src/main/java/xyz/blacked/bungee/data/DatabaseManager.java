package xyz.blacked.bungee.data;

import xyz.blacked.bungee.RoleLogin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final RoleLogin plugin;

    public DatabaseManager(RoleLogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Setup database tables
     */
    public void setupTables() {
        String playerTable = "CREATE TABLE IF NOT EXISTS players (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid VARCHAR(36) NOT NULL UNIQUE, " +
                "username VARCHAR(16) NOT NULL, " +
                "password TEXT, " +
                "ip VARCHAR(45), " +
                "last_login BIGINT, " +
                "premium BOOLEAN DEFAULT 0, " +
                "registered BOOLEAN DEFAULT 0)";

        plugin.getDatabase().execute(playerTable);
    }

    /**
     * Check if player exists in database
     *
     * @param uuid Player UUID
     * @return True if player exists
     */
    public boolean playerExists(UUID uuid) {
        try (ResultSet rs = plugin.getDatabase().query("SELECT uuid FROM players WHERE uuid = ?", uuid.toString())) {
            return rs != null && rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player exists", e);
            return false;
        }
    }

    /**
     * Check if username exists in database
     *
     * @param username Player username
     * @return True if username exists
     */
    public boolean usernameExists(String username) {
        try (ResultSet rs = plugin.getDatabase().query("SELECT username FROM players WHERE LOWER(username) = LOWER(?)", username)) {
            return rs != null && rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if username exists", e);
            return false;
        }
    }

    /**
     * Create player in database
     *
     * @param uuid Player UUID
     * @param username Player username
     * @param password Player password (hashed)
     * @param ip Player IP
     * @param isPremium Whether player is premium
     */
    public void createPlayer(UUID uuid, String username, String password, String ip, boolean isPremium) {
        plugin.getDatabase().execute(
                "INSERT INTO players (uuid, username, password, ip, last_login, premium, registered) VALUES (?, ?, ?, ?, ?, ?, ?)",
                uuid.toString(), username, password, ip, System.currentTimeMillis(), isPremium, password != null
        );
    }

    /**
     * Update player in database
     *
     * @param uuid Player UUID
     * @param username Player username
     * @param password Player password (hashed)
     * @param ip Player IP
     */
    public void updatePlayer(UUID uuid, String username, String password, String ip) {
        plugin.getDatabase().execute(
                "UPDATE players SET username = ?, password = ?, ip = ?, last_login = ?, registered = ? WHERE uuid = ?",
                username, password, ip, System.currentTimeMillis(), password != null, uuid.toString()
        );
    }

    /**
     * Update player login time and IP
     *
     * @param uuid Player UUID
     * @param ip Player IP
     */
    public void updateLoginData(UUID uuid, String ip) {
        plugin.getDatabase().execute(
                "UPDATE players SET ip = ?, last_login = ? WHERE uuid = ?",
                ip, System.currentTimeMillis(), uuid.toString()
        );
    }

    /**
     * Set player premium status
     *
     * @param uuid Player UUID
     * @param premium Whether player is premium
     */
    public void setPremium(UUID uuid, boolean premium) {
        plugin.getDatabase().execute("UPDATE players SET premium = ? WHERE uuid = ?", premium, uuid.toString());
    }

    /**
     * Update player password
     *
     * @param uuid Player UUID
     * @param password New password (hashed)
     */
    public void updatePassword(UUID uuid, String password) {
        plugin.getDatabase().execute(
                "UPDATE players SET password = ?, registered = ? WHERE uuid = ?",
                password, true, uuid.toString()
        );
    }

    /**
     * Check if player is premium
     *
     * @param uuid Player UUID
     * @return True if player is premium
     */
    public boolean isPremium(UUID uuid) {
        try (ResultSet rs = plugin.getDatabase().query("SELECT premium FROM players WHERE uuid = ?", uuid.toString())) {
            return rs != null && rs.next() && rs.getBoolean("premium");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player is premium", e);
            return false;
        }
    }

    /**
     * Check if player is registered
     *
     * @param uuid Player UUID
     * @return True if player is registered
     */
    public boolean isRegistered(UUID uuid) {
        try (ResultSet rs = plugin.getDatabase().query("SELECT registered FROM players WHERE uuid = ?", uuid.toString())) {
            return rs != null && rs.next() && rs.getBoolean("registered");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player is registered", e);
            return false;
        }
    }

    /**
     * Get player password hash
     *
     * @param uuid Player UUID
     * @return Password hash or null if not found
     */
    public String getPassword(UUID uuid) {
        try (ResultSet rs = plugin.getDatabase().query("SELECT password FROM players WHERE uuid = ?", uuid.toString())) {
            if (rs != null && rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player password", e);
        }
        return null;
    }

    /**
     * Reset player credentials
     *
     * @param uuid Player UUID
     */
    public void resetCredentials(UUID uuid) {
        plugin.getDatabase().execute(
                "UPDATE players SET password = NULL, registered = 0 WHERE uuid = ?",
                uuid.toString()
        );
    }
}