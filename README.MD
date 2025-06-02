
# iTEM

[![Build Status](https://ci.thom.club/job/TheExoticsMod/job/master/badge/icon)](https://ci.thom.club/job/TheExoticsMod/job/master/) [![Discord](https://img.shields.io/discord/932106421338779709?label=discord&logo=Discord&logoColor=FFFFFF%22)](https://discord.gg/bUE3r3Jckc) [![Contributors](https://img.shields.io/github/contributors/TGWaffles/TheExoticsMod?&logo=GitHub)](https://github.com/TGWaffles/TheExoticsMod/graphs/contributors) [![Jenkins Coverage](https://img.shields.io/jenkins/coverage/jacoco?jobUrl=https%3A%2F%2Fci.thom.club%2Fjob%2FTheExoticsMod%2Fjob%2Fmaster)](https://ci.thom.club/job/TheExoticsMod/job/master/jacoco/) [![Jenkins tests](https://img.shields.io/jenkins/tests?compact_message&jobUrl=https%3A%2F%2Fci.thom.club%2Fjob%2FTheExoticsMod%2Fjob%2Fmaster)](https://ci.thom.club/job/TheExoticsMod/job/master/lastBuild/testReport/)

---

__What is iTEM?__

iTEM is an open source, community-led effort to track every single item in Hypixel Skyblock.

By contributing to the effort, you earn "contributions" (because you contributed), and using the contributions you can search the iTEM database for (currently) exotic armour sets, applied pet skins, and unapplied pet skins. In the future, you will be able to search for any item with any attribute you want.

iTEM uses this data to be able to provide "active dupe checking", item ownership history, quick access to item nbt, hypixel searches, item statistics/rarity, and more to come.

It also provides handy in-game features, such as lobby scanning for exotics, exotic labelling (eg fairy/spooky/exotic/glitched), copying item uuid to clipboard, dupe-checking via a hotkey, armour rarity annotations (how many there are in the game), item exports (getting a machine-readable list of all items in your chests/item frames), seymour comparisons (how similar a seymour piece is to skyblock armor pieces), and more to come.

We currently track:
- ~314 million items
- ~20 million pets
- ~18 million players

Unfortunately, in February 2023, the Hypixel Staff team announced in [a forum post](https://hypixel.net/threads/public-api-changes-february-2023.5266129/) that the Hypixel API had grown too large and had become too costly to continue operating as it was. This led to the removal of player-based API keys, which effectively ended iTEM as we knew it. iTEM now continues to operate as a quality-of-life mod with several useful features for collectors, such as Seymour comparison, chest and item frame searching, item exports, and more. TGWaffles later created [a video recapping the history of iTEM](https://youtu.be/_DDT3SC29ug)

[Join the Discord!](https://discord.gg/bUE3r3Jckc)

---
## How to build?

### Windows

```bash
gradlew.bat clean
gradlew.bat setupDecompWorkspace
gradlew.bat test -i
gradlew.bat reobfShadowJar
```

### Linux

```bash
./gradlew clean
./gradlew setupDecompWorkspace
./gradlew test -i
./gradlew reobfShadowJar
```

### Protoc Files

```bash
protoc -I . --java_out annotate_code:src/main/java/ clientMessages.proto
protoc -I . --java_out annotate_code:src/main/java/ serverMessages.proto
```
