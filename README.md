# Player Totem Skin (Fabric, Minecraft 1.21.11)

Das Totem der Unsterblichkeit zeigt Skin und Rüstung des Spielers, der es benutzt.

## Bauen
1. JDK 21 installieren.
2. In den Projektordner wechseln und einmalig den Gradle-Wrapper erzeugen (Gradle 8.14+ oder 9.x): `gradle wrapper`
3. `./gradlew build` (Windows: `gradlew.bat build`)
4. Die JAR liegt in `build/libs/player-totem-skin-1.0.0.jar` (nicht die `-sources.jar`).

Falls Versionen nicht aufgelöst werden: aktuelle Werte auf https://fabricmc.net/develop in `gradle.properties` eintragen.

## Installieren
1. Fabric Loader und Fabric API für 1.21.11 installieren (Mod Menu optional, aber empfohlen).
2. JAR in `.minecraft/mods` legen. Die Mod ist rein clientseitig, der Server braucht nichts.
3. Einstellungen: Mod Menu -> Player Totem Skin. Konfigurationsdatei: `config/playertotemskin.json`.

## Funktionsweise
- Die Mod hört auf das Totem-Event (Entity-Event 35) und liest den auslösenden Spieler per UUID aus.
- Eine Client-Puppe mit demselben GameProfile nutzt Minecrafts SkinManager (asynchron + gecacht, Classic/Slim, Cape).
- Rüstung, Verzauberungsglanz und Lederfarbe kommen vom normalen Spieler-Renderer (echtes 3D-Modell mit Rüstungsschichten).
- Puppen liegen in einem UUID-LRU-Cache (Größe und Lebensdauer einstellbar) und werden beim Verlassen von Welt/Server geleert.
- Fehlt der Skin und ist "Klassisches Totem" als Fallback gewählt, läuft die normale Vanilla-Animation.
