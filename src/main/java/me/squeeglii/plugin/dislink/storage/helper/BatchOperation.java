package me.squeeglii.plugin.dislink.storage.helper;

import java.sql.SQLException;

/**
 * Slightly modified runnable that passes on any SQL errors.
 */
@FunctionalInterface
public interface BatchOperation {

    void run() throws SQLException;

}