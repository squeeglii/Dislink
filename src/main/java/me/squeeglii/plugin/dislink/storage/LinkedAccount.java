package me.squeeglii.plugin.dislink.storage;

import me.squeeglii.plugin.dislink.Dislink;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

// isGuest = is not actually linked. Allowed on to the server by other means.
public record LinkedAccount(String discordId, UUID minecraftId, String verifier, boolean isGuest) {

    public OfflinePlayer getPlayer() {
        return Dislink.plugin().getServer().getOfflinePlayer(this.minecraftId);
    }

    public Optional<Player> getOnlinePlayer() {
        Player p = Dislink.plugin().getServer().getPlayer(this.minecraftId);
        return Optional.ofNullable(p);
    }

}
