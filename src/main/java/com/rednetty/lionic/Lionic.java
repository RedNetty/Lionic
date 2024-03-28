package com.rednetty.lionic;

import com.rednetty.lionic.sql.config.SQLConfigManager;
import com.rednetty.lionic.sql.SQLConnector;

public class Lionic {
    private static SQLConnector sqlConnector;
    private static SQLConfigManager sqlConfig;

    public static void main(String[] args) {
        sqlConnector = new SQLConnector();

        SQLConfigManager sqlConfigManager = new SQLConfigManager();
        sqlConfig = sqlConfigManager;
        sqlConfigManager.initialize(sqlConnector);
    }

    public static SQLConfigManager getSqlConfigManager() {
        return sqlConfig;
    }

    public static SQLConnector getSqlConnector() {
        return sqlConnector;
    }
}
