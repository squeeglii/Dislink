package me.squeeglii.plugin.dislink.storage.helper;

import java.sql.SQLException;

@FunctionalInterface
public interface BatchOperation {

    void run() throws SQLException;

}