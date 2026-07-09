# Legacy Source Archive

This folder keeps older Battleship implementations and inactive JavaFX copies for reference only.

The active runtime build is controlled by `build.gradle`:

```groovy
sourceSets {
    main {
        java {
            include 'navalbattle/**'
        }
    }
}

application {
    mainClass = 'navalbattle.BattleshipFxApp'
}
```

## Active Runtime

- Entry point: `navalbattle.BattleshipFxApp`
- Active source root: `src/main/java/navalbattle`
- Active sound helper: `navalbattle.GameSounds`

## Legacy Areas

- `legacy/console`: original console exercise classes.
- `legacy/inactive-javafx`: older JavaFX package variants that are not included by Gradle.

These files should not be imported back into the active build unless they are deliberately refactored, tested, and wired into `build.gradle`.
