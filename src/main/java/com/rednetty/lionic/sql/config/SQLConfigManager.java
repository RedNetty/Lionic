package com.rednetty.lionic.sql.config;

import com.google.gson.Gson;
import com.rednetty.lionic.sql.DatabaseLogin;
import com.rednetty.lionic.sql.SQLConnector;

import java.io.FileReader;
import java.io.IOException;

public class SQLConfigManager {

    private static final String DEFAULT_FILE_LOCATION = "./src/main/resources/sql-config.json";
    private DatabaseLogin databaseLogin;
    private final Gson gson;


    public SQLConfigManager() {
        this.gson = new Gson();
    }

    public SQLConfigManager initialize(SQLConnector sqlConnector) {
        try (FileReader reader = new FileReader(DEFAULT_FILE_LOCATION)) {
            DatabaseLogin databaseLogin = gson.fromJson(reader, DatabaseLogin.class);
            this.databaseLogin = databaseLogin;
            sqlConnector.initializeSQL(databaseLogin);
        } catch (IOException e) {
            System.err.println("Error grabbing SQL Configuration File.");
            e.printStackTrace();
        }
        return this;
    }

    public DatabaseLogin getDatabaseLogin() {
        return databaseLogin;
    }
}
