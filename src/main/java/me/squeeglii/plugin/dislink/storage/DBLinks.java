package me.squeeglii.plugin.dislink.storage;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.exception.MissedFetchException;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseHelper;
import me.squeeglii.plugin.dislink.util.OptionalFuture;
import me.squeeglii.plugin.dislink.util.Run;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles fully-formed account links & checking on login.
 */
public class DBLinks {

    private static final String SQL_GET_PAIRED_ACCOUNT_QUANTITY = "SELECT COUNT(*) FROM UserLinks WHERE discord_id=?;";

    /**
     * Checks how many accounts are already linked to a single discord account.
     * @param discordId the discord account id
     * @return completable future returned once complete - how many accounts were found to be fully linked.
     */
    public static CompletableFuture<Integer> getExistingAccountQuantityFor(String discordId) {
        CompletableFuture<Integer> output = new CompletableFuture<>();

        Run.async(() -> {
            ConnectionWrapper connection = null;
            PreparedStatement statement = null;

            try {
                connection = Dislink.get().getDbConnection();
                statement = connection.prepareStatement(SQL_GET_PAIRED_ACCOUNT_QUANTITY, discordId);

                ResultSet result = statement.executeQuery();

                if(!result.next())
                    throw new MissedFetchException("Failed to get amount of paired accounts.");

                int count = result.getInt(1);
                result.close();

                output.complete(count);

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

    /**
     * Checks if a given Minecraft account is linked to a Discord account, returning the data of the link entry.
     * @param platformId the minecraft account id
     * @return completable future returned once complete - optional is filled if the link existed.
     */
    public static OptionalFuture<LinkedAccount> getLinkFor(UUID platformId) {
        OptionalFuture<LinkedAccount> optionalCompletableFuture = new OptionalFuture<>();
        optionalCompletableFuture.complete(Optional.empty());
        return optionalCompletableFuture;
    }

    /**
     * Completes a link between a Discord account with a Minecraft account without checking if the pairing tokens match.
     * Clears any Pending Links for that account as well.
     * @param discordId the discord account id to pair with
     * @param platformId the minecraft account id
     * @param verifier where did the player get verified from (admin, [discord server short name], DMs?)
     * @return completable future returned once complete - true if the link was successfully completed.
     */
    public static CompletableFuture<Boolean> createLinkBetween(String discordId, String platformId, String verifier) {
        return null;
    }

}
