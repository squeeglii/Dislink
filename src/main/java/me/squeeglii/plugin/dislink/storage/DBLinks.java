package me.squeeglii.plugin.dislink.storage;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.exception.ExhaustedOptionsException;
import me.squeeglii.plugin.dislink.exception.MissedFetchException;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseHelper;
import me.squeeglii.plugin.dislink.util.OptionalFuture;
import me.squeeglii.plugin.dislink.util.Run;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles fully-formed account links & checking on login.
 */
public class DBLinks {

    private static final String SQL_DELETE_ALL_FOR_DISCORD = "DELETE FROM UserLinks WHERE discord_id=?;";

    private static final String SQL_GET_PAIRED_ACCOUNT_QUANTITY = "SELECT COUNT(*) FROM UserLinks WHERE discord_id=?;";

    private static final String SQL_GET_PAIRING = "SELECT discord_id, validator FROM UserLinks WHERE platform_id=?;";

    public static final String SQL_FORM_LINK = "INSERT INTO UserLinks (discord_id, platform_id, validator) VALUES (?, ?, ?);";

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
                connection = Dislink.plugin().getDbConnection();
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
        OptionalFuture<LinkedAccount> output = new OptionalFuture<>();
        String platformIdStr = platformId.toString();

        Run.async(() -> {
            ConnectionWrapper connection = null;
            PreparedStatement statement = null;

            try {
                connection = Dislink.plugin().getDbConnection();
                statement = connection.prepareStatement(SQL_GET_PAIRING, platformIdStr);

                ResultSet results = statement.executeQuery();

                if(!results.next()) {
                    output.complete(Optional.empty());
                    return;
                }

                String discordId = results.getString(1);
                String validator = results.getString(2);

                LinkedAccount linkedAccount = new LinkedAccount(discordId, platformId, validator, false);
                output.complete(Optional.of(linkedAccount));

            } catch (Exception err) {
                output.completeExceptionally(err);

            } finally {
                DatabaseHelper.closeQuietly(statement);
                DatabaseHelper.closeQuietly(connection);
            }
        });

        return output;
    }

    /**
     * Completes a link between a Discord account with a Minecraft account without checking if the pairing tokens match.
     * Clears any Pending Links for that account as well.
     * @param discordId the discord account id to pair with
     * @param platformId the minecraft account id
     * @param verifier where did the player get verified from (admin, [discord server short name], DMs?)
     * @return completable future returned once complete - true if the link was successfully completed.
     */
    public static CompletableFuture<Void> createLinkBetween(String discordId, String platformId, String verifier) {
        CompletableFuture<Void> output = new CompletableFuture<>();

        Run.async(() -> {
            ConnectionWrapper connection = null;
            PreparedStatement statement = null;

            try {
                connection = Dislink.plugin().getDbConnection();

                statement = connection.prepareStatement(SQL_FORM_LINK, discordId, platformId, verifier);
                statement.execute();

                output.complete(null);

            } catch (Exception err) {
                output.completeExceptionally(err);
                return;

            } finally {
                DatabaseHelper.closeQuietly(statement);
                DatabaseHelper.closeQuietly(connection);
            }
        });

        return output;
    }


    public static CompletableFuture<Void> deleteAllLinksFor(String discordId) {
        CompletableFuture<Void> output = new CompletableFuture<>();

        Run.async(() -> {
            ConnectionWrapper connection = null;
            PreparedStatement statement = null;

            try {
                connection = Dislink.plugin().getDbConnection();

                statement = connection.prepareStatement(SQL_DELETE_ALL_FOR_DISCORD, discordId);
                statement.execute();

                output.complete(null);

            } catch (Exception err) {
                output.completeExceptionally(err);

            } finally {
                DatabaseHelper.closeQuietly(statement);
                DatabaseHelper.closeQuietly(connection);
            }
        });

        return output;
    }

}
