package xyz.blacked.bungee.tasks;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.blacked.bungee.RoleLogin;

import java.util.concurrent.TimeUnit;

public class CrackedTask {

    private final RoleLogin plugin;
    private final ProxiedPlayer player;
    private int taskId = -1;

    public CrackedTask(RoleLogin plugin, ProxiedPlayer player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Run task periodically
     *
     * @param delaySeconds Delay in seconds
     */
    public void runTaskTimer(int delaySeconds) {
        taskId = plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (player.isConnected() && plugin.getUtils().isAuthenticating(player.getUniqueId())) {
                if (plugin.getDatabaseManager().isRegistered(player.getUniqueId())) {
                    plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("login-reminder"));
                } else {
                    plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("register-reminder"));
                }
            } else {
                cancel();
            }
        }, delaySeconds, delaySeconds, TimeUnit.SECONDS).getId();
    }

    /**
     * Cancel task
     */
    public void cancel() {
        if (taskId != -1) {
            plugin.getProxy().getScheduler().cancel(taskId);
            taskId = -1;
        }
    }
}