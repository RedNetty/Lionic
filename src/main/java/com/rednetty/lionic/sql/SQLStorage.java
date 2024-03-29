package com.rednetty.lionic.sql;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rednetty.lionic.population.Person;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;

public class SQLStorage {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS people (" +
                    "id SERIAL PRIMARY KEY," +
                    "serializedPeople TEXT" +
                    ")";

    private static final String INSERT_SQL = "INSERT INTO people (serializedPeople) VALUES (?)";
    private static final String DELETE_SQL = "DELETE FROM people";
    private static final String SELECT_SQL = "SELECT serializedPeople FROM people";

    private final Gson gson = new Gson();
    private final SQLConnector sqlConnector;

    public SQLStorage(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    public void initialize() {
        try (Connection connection = sqlConnector.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage()); // More informative error
        }
    }

    private void storePeople(ArrayList<Person> people) {
        String serializedPeople = gson.toJson(people);

        try (Connection connection = sqlConnector.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(DELETE_SQL);
             PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL)) {

            deleteStatement.executeUpdate();
            insertStatement.setString(1, serializedPeople);
            insertStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error storing people: " + e.getMessage());
        }
    }

    public ArrayList<Person> grabPeople() {
        try (Connection connection = sqlConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SQL)) {

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String rawJson = resultSet.getString(1);
                System.out.println("Raw JSON from Database: " + rawJson);

                Type type = new TypeToken<ArrayList<Person>>() {}.getType();
                return gson.fromJson(rawJson, type);
            } else {
                return new ArrayList<>(); // Return empty list if no data found
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving people: " + e.getMessage());
            return new ArrayList<>(); // More consistent error handling
        }
    }
}
