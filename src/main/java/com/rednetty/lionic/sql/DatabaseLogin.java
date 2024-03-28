package com.rednetty.lionic.sql;

public class DatabaseLogin {
    String dbType;
    String hostName;
    String dbName;
    String dbUsername;
    String dbPassword;
    int port;

    public DatabaseLogin(String dbName, String hostName, String dbType, String dbUsername, String dbPassword, int port) {
        this.dbName = dbName;
        this.hostName = hostName;
        this.dbType = dbType;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.port = port;
    }

    public String getDbType() {
        return dbType;
    }

    public String getHostName() {
        return hostName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public int getPort() {
        return port;
    }
}
