package me.squeeglii.plugin.dislink.storage;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.config.Feature;
import me.squeeglii.plugin.dislink.exception.ExhaustedOptionsException;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseHelper;
import me.squeeglii.plugin.dislink.config.Cfg;
import me.squeeglii.plugin.dislink.util.Check;
import me.squeeglii.plugin.dislink.util.Generate;
import me.squeeglii.plugin.dislink.util.Run;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles generating link codes + verifying that they're correct.
 */
public class DBPendingLinks {

    public static final int LINK_CODE_SIZE_LIMIT = 128; // If you modify the table, update this too.

    private static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ? (link_code VARCHAR(128) PRIMARY KEY, platform_id VARCHAR(36));";

    public static final String SQL_CLEAR_ALL = "TRUNCATE TABLE ?;";

    public static final String SQL_CREATE_PENDING_LINK = "INSERT INTO ? (platform_id, link_code) VALUES (?, ?);";

    public static final String SQL_CHECK_NO_DUPES = "SELECT COUNT(*) FROM ? WHERE link_code=?;";

    public static final String SQL_GET_EXISTING_LINKS = "SELECT link_code FROM ? WHERE platform_id=?;";

    public static final String SQL_GET_LINK_BY_CODE = "SELECT platform_id FROM ? WHERE link_code=?;";

    public static final String SQL_CLEAR_PENDING_LINKS = "DELETE FROM ? WHERE platform_id=?;";

    // Errors for the config being invalid should only be logged once per reload
    // as they end up being spam. This just tracks if an error has been logged on a given load.
    private static int lastLoadWarned = -1;

