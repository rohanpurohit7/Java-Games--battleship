# Battleship Command Center — JavaFX Edition

A modernized JavaFX version of the original console Battleship exercise. The original Java classes remain preserved, while the new application adds a polished GUI, animated tactical board, mission log, hit/miss feedback, and IntelliJ/Cursor-friendly Gradle configuration.

## Highlights

- JavaFX tactical ocean grid
- Animated hit, miss, and sunk markers
- Hidden fleet placement with training reveal mode
- Mission statistics and battle log
- Preserved original console implementation
- Gradle project that opens cleanly in IntelliJ IDEA or Cursor

## Navigation

```text
README.md
├── build.gradle                         JavaFX Gradle build
├── settings.gradle                      Gradle project settings
├── docs/
│   └── gui-concept.svg                  Visual concept mockup
├── src/main/java/io/github/rohanpurohit7/battleship/
│   ├── BattleshipFxApp.java             JavaFX GUI application entry point
│   └── FleetShip.java                   GUI game model
├── src/main/resources/styles/
│   └── battleship.css                   Rich tactical UI styling
└── legacy source files                  Original console game classes
```

## Run in IntelliJ IDEA or Cursor

1. Open the repository folder as a Gradle project.
2. Let the IDE import Gradle dependencies.
3. Use JDK 21 or newer.
4. Run the Gradle task:

```bash
./gradlew run
```

On Windows:

```powershell
gradlew.bat run
```

You can also run the main class directly after Gradle import:

```text
io.github.rohanpurohit7.battleship.BattleshipFxApp
```

## UX Concept

Open `docs/gui-concept.svg` to preview the intended interface: a sonar-style board, mission console, action buttons, and battle log.

## Design Narrative

The original project demonstrated object-oriented game logic through a text console. The JavaFX version turns that logic into an interactive desktop game. The board is represented as an 8x8 tactical ocean grid. Each click launches a torpedo. The UI then records a hit, miss, or kill-confirmed event and updates mission statistics.

## Portfolio Value

This project now demonstrates:

- Java object modeling
- event-driven GUI programming
- JavaFX layout and styling
- user interaction design
- game-state management
- Gradle-based Java application packaging
