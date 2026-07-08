package io.github.rohanpurohit7.battleship;

import java.util.HashSet;
import java.util.Set;

final class FleetShip {
    private final String name;
    private final int length;
    private final Set<String> occupiedCells = new HashSet<>();
    private final Set<String> hits = new HashSet<>();

    FleetShip(String name, int length) {
        this.name = name;
        this.length = length;
    }

    String name() {
        return name;
    }

    int length() {
        return length;
    }

    void addCell(String coordinate) {
        occupiedCells.add(coordinate);
    }

    void hit(String coordinate) {
        if (occupiedCells.contains(coordinate)) {
            hits.add(coordinate);
        }
    }

    boolean isSunk() {
        return occupiedCells.size() == hits.size();
    }

    int remainingCells() {
        return occupiedCells.size() - hits.size();
    }
}
