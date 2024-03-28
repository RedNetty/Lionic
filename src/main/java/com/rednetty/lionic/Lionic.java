package com.rednetty.lionic;

import com.rednetty.lionic.population.Person;
import com.rednetty.lionic.population.PersonManager;
import com.rednetty.lionic.sql.config.SQLConfigManager;
import com.rednetty.lionic.sql.SQLConnector;

import java.util.Scanner;

public class Lionic {
    private static SQLConnector sqlConnector;
    private static SQLConfigManager sqlConfig;
    private static PersonManager personManager;

    public static void main(String[] args) {
        sqlConnector = new SQLConnector();

        SQLConfigManager sqlConfigManager = new SQLConfigManager();
        sqlConfig = sqlConfigManager;
        sqlConfigManager.initialize(sqlConnector);
        personManager = new PersonManager();
        personManager.initialize();


    }




    public static SQLConfigManager getSqlConfigManager() {
        return sqlConfig;
    }

    public static SQLConnector getSqlConnector() {
        return sqlConnector;
    }
}
