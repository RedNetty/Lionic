package com.rednetty.lionic;

import com.rednetty.lionic.population.PersonManager;
import com.rednetty.lionic.sql.SQLConnector;
import com.rednetty.lionic.sql.config.SQLConfigManager;

public class Lionic {
    private static SQLConnector sqlConnector;
    private static SQLConfigManager sqlConfig;
    private static PersonManager personManager;

    public Lionic() {
        sqlConnector = new SQLConnector();
        sqlConfig = new SQLConfigManager().initialize(sqlConnector);
        personManager = new PersonManager().initialize();
    }

    public static SQLConfigManager getSqlConfigManager() {
        return sqlConfig;
    }

    public static SQLConnector getSqlConnector() {
        return sqlConnector;
    }

    public static PersonManager getPersonManager() {
        return personManager;
    }
}
