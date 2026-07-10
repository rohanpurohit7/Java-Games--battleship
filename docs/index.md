# Battleship Command Documentation

## Overview

Battleship Command turns the original text-based Battleship exercise into a GUI mission with hidden fleets, visual strike feedback, submarine reveal states, sound effects, spoken admiral narration, and difficulty-based torpedo limits.

The current game is intentionally compact: all gameplay takes place on one screen, and the player can immediately choose a difficulty level and begin firing on the grid.

## Document Map

- [Demo Guide](demo.md): how to run and present the game.
- [Game Narrative](narrative.md): the mission premise, admiral identities, and voice line behavior.
- [Architecture Notes](architecture.md): project structure, JavaFX components, audio strategy, and build notes.

## Main User Flow

1. Pick a level: Easy, Medium, or High.
2. Fire on the 7x7 grid.
3. Watch miss, hit, and explosion feedback.
4. Track hits and kills on the fleet kill board.
5. Sink all three fleets before the torpedo counter reaches zero.

## Current Entry Point

```text
navalbattle.BattleshipFxApp
```

## Runtime Requirements

- Windows
- JDK 24 configured through `JAVA_HOME`
- Gradle wrapper included in the project
- JavaFX dependencies resolved by Gradle

The project includes `run-gui.bat` for the local Windows setup.
