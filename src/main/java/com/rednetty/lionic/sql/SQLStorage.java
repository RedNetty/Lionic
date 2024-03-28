package com.rednetty.lionic.sql;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rednetty.lionic.Person;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLStorage {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS people (" +
                    "    id SERIAL PRIMARY KEY," +
                    "    serializedPeople TEXT" +
                    ")";

    private static final String INSERT_SQL = "INSERT INTO people (serializedPeople) VALUES (?)";
    private static final String SELECT_SQL = "SELECT serializedPeople FROM people";
    private boolean initialized = false;

    private final Gson gson = new Gson();
    private final SQLConnector sqlConnector;

    public SQLStorage(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    public void initialize() {
        if(initialized) return;
        try (Connection connection = sqlConnector.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);

            storePeople(createFakePeople());

            grabPeople().forEach(person ->
                    System.out.println("-------------------\nName: " + person.getName() +  "\nAge: " + person.getAge() + "\n-------------------\n"));
            initialized = true;
        } catch (SQLException e) {
            System.err.println("Error creating table");
        }
    }

    public ArrayList<Person> createFakePeople() {
        ArrayList<Person> peopleList = new ArrayList<>();
        Person john = new Person("john", 24, 2000);
        Person jim = new Person("jim", 20, 2003);
        Person jake = new Person("jake", 23, 2001);
        Person jack = new Person("jack", 19, 2004);
        peopleList.add(john);
        peopleList.add(jim);
        peopleList.add(jake);
        peopleList.add(jack);
        return peopleList;
    }
    public void storePeople(List<Person> people) {
        String serializedPeople = gson.toJson(people);
        try (Connection connection = sqlConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, serializedPeople);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error storing people");
            e.printStackTrace();
        }
    }

    public List<Person> grabPeople() {
        try (Connection connection = sqlConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SQL)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Type type = new TypeToken<ArrayList<Person>>(){}.getType();
                return gson.fromJson(resultSet.getString(1), type);
            } else {
                return new ArrayList<>();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving people");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
