package com.rednetty.lionic.sql;

import com.rednetty.lionic.Lionic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnector {

    private Connection connection;

    private DatabaseLogin databaseLogin;
    public void initializeSQL(DatabaseLogin databaseLogin) {
        this.databaseLogin = databaseLogin;
        try {
            connect();
            System.out.println("Connection Established with PostgreSQL database!");
            new SQLStorage(this).initialize();
        }catch (IOException e) {
            System.err.println("Error while initializing the SQL Connector.");
            e.printStackTrace();
        }
    }
    public Connection connect() throws IOException {
        String url = "jdbc:" + databaseLogin.getDbType() + "://" + databaseLogin.getHostName() + ":" + databaseLogin.getPort() + "/" + databaseLogin.getDbName();

        try {
            connection = DriverManager.getConnection(url, databaseLogin.getDbUsername(), databaseLogin.getDbPassword());
            if(databaseLogin == null) System.err.println("Database Login returned a null value.");
            if (connection != null) {
                return connection;

            } else {
                System.err.println("Connection failed.");
            }
        } catch (SQLException e) {
            throw new IOException("Connection failure: " + e.getMessage(), e);
        }
        return null;
    }
    public Connection getConnection() throws SQLException {
        if(connection.isClosed()) try {
            return connect();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
