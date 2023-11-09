package me.squeeglii.plugin.dislink.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class LinkedAccountCache implements Listener {

    private final HashMap<UUID, LinkedAccount> accountCache;

    public LinkedAccountCache() {
        this.accountCache = new HashMap<>();
    }

    private Optional<LinkedAccount> getAccount(UUID playerId) {
        LinkedAccount account = this.accountCache.get(playerId);

        if(account == null)
            return Optional.empty();

        return Optional.of(account);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        this.accountCache.remove(uuid);
    }

}
