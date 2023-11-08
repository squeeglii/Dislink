package me.squeeglii.plugin.dislink;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;


public class PlayerLifecycleListener implements Listener {

    @EventHandler
    public void handleCustomWhitelist(AsyncPlayerPreLoginEvent event) {
        String formattedKickMessage = "%sKick Test! %s%sNow in Bold!".formatted(
                ChatColor.RED, ChatColor.AQUA, ChatColor.BOLD
        );

        event.disallow(Result.KICK_WHITELIST, formattedKickMessage);
    }

}
