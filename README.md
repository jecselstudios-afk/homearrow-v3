# Home Arrow

A client-side Fabric mod for Minecraft 1.21.1. Set one home point, press **N** to toggle an arrow on screen that points toward it.

## Commands
- `/sethome` saves your current position as home (dimension included)
- `/clearhome` removes the saved home

## Controls
- **N** toggles the arrow (rebindable in Controls > Home Arrow)

## Requirements
- Minecraft 1.21.1
- Fabric Loader 0.16.x or newer
- Fabric API
- Java 21

Install by putting the built jar from `build/libs/` (or the Actions artifact) into your `mods` folder. Client-side only, so it works on any server.

## Building
```
gradle build
```
The jar appears in `build/libs/`. GitHub Actions builds it automatically on every push.
