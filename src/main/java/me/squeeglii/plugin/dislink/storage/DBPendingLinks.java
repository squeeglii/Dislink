package me.squeeglii.plugin.dislink.storage;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.util.Run;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseHelper;

import java.sql.PreparedStatement;
import java.util.concurrent.CompletableFuture;

public class DBPendingLinks {

    public static final String SQL_CLEAR_ALL = "TRUNCATE TABLE PendingLinks;";


    /**
     * Asynchronously begins the linking process for a given Minecraft account. If
     * @param platformId the id of the account (whatever is primarily used to identify it)
     * @param isJavaAccount is the account native to Java Edition, or is it Bedrock?
     * @return a link code to enter via the discord command to complete the link.
     */
    public static CompletableFuture<String> startLinkingFor(String platformId, boolean isJavaAccount) {
        CompletableFuture<String> output = new CompletableFuture<>();



        return output;
    }

    /**
     * Asynchronously checks if the link code provided matches with a pending link entry and
     * triggers the completion of the linking process if the codes match.
     * @param discordId
     * @param accountId the id of the account (whatever is primarily used to identify it)
     * @param isJavaAccount is the account native to Java Edition, or is it Bedrock?
     * @return a link code to enter via the discord command to complete the link.
     */
    public static CompletableFuture<LinkResult> tryLink(String discordId, String accountId, boolean isJavaAccount, String pairCode) {
        return null;
    }


    public static CompletableFuture<Void> clearPendingLinks() {
        CompletableFuture<Void> output = new CompletableFuture<>();

        Run.async(() -> {
            ConnectionWrapper connection = null;
            PreparedStatement statement = null;

            try {
                connection = Dislink.get().getDbConnection();
                statement = connection.prepareStatement(SQL_CLEAR_ALL);

                statement.execute();

            } catch (Exception err) {
                output.completeExceptionally(err);
                return;

            } finally {
                DatabaseHelper.closeQuietly(statement);
                DatabaseHelper.closeQuietly(connection);
            }

            output.complete(null);
        });

        return output;
    }

}
