package me.squeeglii.plugin.dislink.storage;

import java.util.concurrent.CompletableFuture;

/**
 * Handles fully-formed account links & checking on login.
 */
public class DBLinks {

    /**
     * Checks how many accounts are already linked to a single discord account.
     * @param discordId the discord account id
     * @return completable future returned once complete - how many accounts were found to be fully linked.
     */
    public static CompletableFuture<Integer> getExistingAccountQuantityFor(String discordId) {
        return null;
    }

    /**
     * Checks if a given Minecraft account is linked to a Discord account.
     * @param platformId the minecraft account id (dependent on java / bedrock)
     * @param isJavaAccount is the minecraft account id for Java or Bedrock
     * @return completable future returned once complete - true if the link was successfully completed.
     */
    public static CompletableFuture<Boolean> isAccountLinked(String platformId, boolean isJavaAccount) {
        return null;
    }

    /**
     * Completes a link between a Discord account with a Minecraft account without checking if the pairing tokens match.
     * Clears any Pending Links for that account as well.
     * @param discordId the discord account id to pair with
     * @param platformId the minecraft account id (dependent on java / bedrock)
     * @param isJavaAccount is the minecraft account id for Java or Bedrock
     * @param verifier where did the player get verified from (admin, [discord server short name], DMs?)
     * @return completable future returned once complete - true if the link was successfully completed.
     */
    public static CompletableFuture<Boolean> createLinkBetween(String discordId, String platformId,
                                                               boolean isJavaAccount, String verifier) {
        return null;
    }

}
