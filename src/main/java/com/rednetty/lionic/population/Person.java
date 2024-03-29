package com.rednetty.lionic.population;

import java.io.Serializable;

public record Person(int id, String name, double networth, int age, int birthYear) implements Serializable {
}
