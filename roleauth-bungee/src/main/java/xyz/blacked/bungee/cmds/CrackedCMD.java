package xyz.blacked.bungee.cmds;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.blacked.bungee.RoleLogin;

import java.util.UUID;

public class CrackedCMD extends Command {

    private final RoleLogin plugin;

    public CrackedCMD(RoleLogin plugin) {
        super("cracked", "");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getMessage("player-only")));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        UUID uuid = player.getUniqueId();

        if (!plugin.getDatabaseManager().isPremium(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("already-cracked"));
            return;
        }

        plugin.getAuthManager().setPremium(player, false);
        plugin.getUtils().addAuthenticatingPlayer(uuid);

        if (plugin.getDatabaseManager().isRegistered(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("login-required"));
        } else {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("register-required"));
        }
    }
}