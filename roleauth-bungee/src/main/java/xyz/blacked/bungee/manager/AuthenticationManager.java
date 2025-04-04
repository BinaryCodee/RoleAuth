package xyz.blacked.bungee.manager;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.mindrot.jbcrypt.BCrypt;
import xyz.blacked.bungee.RoleLogin;
import xyz.blacked.bungee.checks.IsCrackedPremiumCheck;
import xyz.blacked.bungee.checks.MojangCheck;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthenticationManager {
    private final RoleLogin plugin;
    private final Map<UUID, Integer> loginAttempts = new HashMap<>();
    private final MojangCheck mojangCheck;
    private final IsCrackedPremiumCheck crackedPremiumCheck;
    private final Argon2 argon2;
    private final boolean argon2Available;

    public AuthenticationManager(RoleLogin plugin) {
        this.plugin = plugin;
        this.mojangCheck = new MojangCheck(plugin);
        this.crackedPremiumCheck = new IsCrackedPremiumCheck(plugin);
        Argon2 tempArgon = null;
        boolean argonOk = false;
        try {
            tempArgon = Argon2Factory.create();
            tempArgon.hash(1, 1024, 1, "test".toCharArray());
            argonOk = true;
        } catch (Throwable e) {
            plugin.getLogger().severe("Argon2 initialization failed: " + e.toString());
        }
        this.argon2 = tempArgon;
        this.argon2Available = argonOk;
    }

    /**
     * Authenticate a player (login or register)
     *
     * @param player Player to authenticate
     * @param password Password
     * @param isRegistering Whether player is registering
     * @return True if authentication successful
     */
    public boolean authenticate(ProxiedPlayer player, String password, boolean isRegistering) {
        UUID uuid = player.getUniqueId();

        if (isRegistering) {
            if (plugin.getDatabaseManager().isRegistered(uuid)) {
                plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("already-registered"));
                return false;
            }

            String hashedPassword = hashPassword(password);
            if (plugin.getDatabaseManager().playerExists(uuid)) {
                plugin.getDatabaseManager().updatePassword(uuid, hashedPassword);
            } else {
                plugin.getDatabaseManager().createPlayer(
                        uuid,
                        player.getName(),
                        hashedPassword,
                        player.getAddress().getAddress().getHostAddress(),
                        false
                );
            }

            plugin.getUtils().removeAuthenticatingPlayer(uuid);
            loginAttempts.remove(uuid);
            plugin.getChannelManager().setPlayerBlocked(player, false);
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("register-success"));
            return true;
        } else {
            if (!plugin.getDatabaseManager().isRegistered(uuid)) {
                plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("not-registered"));
                return false;
            }

            String hashedPassword = plugin.getDatabaseManager().getPassword(uuid);
            if (hashedPassword == null) {
                plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("error-retrieving-password"));
                return false;
            }

            if (!verifyPassword(password, hashedPassword)) {
                int attempts = loginAttempts.getOrDefault(uuid, 0) + 1;
                loginAttempts.put(uuid, attempts);

                int maxAttempts = plugin.getConfiguration().getInt("settings.max-login-attempts", 5);
                if (attempts >= maxAttempts) {
                    player.disconnect(plugin.getUtils().colorize(plugin.getUtils().getMessage("too-many-attempts")));
                    loginAttempts.remove(uuid);
                    return false;
                }

                plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("wrong-password")
                        .replace("%attempts%", String.valueOf(attempts))
                        .replace("%max_attempts%", String.valueOf(maxAttempts)));
                return false;
            }

            plugin.getDatabaseManager().updateLoginData(uuid, player.getAddress().getAddress().getHostAddress());
            plugin.getUtils().removeAuthenticatingPlayer(uuid);
            loginAttempts.remove(uuid);
            plugin.getChannelManager().setPlayerBlocked(player, false);
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("login-success"));
            return true;
        }
    }

    /**
     * Change player password
     *
     * @param player Player
     * @param oldPassword Old password
     * @param newPassword New password
     * @return True if password changed successfully
     */
    public boolean changePassword(ProxiedPlayer player, String oldPassword, String newPassword) {
        UUID uuid = player.getUniqueId();

        if (!plugin.getDatabaseManager().isRegistered(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("not-registered"));
            return false;
        }

        String hashedPassword = plugin.getDatabaseManager().getPassword(uuid);
        if (hashedPassword == null) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("error-retrieving-password"));
            return false;
        }

        if (!verifyPassword(oldPassword, hashedPassword)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("wrong-password-change"));
            return false;
        }

        String newHashedPassword = hashPassword(newPassword);
        plugin.getDatabaseManager().updatePassword(uuid, newHashedPassword);
        plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("password-changed"));
        return true;
    }

    /**
     * Set player premium status
     *
     * @param player Player
     * @param premium Whether player is premium
     */
    public void setPremium(ProxiedPlayer player, boolean premium) {
        UUID uuid = player.getUniqueId();
        plugin.getDatabaseManager().setPremium(uuid, premium);

        if (premium) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("premium-set"));
        } else {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("cracked-set"));
        }
    }

    /**
     * Check if player is premium via Mojang API
     *
     * @param player Player to check
     * @param callback Callback with result
     */
    public void checkPremium(ProxiedPlayer player, PremiumCheckCallback callback) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            boolean isPremium = mojangCheck.checkPremium(player.getName());
            callback.onResult(isPremium);
        });
    }

    /**
     * Auto-login premium player
     *
     * @param player Player to auto-login
     */
    public void autologinPremium(ProxiedPlayer player) {
        UUID uuid = player.getUniqueId();

        if (!plugin.getDatabaseManager().playerExists(uuid)) {
            plugin.getDatabaseManager().createPlayer(
                    uuid,
                    player.getName(),
                    null,
                    player.getAddress().getAddress().getHostAddress(),
                    true
            );
        } else {
            plugin.getDatabaseManager().updateLoginData(uuid, player.getAddress().getAddress().getHostAddress());
        }

        plugin.getChannelManager().setPlayerBlocked(player, false);
        plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("auto-login-premium"));
    }

    /**
     * Force login a player
     *
     * @param player Player to force login
     */
    public void forceLogin(ProxiedPlayer player) {
        UUID uuid = player.getUniqueId();

        if (!plugin.getDatabaseManager().playerExists(uuid)) {
            plugin.getDatabaseManager().createPlayer(
                    uuid,
                    player.getName(),
                    null,
                    player.getAddress().getAddress().getHostAddress(),
                    false
            );
        } else {
            plugin.getDatabaseManager().updateLoginData(uuid, player.getAddress().getAddress().getHostAddress());
        }

        plugin.getUtils().removeAuthenticatingPlayer(uuid);
        loginAttempts.remove(uuid);
        plugin.getChannelManager().setPlayerBlocked(player, false);
        plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("force-login"));
    }

    /**
     * Force register a player
     *
     * @param player Player to force register
     * @param password Password to set
     */
    public void forceRegister(ProxiedPlayer player, String password) {
        UUID uuid = player.getUniqueId();
        String hashedPassword = hashPassword(password);

        if (plugin.getDatabaseManager().playerExists(uuid)) {
            plugin.getDatabaseManager().updatePassword(uuid, hashedPassword);
        } else {
            plugin.getDatabaseManager().createPlayer(
                    uuid,
                    player.getName(),
                    hashedPassword,
                    player.getAddress().getAddress().getHostAddress(),
                    false
            );
        }

        plugin.getUtils().removeAuthenticatingPlayer(uuid);
        loginAttempts.remove(uuid);
        plugin.getChannelManager().setPlayerBlocked(player, false);
        plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("force-register"));
    }

    /**
     * Reset player credentials
     *
     * @param uuid Player UUID
     */
    public void resetCredentials(UUID uuid) {
        plugin.getDatabaseManager().resetCredentials(uuid);
    }

    /**
     * Hash a password using configured method
     *
     * @param password Password to hash
     * @return Hashed password
     */
    private String hashPassword(String password) {
        String hashMethod = plugin.getConfiguration().getString("settings.hash-method", "bcrypt").toLowerCase();

        if ("argon2".equals(hashMethod) && argon2Available) {
            char[] passwordChars = password.toCharArray();
            try {
                return argon2.hash(10, 65536, 1, passwordChars);
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verify a password against a hash
     *
     * @param password Password to verify
     * @param hash Hash to verify against
     * @return True if password matches hash
     */
    private boolean verifyPassword(String password, String hash) {
        if (hash.startsWith("$argon2") && argon2Available) {
            char[] passwordChars = password.toCharArray();
            try {
                return argon2.verify(hash, passwordChars);
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
        }
        return BCrypt.checkpw(password, hash);
    }

    /**
     * Interface for premium check callback
     */
    public interface PremiumCheckCallback {
        void onResult(boolean isPremium);
    }
}