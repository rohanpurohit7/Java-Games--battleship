# Architecture Notes

## Application Structure

The active JavaFX application lives in:

```text
src/main/java/navalbattle/
```

The main classes are:

- `BattleshipFxApp`: JavaFX scene construction, game state, grid cell rendering, fleet placement, scoring, animations, and mission rules.
- `GameSounds`: generated sound effects and Windows speech synthesis.

## Build Configuration

The Gradle application entry point is:

```text
navalbattle.BattleshipFxApp
```

The build output is configured to use:

```text
build-naval/
```

This avoids stale generated output from earlier JavaFX package paths that were affected by OneDrive placeholder/reparse behavior.

## Gameplay Model

The board is a 7x7 grid. Each fleet occupies three cells. Fleet placement is randomized at the start of each new battle and validated so fleets do not overlap or extend beyond the board.

The game tracks:

- fired shots
- remaining torpedoes
- hit positions per fleet
- sunk fleet count
- mission over state

## Rendering Model

Each grid cell is a JavaFX `StackPane` containing:

- water rectangle
- submarine hull ellipse
- submarine tower rectangle
- accent stripe
- portholes
- miss marker

Submarine shapes are hidden until the cell is hit. When a fleet is sunk, all three submarine segments for that fleet are revealed.

## Audio Model

Sound effects are generated at runtime with `javax.sound.sampled.SourceDataLine`. This keeps the project self-contained and avoids external audio assets.

Windows speech synthesis is invoked through PowerShell and `System.Speech.Synthesis.SpeechSynthesizer`.

## Known Environment Notes

This project currently targets JDK 24 because that is the installed local JDK discovered on the machine:

```text
C:\Users\rpuro\.jdks\temurin-24.0.2
```

The helper launcher `run-gui.bat` sets `JAVA_HOME` to that local JDK before running Gradle.
