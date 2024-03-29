package com.rednetty.lionic.population;

import com.rednetty.lionic.Lionic;
import com.rednetty.lionic.sql.SQLStorage;

import java.util.ArrayList;
import java.util.Scanner;

public class PersonManager {
    private ArrayList<Person> people;
    private SQLStorage sqlStorage;

    public PersonManager() {
        sqlStorage = Lionic.getSqlConnector().getSqlStorage();
        people = sqlStorage.grabPeople();
    }

    public PersonManager initialize() {
       inputPeople();
        return this;
    }

    public void inputPeople() {
        Scanner scanner = new Scanner(System.in);
        boolean finished = false;
        while(!finished) {
            System.out.println("Please enter a valid command. \n" +
                    "Adding People: (-add name,age,birth_year) <- Proper Formatting \n" +
                    "Removing People: (-remove name)\n" +
                    "List total population: (-list)");

            String nextString = scanner.nextLine(); // Read the entire line

            if (nextString.startsWith("-add")) {
                handleAddCommand(nextString, scanner);
            } else if (nextString.startsWith("-remove")) {
                handleRemoveCommand(nextString);
            } else if (nextString.equalsIgnoreCase("-list")) {
                handleListCommand();
            }else if(nextString.equalsIgnoreCase("-save")) {
                sqlStorage.storePeople(people);
            } else {
                System.out.println("Invalid command.");
            }
        }
    }

    // Helper functions for handling each command
    private void handleAddCommand(String commandString, Scanner scanner) {
        String[] parts = commandString.substring(5).split(","); // Split by comma, ignore "-add "
        if (parts.length == 3) {
            String name = parts[0].trim();
            int age;
            int birthYear;
            try {
                age = Integer.parseInt(parts[1].trim());
                birthYear = Integer.parseInt(parts[2].trim());
                newPerson(new Person(name, age, birthYear));
            } catch (NumberFormatException e) {
                System.out.println("Invalid age or birth year format.");
            }
        } else {
            System.out.println("Incorrect formatting for add command.");
        }
    }

    private void handleRemoveCommand(String commandString) {
        String name = commandString.substring(8).trim();
       removePerson(name);
    }

    private void handleListCommand() {
        listPeople();
    }

    public void listPeople() {
       people.forEach(person ->
                System.out.println("-------------------\nName: " + person.name() +  "\nAge: " + person.age() + "\n-------------------\n"));
    }
    public void newPerson(Person person) {
        people.add(person);
        sqlStorage.storePeople(people);
        sqlStorage.grabPeople();
    }

    /*
    Just for testing, obviously checking by name isn't a good way to do this
    as if two people had the same name, it would just choose the first person.
     */
    public void removePerson(String name) {
        for (Person person : people) {
            if(person.name().equalsIgnoreCase(name)){
                people.remove(person);

                System.out.println("Removed " + person.name() + " from the population.");
                sqlStorage.storePeople(people);
                return;
            }

        }
    }
}
