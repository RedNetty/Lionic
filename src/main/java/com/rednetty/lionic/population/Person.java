package com.rednetty.lionic.population;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a person entity in the system.
 * This class stores basic person information and additional flexible attributes.
 * It supports both traditional get/set methods and record-style accessor methods
 * for backward compatibility.
 */
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private String firstName;
    private String lastName;
    private int age;
    private String email;
    private Map<String, Object> additionalData;

    /**
     * Creates a new Person with the specified attributes.
     *
     * @param id Unique identifier for the person
     * @param firstName Person's first name
     * @param lastName Person's last name
     * @param age Person's age
     * @param email Person's email address
     */
    public Person(long id, String firstName, String lastName, int age, String email) {
        validateId(id);
        validateName(firstName);
        validateAge(age);

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
        this.additionalData = new HashMap<>();
    }

    /**
     * Creates a new Person with a single name field.
     * Used for backward compatibility.
     *
     * @param id Unique identifier for the person
     * @param name Person's name
     * @param networth Person's net worth
     * @param age Person's age
     * @param birthYear Person's birth year
     */
    public Person(long id, String name, double networth, int age, int birthYear) {
        validateId(id);
        validateName(name);
        validateAge(age);

        this.id = id;
        this.firstName = name; // Use name as firstName
        this.lastName = "";    // Empty lastName
        this.age = age;
        this.email = generateDefaultEmail(name);

        this.additionalData = new HashMap<>();
        this.additionalData.put("networth", networth);
        this.additionalData.put("birthYear", birthYear);
    }

    /**
     * Validates the person ID.
     *
     * @param id ID to validate
     * @throws IllegalArgumentException if ID is negative
     */
    private void validateId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Person ID cannot be negative");
        }
    }

    /**
     * Validates a name.
     *
     * @param name Name to validate
     * @throws IllegalArgumentException if name is null or empty
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }

    /**
     * Validates age.
     *
     * @param age Age to validate
     * @throws IllegalArgumentException if age is negative
     */
    private void validateAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }

    /**
     * Generates a default email from a name.
     *
     * @param name Person's name
     * @return Generated email address
     */
    private String generateDefaultEmail(String name) {
        // Replace spaces with dots and add example domain
        return name.toLowerCase().replace(' ', '.') + "@example.com";
    }

    /**
     * @return Person's unique identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Record-style accessor for ID (backward compatibility).
     *
     * @return Person's unique identifier
     */
    public long id() {
        return id;
    }

    /**
     * @return Person's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the person's first name.
     *
     * @param firstName New first name
     * @throws IllegalArgumentException if name is null or empty
     */
    public void setFirstName(String firstName) {
        validateName(firstName);
        this.firstName = firstName;
    }

    /**
     * @return Person's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the person's last name.
     *
     * @param lastName New last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName : "";
    }

    /**
     * @return Person's full name (first + last)
     */
    public String getFullName() {
        if (lastName == null || lastName.isEmpty()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Record-style accessor for name (backward compatibility).
     *
     * @return Person's name
     */
    public String name() {
        return getFullName();
    }

    /**
     * @return Person's age
     */
    public int getAge() {
        return age;
    }

    /**
     * Record-style accessor for age (backward compatibility).
     *
     * @return Person's age
     */
    public int age() {
        return age;
    }

    /**
     * Sets the person's age.
     *
     * @param age New age
     * @throws IllegalArgumentException if age is negative
     */
    public void setAge(int age) {
        validateAge(age);
        this.age = age;
    }

    /**
     * @return Person's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the person's email address.
     *
     * @param email New email address
     */
    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    /**
     * @return Map of additional flexible attributes for this person
     */
    public Map<String, Object> getAdditionalData() {
        return Collections.unmodifiableMap(additionalData);
    }

    /**
     * Sets additional attributes for this person.
     *
     * @param additionalData Map of key-value attributes
     */
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = new HashMap<>();
        if (additionalData != null) {
            this.additionalData.putAll(additionalData);
        }
    }

    /**
     * Adds a single attribute to the additional data map.
     *
     * @param key Attribute name
     * @param value Attribute value
     * @return This person instance for chaining
     */
    public Person addAttribute(String key, Object value) {
        if (key != null) {
            this.additionalData.put(key, value);
        }
        return this;
    }

    /**
     * Gets a specific attribute from the additional data map.
     *
     * @param key Attribute name
     * @param <T> Type of the attribute value
     * @return Attribute value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) this.additionalData.get(key);
    }

    /**
     * Gets a specific attribute with a default value if not found.
     *
     * @param key Attribute name
     * @param defaultValue Default value to return if attribute not found
     * @param <T> Type of the attribute value
     * @return Attribute value, or defaultValue if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = this.additionalData.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Checks if this person has a specific attribute.
     *
     * @param key Attribute name
     * @return true if the attribute exists, false otherwise
     */
    public boolean hasAttribute(String key) {
        return this.additionalData.containsKey(key);
    }

    /**
     * Removes an attribute from the additional data map.
     *
     * @param key Attribute name
     * @return The previous value of the attribute, or null if not found
     */
    public Object removeAttribute(String key) {
        return this.additionalData.remove(key);
    }

    /**
     * Gets the person's net worth from additional data.
     * Record-style accessor for backward compatibility.
     *
     * @return Person's net worth, or 0 if not set
     */
    public double networth() {
        Double networth = getAttribute("networth");
        return networth != null ? networth : 0.0;
    }

    /**
     * Gets the person's birth year from additional data.
     *
     * @return Person's birth year, or current year - age if not set
     */
    public int birthYear() {
        Integer birthYear = getAttribute("birthYear");
        if (birthYear != null) {
            return birthYear;
        }
        // Approximate birth year based on age
        return java.time.Year.now().getValue() - age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("Person{")
                .append("id=").append(id)
                .append(", name='").append(getFullName()).append('\'')
                .append(", age=").append(age)
                .append(", email='").append(email).append('\'');

        // Add networth if present
        Double networth = getAttribute("networth");
        if (networth != null) {
            sb.append(", networth=").append(String.format("$%,.2f", networth));
        }

        // Add birth year if present
        Integer birthYear = getAttribute("birthYear");
        if (birthYear != null) {
            sb.append(", birthYear=").append(birthYear);
        }

        // Add other additional data
        if (!additionalData.isEmpty()) {
            sb.append(", additionalData=");

            // Filter out already displayed attributes
            Map<String, Object> filteredData = new HashMap<>(additionalData);
            filteredData.remove("networth");
            filteredData.remove("birthYear");

            if (!filteredData.isEmpty()) {
                sb.append(filteredData);
            } else {
                sb.append("{}");
            }
        }

        sb.append('}');
        return sb.toString();
    }

    /**
     * Creates a deep copy of this person.
     *
     * @return A new Person instance with the same data
     */
    public Person copy() {
        Person copy = new Person(id, firstName, lastName, age, email);
        copy.setAdditionalData(new HashMap<>(additionalData));
        return copy;
    }
}