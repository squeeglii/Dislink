package me.squeeglii.plugin.dislink.storage;

import java.util.UUID;

// isGuest = is not actually linked. Allowed on to the server by other means.
public record LinkedAccount(String discordId, UUID minecraftId, String verifier, boolean isGuest) {

}
