package com.rednetty.lionic.population;

import java.io.Serializable;

public record Person(String name, int age, int birthYear) implements Serializable {
}
