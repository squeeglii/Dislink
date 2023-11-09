package me.squeeglii.plugin.dislink;

import me.squeeglii.plugin.dislink.storage.LinkedAccount;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;


public class PlayerLifecycleListener implements Listener {

    @EventHandler
    public void handleCustomWhitelist(AsyncPlayerPreLoginEvent event) {
        String formattedKickMessage = "%sKick Test! %s%sNow in Bold!".formatted(
                ChatColor.RED, ChatColor.AQUA, ChatColor.BOLD
        );

        UUID accountId = event.getUniqueId();

        // If players is on the whitelist (even if it's turned off),
        // skip all verification on account linking.
        if(this.hasWhitelistBypass(accountId)) {
            LinkedAccount guestAcc = new LinkedAccount(null, accountId, "whitelist", true);
            return;
        }


        event.disallow(Result.KICK_WHITELIST, formattedKickMessage);
    }

    private boolean hasWhitelistBypass(UUID uuid) {
        Server server = Dislink.get().getServer();

        if(!server.hasWhitelist())
            return false;

        OfflinePlayer player = server.getOfflinePlayer(uuid);

        return server.getWhitelistedPlayers().contains(player);
    }

}
