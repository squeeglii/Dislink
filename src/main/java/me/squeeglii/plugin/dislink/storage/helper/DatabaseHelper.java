package me.squeeglii.plugin.dislink.storage.helper;

import me.squeeglii.plugin.dislink.config.Cfg;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {

    /**
     * Close a statement without throwing any errors.
     * @param stmt The {@link Statement} being closed.
     */
    public static void closeQuietly(Statement stmt) {
        if (stmt == null) return;

        try {
            stmt.close();
        } catch (SQLException ignored) { }
    }

    /**
     * Close a connection without throwing any errors
     * @param wrapper The {@link ConnectionWrapper} being closed.
     */
    public static void closeQuietly(ConnectionWrapper wrapper) {
        if (wrapper == null) return;

        try {
            wrapper.close();
        } catch (SQLException ignored) { }
    }

    /**
     * Gets a table name with the plugin prefix.
     */
    public static String getFullTableName(String shortName) {
        return Cfg.DB_PREFIX.dislink().orElse("") + shortName;
    }
}