    /**
     * Creates the tables necessary to run the link code
     * generation system.
     */
    public static CompletableFuture<Void> createTables() {
        CompletableFuture<Void> output = new CompletableFuture<>();

        Run.async(() -> {
            ConnectionWrapper connection = null;
            PreparedStatement statement = null;

            try {
                String tableName = DBLinks.getEstablishedLinksTable();
                connection = Dislink.plugin().getDbConnection();
                statement = connection.prepareStatement(SQL_CREATE_TABLE, tableName);

                statement.execute();

                output.complete(null);

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
     * Asynchronously begins the linking process for a given Minecraft account.
     * @param platformId the id of the account (whatever is primarily used to identify it)
     * @return a link code to enter via the discord command to complete the link.
     */
    public static CompletableFuture<String> startLinkingFor(UUID platformId) {
        CompletableFuture<String> output = new CompletableFuture<>();

        int maxGenerationAttempts = Cfg.PAIRING_GENERATION_ATTEMPTS.dislink().orElse(3);

        Run.async(() -> {
            ConnectionWrapper conn = null;
            List<PreparedStatement> statements = new LinkedList<>();

            try {
                conn = Dislink.plugin().getDbConnection();
                conn.batch(connection -> {
                    Optional<String> existingLinkCode = getExistingLink(connection, statements, platformId);

                    // User has already tried to link - give them the same code.
                    if(existingLinkCode.isPresent()) {
                        output.complete(existingLinkCode.get());
                        return;
                    }

                    int attempts = 0;
                    Optional<String> codeGenerated = Optional.empty();

                    while (codeGenerated.isEmpty() && attempts < maxGenerationAttempts) {
                        attempts++;
                        codeGenerated = attemptAndCheckCodeGeneration(connection, statements);
                    }

                    if(codeGenerated.isEmpty()) {
                        output.completeExceptionally(new ExhaustedOptionsException());
                        return;
                    }

                    String id = platformId.toString();
                    String code = codeGenerated.get();
                    String tableName = DBPendingLinks.getPendingLinkTable();

                    PreparedStatement submitStatement = connection.prepareStatement(SQL_CREATE_PENDING_LINK, tableName, id, code);
                    statements.add(submitStatement);
                    submitStatement.execute();

                    output.complete(code);
                });

            } catch (Exception err) {
                output.completeExceptionally(err);
                return;

            } finally {
                for(PreparedStatement statement: statements)
                    DatabaseHelper.closeQuietly(statement);

                DatabaseHelper.closeQuietly(conn);
            }
        });

        return output;
    }

    /**
     * Asynchronously checks if the link code provided matches with a pending link entry and
     * triggers the completion of the linking process if the codes match.
     * @param discordId the id of the discord account being paired to
     * @param pairCode the code to check against the code stored in-database
     * @param verifier where did the player get verified from (admin, [discord server short name], DMs?)
     * @return a link code to enter via the discord command to complete the link.
     */
    public static CompletableFuture<LinkResult> tryCompleteLink(String discordId, String pairCode, String verifier) {
        CompletableFuture<LinkResult> output = new CompletableFuture<>();

        if(Check.isPairCodeUnsafe(pairCode)) {
            output.complete(LinkResult.INVALID_CODE);
            return output;
        }

        Run.async(() -> {
            int maxAccountCount = Cfg.MAX_ACCOUNT_LIMIT.dislink().orElse(2);

            // if 0 or -1, just skip the check.
            if(maxAccountCount > 0) {
                int currentAccountCount = DBLinks.getExistingAccountQuantityFor(discordId).join();

                // Would adding another account exceed the limit? Fail time!
                if(currentAccountCount >= maxAccountCount) {
                    output.complete(LinkResult.ACCOUNT_CAP_REACHED);
                    return;
                }
            }


            ConnectionWrapper conn = null;
            List<PreparedStatement> statements = new LinkedList<>();

            try {
                conn = Dislink.plugin().getDbConnection();
                conn.batch(connection -> {
                    String pendingLinkTable = DBPendingLinks.getPendingLinkTable();
                    String establishedLinkTable = DBLinks.getEstablishedLinksTable();
                    PreparedStatement getPlatformId = connection.prepareStatement(SQL_GET_LINK_BY_CODE, pendingLinkTable, pairCode.trim().toLowerCase());
                    statements.add(getPlatformId);

                    ResultSet result = getPlatformId.executeQuery();

                    if(!result.next()) {
                        output.complete(LinkResult.INVALID_CODE);
                        return;
                    }

                    String platformId = result.getString(1);

                    PreparedStatement finaliseLink = connection.prepareStatement(DBLinks.SQL_FORM_LINK, establishedLinkTable, discordId, platformId, verifier);
                    statements.add(finaliseLink);
                    finaliseLink.execute();

                    PreparedStatement cleanUpLink = connection.prepareStatement(SQL_CLEAR_PENDING_LINKS, pendingLinkTable, platformId);
                    statements.add(cleanUpLink);
                    cleanUpLink.execute();

                    output.complete(LinkResult.SUCCESS);
                });

            } catch (Exception err) {
                output.complete(LinkResult.INTERNAL_ERROR);
                Dislink.plugin().getLogger().info("Err: "+err.getMessage());
                err.printStackTrace();

            } finally {
                for(PreparedStatement statement: statements)
                    DatabaseHelper.closeQuietly(statement);

                DatabaseHelper.closeQuietly(conn);
            }
        });

        return output;
    }


    /**
     * Clears the Pending Links list to reset the linking process for everyone that isn't already linked.
     * @return completable future that returns when database edits are complete (or error is thrown)
     */
    public static CompletableFuture<Void> clearPendingLinks() {
        CompletableFuture<Void> output = new CompletableFuture<>();

        Run.async(() -> {
            ConnectionWrapper connection = null;
            PreparedStatement statement = null;

            try {
                String tableName = DBPendingLinks.getPendingLinkTable();
                connection = Dislink.plugin().getDbConnection();
                statement = connection.prepareStatement(SQL_CLEAR_ALL, tableName);

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


    private static Optional<String> getExistingLink(ConnectionWrapper conn, List<PreparedStatement> statementPool, UUID accountId) throws SQLException {
        String accountIdStr = accountId.toString();
        String tableName = DBPendingLinks.getPendingLinkTable();

        PreparedStatement statement = conn.prepareStatement(SQL_GET_EXISTING_LINKS, tableName, accountIdStr);
        statementPool.add(statement);

        ResultSet results = statement.executeQuery();

        if(!results.next())
            return Optional.empty();


        String linkCode = results.getString(1);
        return Optional.of(linkCode);
    }

    private static Optional<String> attemptAndCheckCodeGeneration(ConnectionWrapper conn, List<PreparedStatement> statementPool) throws SQLException {

        if(Dislink.usingFeature(Feature.PAIR_CODE_GENERATION)) {

            if(Dislink.getEnableCount() == lastLoadWarned)
                return Optional.empty();

            Dislink.plugin().getLogger().severe(
                    "Link Code generation is disabled due to the config being invalid. Check logs and fix any issues."
            );

            return Optional.empty();
        }

        String newCode = Generate.newLinkCode();
        String tableName = DBPendingLinks.getPendingLinkTable();

        if(newCode.isEmpty()) {
            Dislink.plugin().getLogger().severe("Generated an empty link code! Plugin is misconfigured.");
            return Optional.empty();
        }

        if(newCode.length() > DBPendingLinks.LINK_CODE_SIZE_LIMIT) {
            Dislink.plugin().getLogger().severe("Generated a link code that is too big! " +
                    "Please update the config with shorter words or less link code blocks.");
            return Optional.empty();
        }

        PreparedStatement statement = conn.prepareStatement(SQL_CHECK_NO_DUPES, tableName, newCode);
        statementPool.add(statement);

        ResultSet results = statement.executeQuery();

        if(!results.next()) {
            Dislink.plugin().getLogger().warning("Failed to count link code dupes. This could mean the Database is broken!");
            return Optional.empty();
        }

        int dupeCount = results.getInt(1);
        results.close();

        return dupeCount == 0
                ? Optional.of(newCode)
                : Optional.empty();
    }


    private static String getPendingLinkTable() {
        return DatabaseHelper.getFullTableName("PendingLinks");
    }

}
