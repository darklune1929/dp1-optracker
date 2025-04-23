// Population.java
package edu.pucp.plg_back.service.impl;

import java.util.*;

class Population {
    List<Chromosome> individuals;

    Population(int size) {
        individuals = new ArrayList<>(size);
    }

    void add(Chromosome c) {
        individuals.add(c);
    }

    void sort() {
        Collections.sort(individuals);
    }

    Chromosome best() {
        return individuals.get(0);
    }
}
