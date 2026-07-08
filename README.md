# Battleship Command

Battleship Command is a JavaFX naval strategy game built from a simple console Battleship exercise and expanded into an interactive GUI mission.

The game uses a 7x7 battlefield, hidden submarine fleets, animated torpedo strikes, generated sound effects, spoken admiral taunts, and a mission difficulty selector that limits available torpedoes.

## Documentation

- [Documentation Index](docs/index.md)
- [Demo Guide](docs/demo.md)
- [Game Narrative](docs/narrative.md)
- [Architecture Notes](docs/architecture.md)

## Features

- JavaFX graphical battlefield with clickable grid cells
- Hidden Japanese, English, and German submarine fleets
- Hit cells reveal colored submarine silhouettes
- Fleet kill board with per-fleet hit counts and kill counts
- Easy, Medium, and High mission levels
- Torpedo limits by level:
  - Easy: 15 torpedoes
  - Medium: 13 torpedoes
  - High: 10 torpedoes
- Torpedo flight and explosion visual effects
- Generated launch, miss, hit, sunk, and victory sound effects
- Windows text-to-speech admiral comments for hits, sinks, victory, and failure

## Run

This project is configured as a Gradle JavaFX application.

On this machine, use:

```powershell
.\run-gui.bat
```

Or run Gradle directly after setting `JAVA_HOME` to a JDK 24 installation:

```powershell
$env:JAVA_HOME='C:\Users\rpuro\.jdks\temurin-24.0.2'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat run
```

The active GUI entry point is:

```text
navalbattle.BattleshipFxApp
```

## Source Layout

```text
src/main/java/navalbattle/
  BattleshipFxApp.java   JavaFX game UI, state, grid, kill board, and animations
  GameSounds.java        Generated tone effects and Windows speech synthesis
```

Older console exercise files remain in the repository root for reference, but the supported application is the JavaFX GUI.
