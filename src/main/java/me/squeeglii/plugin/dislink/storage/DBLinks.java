package me.squeeglii.plugin.dislink.storage;

import java.util.concurrent.CompletableFuture;

public class DBLinks {


    public CompletableFuture<Integer> getExistingAccountQuantityFor(String discordId) {
        return null;
    }

    public CompletableFuture<Boolean> isAccountLinked(String platformId, boolean isJavaAccount) {
        return null;
    }

    /**
     * Links a Discord account with a Minecraft account without checking
     * @param discordId
     * @param platformId
     * @param isJavaAccount
     * @return
     */
    public CompletableFuture<Boolean> createLinkBetween(String discordId, String platformId, boolean isJavaAccount) {
        return null;
    }

}
