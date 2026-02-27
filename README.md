# AutoPlayTime

Fabric mod for Minecraft 1.20.1 - HolyWorld moderator tool.

## Features

- PlayTime scanning for all players in TAB
- GUI interface (K key) with color coding
- Report parsing from /reportlist GUI
- Moderation tools (spy, find, server switch)
- Delay and activity threshold settings
- Anti-spam command queue

## Color Coding

| Time | Color |
|------|-------|
| < 1h | Red |
| < 3h | Yellow |
| < 10h | Green |
| 10+h | Cyan |

## Build

```
./gradlew build
```

JAR will be in `build/libs/`

## License

MIT
