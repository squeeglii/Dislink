package me.squeeglii.plugin.dislink.storage;

import java.util.concurrent.CompletableFuture;

public class DBPendingLinks {


    /**
     * Asynchronously begins the linking process for a given Minecraft account. If
     * @param platformId the id of the account (whatever is primarily used to identify it)
     * @param isJavaAccount is the account native to Java Edition, or is it Bedrock?
     * @return a link code to enter via the discord command to complete the link.
     */
    private CompletableFuture<String> startLinkingFor(String platformId, boolean isJavaAccount) {
        return null;
    }

    /**
     * Asynchronously checks if the link code provided matches with a pending link entry and
     * triggers the completion of the linking process if the codes match.
     * @param discordId
     * @param accountId the id of the account (whatever is primarily used to identify it)
     * @param isJavaAccount is the account native to Java Edition, or is it Bedrock?
     * @return a link code to enter via the discord command to complete the link.
     */
    private CompletableFuture<LinkResult> tryLink(String discordId, String accountId, boolean isJavaAccount, String pairCode) {
        return null;
    }

}
