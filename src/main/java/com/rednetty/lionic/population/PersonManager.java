package com.rednetty.lionic.population;

import com.rednetty.lionic.sql.exception.DatabaseException;
import com.rednetty.lionic.sql.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages a collection of Person entities and provides a user interface
 * for adding, removing, and listing people.
 */
public class PersonManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonManager.class);
    private final DatabaseService databaseService;
    private List<Person> people;
    private boolean running = true;

    /**
     * Creates a new PersonManager instance.
     *
     * @param databaseService Database service for persistence operations
     */
    public PersonManager(DatabaseService databaseService) {
        this.databaseService = databaseService;
        refreshPeopleList();
    }

    /**
     * Initializes the PersonManager and starts the command interface.
     *
     * @return This PersonManager instance
     */
    public PersonManager initialize() {
        startCommandInterface();
        return this;
    }

    /**
     * Refreshes the in-memory list of people from the database.
     */
    private void refreshPeopleList() {
        try {
            people = databaseService.getAllPeople();
            LOGGER.info("Loaded {} people from database", people.size());
        } catch (DatabaseException e) {
            LOGGER.error("Failed to load people from database: {}", e.getMessage());
            people = new ArrayList<>();
        }
    }

    /**
     * Starts the interactive command interface for managing people.
     */
    public void startCommandInterface() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Person Manager");
        System.out.println("=========================");

        while (running) {
            displayMenu();
            String command = scanner.nextLine().trim();

            try {
                processCommand(command, scanner);
            } catch (Exception e) {
                LOGGER.error("Error processing command: {}", e.getMessage());
                System.out.println("An error occurred: " + e.getMessage());
            }

            System.out.println(); // Add empty line for readability
        }
    }

    /**
     * Displays the available commands menu.
     */
    private void displayMenu() {
        System.out.println("\nPlease enter a command:");
        System.out.println("1. Add person (-add networth,name,age,birthYear)");
        System.out.println("2. Remove person by name (-remove name)");
        System.out.println("3. Remove person by ID (-remove id)");
        System.out.println("4. List all people (-list)");
        System.out.println("5. Find people by name (-find namePattern)");
        System.out.println("6. Save changes (-save)");
        System.out.println("7. Exit (-exit)");
        System.out.print("> ");
    }

    /**
     * Processes a user command.
     *
     * @param command The command string to process
     * @param scanner Scanner for additional input if needed
     */
    private void processCommand(String command, Scanner scanner) {
        if (command.startsWith("-add")) {
            handleAddCommand(command);
        } else if (command.startsWith("-remove")) {
            handleRemoveCommand(command);
        } else if (command.startsWith("-find")) {
            handleFindCommand(command);
        } else if (command.equals("-list")) {
            listPeople();
        } else if (command.equals("-save")) {
            savePeople();
        } else if (command.equals("-exit")) {
            running = false;
            System.out.println("Exiting Person Manager...");
        } else {
            System.out.println("Unknown command. Please try again.");
        }
    }

    /**
     * Handles the add person command.
     *
     * @param commandString The full command string
     */
    private void handleAddCommand(String commandString) {
        // Remove the "-add " part and split by comma
        String[] parts = commandString.substring(5).trim().split(",");

        if (parts.length != 4) {
            System.out.println("Invalid format. Use: -add networth,name,age,birthYear");
            return;
        }

        try {
            double networth = Double.parseDouble(parts[0].trim());
            String name = parts[1].trim();
            int age = Integer.parseInt(parts[2].trim());
            int birthYear = Integer.parseInt(parts[3].trim());


            // Generate a new ID based on the current size or maximum ID
            long newId = people.isEmpty() ? 1 : people.stream().mapToLong(Person::getId).max().orElse(0) + 1;

            // Create person with existing fields - use firstName, lastName format
            Person person = new Person(newId, name, "", age, name + "@example.com");

            // Add the additional data (networth and birthYear)
            person.addAttribute("networth", networth);
            person.addAttribute("birthYear", birthYear);

            addPerson(person);
            System.out.println("Added new person: " + person.getFirstName() + " (ID: " + person.getId() + ")");

        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please check your input.");
        } catch (Exception e) {
            System.out.println("Error adding person: " + e.getMessage());
        }
    }

    /**
     * Handles the remove person command.
     *
     * @param commandString The full command string
     */
    private void handleRemoveCommand(String commandString) {
        String value = commandString.substring(8).trim();

        try {
            // Try to parse as ID first
            long id = Long.parseLong(value);
            removePerson(id);
        } catch (NumberFormatException e) {
            // If not a number, treat as name
            removePersonByName(value);
        }
    }

    /**
     * Handles the find people by name command.
     *
     * @param commandString The full command string
     */
    private void handleFindCommand(String commandString) {
        String namePattern = commandString.substring(6).trim();

        if (namePattern.isEmpty()) {
            System.out.println("Please provide a name pattern to search for.");
            return;
        }

        try {
            List<Person> matches = databaseService.findPeopleByName(namePattern);

            if (matches.isEmpty()) {
                System.out.println("No people found matching '" + namePattern + "'");
            } else {
                System.out.println("Found " + matches.size() + " people matching '" + namePattern + "':");
                displayPeopleList(matches);
            }
        } catch (Exception e) {
            System.out.println("Error searching for people: " + e.getMessage());
        }
    }

    /**
     * Lists all people in the system.
     */
    public void listPeople() {
        if (people.isEmpty()) {
            System.out.println("No people in the system.");
        } else {
            System.out.println("Total population: " + people.size() + " people");
            displayPeopleList(people);
        }
    }

    /**
     * Displays a formatted list of people.
     *
     * @param peopleList List of people to display
     */
    private void displayPeopleList(List<Person> peopleList) {
        for (Person person : peopleList) {
            System.out.println("-------------------");
            System.out.println("ID: " + person.getId());
            System.out.println("Name: " + person.getFirstName());
            System.out.println("Age: " + person.getAge());

            // Get additional data with type safety
            Double networth = person.getAttribute("networth");
            Integer birthYear = person.getAttribute("birthYear");

            if (networth != null) {
                System.out.println("Net Worth: $" + String.format("%,.2f", networth));
            }

            if (birthYear != null) {
                System.out.println("Birth Year: " + birthYear);
            }

            System.out.println("-------------------");
        }
    }

    /**
     * Adds a person to the system.
     *
     * @param person Person to add
     */
    public void addPerson(Person person) {
        try {
            databaseService.savePerson(person);
            refreshPeopleList();
        } catch (Exception e) {
            LOGGER.error("Failed to add person: {}", e.getMessage());
            throw new RuntimeException("Failed to add person: " + e.getMessage(), e);
        }
    }

    /**
     * Removes a person by name.
     *
     * @param name Name of person to remove
     */
    public void removePersonByName(String name) {
        Optional<Person> personToRemove = people.stream().filter(p -> p.getFirstName().equalsIgnoreCase(name)).findFirst();

        if (personToRemove.isPresent()) {
            Person person = personToRemove.get();
            boolean success = databaseService.deletePerson(person.getId());

            if (success) {
                System.out.println("Removed " + person.getFirstName() + " from the population.");
                refreshPeopleList();
            } else {
                System.out.println("Failed to remove " + person.getFirstName() + ".");
            }
        } else {
            System.out.println("No person found with name: " + name);
        }
    }

    /**
     * Removes a person by ID.
     *
     * @param id ID of person to remove
     */
    public void removePerson(long id) {
        Optional<Person> personToRemove = people.stream().filter(p -> p.getId() == id).findFirst();

        if (personToRemove.isPresent()) {
            Person person = personToRemove.get();
            boolean success = databaseService.deletePerson(id);

            if (success) {
                System.out.println("Removed " + person.getFirstName() + " (ID: " + id + ") from the population.");
                refreshPeopleList();
            } else {
                System.out.println("Failed to remove person with ID: " + id);
            }
        } else {
            System.out.println("No person found with ID: " + id);
        }
    }

    /**
     * Saves all people to the database.
     */
    public void savePeople() {
        try {
            databaseService.savePeople(people);
            System.out.println("Successfully saved " + people.size() + " people to database.");
        } catch (Exception e) {
            System.out.println("Error saving people: " + e.getMessage());
        }
    }

    /**
     * @return Current list of people
     */
    public List<Person> getPeople() {
        return Collections.unmodifiableList(people);
    }
}