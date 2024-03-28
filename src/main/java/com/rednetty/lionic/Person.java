package com.rednetty.lionic;

import java.io.Serializable;

public class Person implements Serializable {
    private String name;
    private int age;
    private int birthYear;

    public Person(String name, int age, int birthYear) {
        this.age = age;
        this.birthYear= birthYear;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public String getName() {
        return name;
    }
}
