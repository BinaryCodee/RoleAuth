package xyz.blacked.bungee.checks;

import xyz.blacked.bungee.RoleLogin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MojangCheck {
    private final RoleLogin plugin;
    private final Map<String, Boolean> premiumCache = new HashMap<>();
    private final Map<String, Long> cacheTime = new HashMap<>();

    public MojangCheck(RoleLogin plugin) {
        this.plugin = plugin;
        plugin.getProxy().getScheduler().schedule(plugin, this::cleanupCache, 30, 30, TimeUnit.MINUTES);
    }

    /**
     * Check if a player is premium via Mojang API
     *
     * @param username Player username
     * @return True if player is premium
     */
    public boolean checkPremium(String username) {
        int cacheTime = plugin.getConfiguration().getInt("settings.premium-cache-time", 60);
        if (premiumCache.containsKey(username.toLowerCase()) &&
                System.currentTimeMillis() - this.cacheTime.getOrDefault(username.toLowerCase(), 0L) < TimeUnit.MINUTES.toMillis(cacheTime)) {
            return premiumCache.get(username.toLowerCase());
        }

        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            boolean isPremium = responseCode == 200;
            premiumCache.put(username.toLowerCase(), isPremium);
            this.cacheTime.put(username.toLowerCase(), System.currentTimeMillis());
            return isPremium;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not check if player is premium", e);
            return false;
        }
    }

    /**
     * Get UUID for premium username
     *
     * @param username Player username
     * @return UUID or null if not premium
     */
    public UUID getPremiumUUID(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                String responseStr = response.toString();
                if (responseStr.contains("\"id\":\"")) {
                    String uuidStr = responseStr.split("\"id\":\"")[1].split("\"")[0];
                    uuidStr = uuidStr.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
                    return UUID.fromString(uuidStr);
                }
            }

            return null;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not get premium UUID", e);
            return null;
        }
    }

    /**
     * Cleanup premium cache
     */
    private void cleanupCache() {
        int cacheTime = plugin.getConfiguration().getInt("settings.premium-cache-time", 60);
        long expiryTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(cacheTime);
        this.cacheTime.entrySet().removeIf(entry -> entry.getValue() < expiryTime);
        premiumCache.keySet().removeIf(key -> !this.cacheTime.containsKey(key));
    }
}