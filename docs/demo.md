# Demo Guide

## Quick Start

From the project directory:

```powershell
.\run-gui.bat
```

If running manually:

```powershell
$env:JAVA_HOME='C:\Users\rpuro\.jdks\temurin-24.0.2'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat run
```

## What To Show

1. Start on Easy mode to demonstrate the full loop with 15 torpedoes.
2. Click several grid cells to show miss markers, torpedo animation, and explosion effects.
3. Continue until a submarine segment appears on a hit.
4. Point out the fleet-specific submarine colors:
   - Japanese Fleet: gray hull with red accent
   - English Fleet: blue-gray hull with white accent
   - German Fleet: charcoal hull with white detail
5. Show the kill board updating hits for the fleet that was struck.
6. Sink all three cells of one fleet to show the kill count changing from `0` to `1`.
7. Switch levels and restart to show torpedo limits:
   - Easy: 15
   - Medium: 13
   - High: 10

## Expected Effects

- A launch sound plays when a torpedo is fired.
- A miss tone plays on water.
- A hit tone plays when a submarine is found.
- A sunk tone plays when an entire fleet is destroyed.
- The admiral voice line plays on fleet hits and fleet sinks.

## Demo Notes

Fleet placement is randomized on every new battle, so the first hit location changes between runs.
