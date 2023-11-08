package me.squeeglii.plugin.dislink.storage.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionWrapper {

    private final Connection connection;

    public ConnectionWrapper(Connection connection) {
        this.connection = connection;
    }


    /**
     * Close the connection
     */
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * Shorthand method of preparing a statement.
     * @see ConnectionWrapper#prepareStatement(DatabaseStatement)
     */
    public PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        return prepareStatement(new DatabaseStatement(sql, params));
    }

    /**
     * Prepare a statement using this connection
     * @param statementIn The statement
     * @return a prepared statement given the query and parameters
     */
    public PreparedStatement prepareStatement(DatabaseStatement statementIn) throws SQLException {
        String sql = statementIn.sql();
        Object[] params = statementIn.params();
        PreparedStatement statement = connection.prepareStatement(sql);

        for (int i = 0; i < params.length; i++)
            statement.setObject(i + 1, params[i]);

        return statement;
    }

    public void batch(BatchOperation runnable) throws SQLException {
        boolean isAutoCommitting = connection.getAutoCommit();
        connection.setAutoCommit(false);

        // All exceptions are passed to the caller from this for
        // convenience and to stop: try -> batch -> try
        runnable.run();

        connection.commit();
        connection.setAutoCommit(isAutoCommitting);
    }


    public static ConnectionWrapper fromAccess(DatabaseAccess access) throws SQLException {
        String username = access.username();
        String password = access.password();
        String address = "jdbc:mysql://%s/%s".formatted(access.address(), access.schema());

        Connection conn = DriverManager.getConnection(address, username, password);
        return new ConnectionWrapper(conn);
    }

}