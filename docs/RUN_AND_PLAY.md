# Run and Play Guide — Battleship Command Center

## 1. Prerequisites

Install:

- JDK 21 or newer
- Gradle
- IntelliJ IDEA, Cursor, VS Code, or another Java IDE

Check your versions:

```bash
java -version
gradle -version
```

## 2. Open the Project in IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Select **File → Open**.
3. Choose the cloned `Java-Games--battleship` repository folder.
4. When prompted, open it as a **Gradle project**.
5. Wait for Gradle sync to finish.
6. Confirm the project SDK is JDK 21 or newer under **File → Project Structure → Project SDK**.

## 3. Open the Project in Cursor

1. Open Cursor.
2. Select **Open Folder**.
3. Choose the `Java-Games--battleship` repository folder.
4. Let Cursor index the project.
5. Open the integrated terminal.
6. Run:

```bash
gradle run
```

## 4. Run from Any IDE Terminal

From the repository root:

```bash
gradle run
```

The JavaFX window should open with the title:

```text
Battleship Command Center — JavaFX Edition
```

## 5. Run by Main Class

After Gradle import, run this class:

```text
io.github.rohanpurohit7.battleship.BattleshipFxApp
```

## 6. How to Play

1. The game starts with three hidden enemy ships.
2. Use the 8x8 tactical ocean grid.
3. Click a coordinate such as `A0`, `B4`, or `H7` to launch a torpedo.
4. The grid updates immediately:
   - white marker = miss
   - red marker = hit
   - gold marker = ship sunk
5. Watch the command console for battle messages.
6. Continue firing until all ships are sunk.
7. Use **New Mission** to restart.
8. Use **Training Reveal** to show hidden ship locations for demo or debugging.

## 7. Rapid Prototyping Ideas

Use Cursor or IntelliJ to quickly modify:

| File | What to Prototype |
|---|---|
| `BattleshipFxApp.java` | board size, ship count, game rules, animations |
| `FleetShip.java` | ship model, hit logic, ship types |
| `battleship.css` | colors, grid style, visual effects |
| `gui-concept.svg` | visual direction and mockup |

## 8. Common Fixes

### JavaFX window does not open

Confirm you opened the project as a Gradle project and that the Gradle JavaFX plugin downloaded dependencies.

### Wrong JDK

Set the SDK to JDK 21 or newer.

### Gradle command not found

Install Gradle or run from an IDE that includes Gradle support.

## 9. Portfolio Demo Script

Say:

> This started as a console-based Java Battleship exercise. I preserved the original source and added a JavaFX GUI with a tactical board, game-state model, animated hit/miss feedback, mission log, and Gradle packaging so it can run in IntelliJ or Cursor as a desktop Java application.
