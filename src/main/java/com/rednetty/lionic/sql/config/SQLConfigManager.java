package com.rednetty.lionic.sql.config;

import com.google.gson.Gson;
import com.rednetty.lionic.sql.DatabaseLogin;
import com.rednetty.lionic.sql.SQLConnector;

import java.io.FileReader;
import java.io.IOException;

public class SQLConfigManager {

    private static final String DEFAULT_FILE_LOCATION = "./src/main/resources/sql-config.json";
    private String sqlFileLocation;
    private DatabaseLogin databaseLogin;
    private Gson gson;

    public SQLConfigManager() {
        this(DEFAULT_FILE_LOCATION); // Use a default location
    }

    public SQLConfigManager(String sqlFileLocation) {
        this.sqlFileLocation = sqlFileLocation;
        this.gson = new Gson();
    }

    public void initialize(SQLConnector sqlConnector) {
        try (FileReader reader = new FileReader(sqlFileLocation)) {
            DatabaseLogin databaseLogin = gson.fromJson(reader, DatabaseLogin.class);
            this.databaseLogin = databaseLogin;
            sqlConnector.initializeSQL(databaseLogin);
        } catch (IOException e) {
            System.err.println("Error grabbing SQL Configuration File.");
            e.printStackTrace();
        }
    }

    public DatabaseLogin getDatabaseLogin() {
        return databaseLogin;
    }
}
