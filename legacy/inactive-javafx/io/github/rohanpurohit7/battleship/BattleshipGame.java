package io.github.rohanpurohit7.battleship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

final class BattleshipGame {
    static final int GRID_SIZE = 7;

    private static final int SHIP_SIZE = 3;
    private static final List<String> SHIP_NAMES = List.of(
            "Captain Nemo",
            "Admiral Nelson",
            "Admiral Yamamoto"
    );

    private final Random random = new Random();
    private final List<Ship> fleet = new ArrayList<>();
    private final Set<Cell> firedCells = new HashSet<>();
    private int torpedoesUsed;

    BattleshipGame() {
        restart();
    }

    void restart() {
        fleet.clear();
        firedCells.clear();
        torpedoesUsed = 0;

        Set<Cell> occupiedCells = new HashSet<>();
        for (String shipName : SHIP_NAMES) {
            List<Cell> cells = placeShip(occupiedCells);
            fleet.add(new Ship(shipName, cells));
            occupiedCells.addAll(cells);
        }
    }

    ShotResult fireAt(int row, int column) {
        Cell target = new Cell(row, column);
        if (firedCells.contains(target)) {
            return new ShotResult(ShotOutcome.REPEAT, null, torpedoesUsed, remainingShips(), false);
        }

        firedCells.add(target);
        torpedoesUsed++;

        for (Ship ship : fleet) {
            if (ship.cells.contains(target)) {
                ship.hits.add(target);
                if (ship.isSunk()) {
                    return new ShotResult(ShotOutcome.SUNK, ship.name, torpedoesUsed, remainingShips(), isWon());
                }
                return new ShotResult(ShotOutcome.HIT, ship.name, torpedoesUsed, remainingShips(), false);
            }
        }

        return new ShotResult(ShotOutcome.MISS, null, torpedoesUsed, remainingShips(), false);
    }

    int remainingShips() {
        int remaining = 0;
        for (Ship ship : fleet) {
            if (!ship.isSunk()) {
                remaining++;
            }
        }
        return remaining;
    }

    boolean isWon() {
        return remainingShips() == 0;
    }

    private List<Cell> placeShip(Set<Cell> occupiedCells) {
        List<List<Cell>> candidates = new ArrayList<>();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column <= GRID_SIZE - SHIP_SIZE; column++) {
                candidates.add(cellsFrom(row, column, 0, 1));
            }
        }

        for (int row = 0; row <= GRID_SIZE - SHIP_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                candidates.add(cellsFrom(row, column, 1, 0));
            }
        }

        Collections.shuffle(candidates, random);
        for (List<Cell> candidate : candidates) {
            if (Collections.disjoint(candidate, occupiedCells)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Unable to place battleship fleet.");
    }

    private static List<Cell> cellsFrom(int row, int column, int rowStep, int columnStep) {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < SHIP_SIZE; i++) {
            cells.add(new Cell(row + (i * rowStep), column + (i * columnStep)));
        }
        return cells;
    }

    enum ShotOutcome {
        HIT,
        MISS,
        SUNK,
        REPEAT
    }

    record Cell(int row, int column) {
    }

    record ShotResult(
            ShotOutcome outcome,
            String shipName,
            int torpedoesUsed,
            int remainingShips,
            boolean won
    ) {
    }

    private static final class Ship {
        private final String name;
        private final List<Cell> cells;
        private final Set<Cell> hits = new HashSet<>();

        private Ship(String name, List<Cell> cells) {
            this.name = name;
            this.cells = List.copyOf(cells);
        }

        private boolean isSunk() {
            return hits.containsAll(cells);
        }
    }
}
