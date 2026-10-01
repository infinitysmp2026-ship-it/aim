# Auto Aim (Fabric, Minecraft 1.21.11, client-side)

Hard aim-lock: locks the camera onto a player within range (priority), otherwise the closest mob.
Client-side only; nothing is needed on the server. Uses Yarn mappings (the last obfuscated MC version).

**No anti-cheat evasion is included.** The lock is instant and perfectly accurate, which servers
detect easily. Use it in singleplayer, on your own server, or where the server owner allows it.
On most public servers it will get you banned.

## Project layout
```
autoaim-mod/
├── build.gradle / settings.gradle / gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── src/main/
    ├── java/com/example/autoaim/
    │   ├── AutoAimClient.java     keybind, config load, tick hook
    │   ├── AutoAimManager.java    enabled state, current target, per-tick logic
    │   ├── AutoAimConfig.java     config/autoaim.json
    │   ├── TargetSelector.java    validation + player>mob, closest-first
    │   ├── AimController.java     yaw/pitch math + applying rotation
    │   └── mixin/EntityMixin.java blocks mouse look for the local player while locked
    └── resources/
        ├── fabric.mod.json
        ├── autoaim.client.mixins.json
        └── assets/autoaim/lang/en_us.json
```

## Build
1. Install JDK 21.
2. The Gradle wrapper JAR is not included. In the project folder run `gradle wrapper --gradle-version 9.2.1`
   (needs Gradle installed), or copy `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`
   from a fresh project made at https://fabricmc.net/develop/template/ (pick 1.21.11).
3. Check the versions in `gradle.properties` against https://fabricmc.net/develop/ for 1.21.11.
4. `./gradlew build` (Windows: `gradlew.bat build`). Output: `build/libs/autoaim-1.0.0.jar`
   (not the `-sources` jar).

## Install
1. Install Fabric Loader for 1.21.11 from https://fabricmc.net/use/installer/ and launch once.
2. Put Fabric API for 1.21.11 in `.minecraft/mods`.
3. Put `autoaim-1.0.0.jar` in the same folder.
4. Start the Fabric 1.21.11 profile.
5. Options > Controls > Key Binds > "Auto Aim" > "Toggle Auto Aim" (default Arrow Up; rebind freely).

## Config (`.minecraft/config/autoaim.json`, created on first launch)
`aimRange` (10.0), `playerPriority` (true), `playerPreemptsMob` (true), `targetPlayers`, `targetMobs`, `showTargetName`.

## Behavior notes
- Starts OFF each launch. OFF = no scanning, no rotation, no mouse blocking.
- Lock is sticky: stays until the target dies, is removed, leaves range, or you toggle off.
- Exception: with `playerPreemptsMob`, a player entering range replaces a *mob* lock (checked every 4 ticks).
  A player lock is never replaced by another player. Set it to false for strictly sticky locks.
- Range is measured from your position to the target's position (squared distance, no sqrt).
- Aim point is the target's eye height, clamped inside its hitbox.
- Rotation is applied once per client tick (start of tick, before the rotation packet is sent). Mouse
  input is cancelled while locked. Armor stands and spectators are ignored.
- Lock clears on disconnect, world change, and dimension change.

## Testing checklist
- [ ] Arrow Up toggles; action bar shows "Auto Aim: ON/OFF"
- [ ] Rebind to another key; it works and Arrow Up no longer does
- [ ] Zombie + second player (use an alt or LAN friend): player chosen even if farther
- [ ] Player at 11 blocks, zombie at 5: zombie chosen
- [ ] Only mobs: closest one chosen; a slightly closer mob later does not steal the lock
- [ ] Walk past 10 blocks: lock releases, next valid target picked
- [ ] Mouse cannot pull the crosshair off while locked
- [ ] Target dies: lock moves on; no target: camera free
- [ ] You are never targeted; armor stands ignored
- [ ] Log out / change dimension: no crash, lock cleared
- [ ] Disabled: camera behaves vanilla

## Troubleshooting
- **Won't compile (cannot find symbol):** a mapping name differs. Check the Yarn version in
  `gradle.properties`; the likeliest candidates are `KeyBinding.Category`, `getEyePos`, `getEyeY`,
  `getEntitiesByClass`.
- **Crash at launch with a mixin error (`changeLookDirection` not found):** `defaultRequire: 1` makes a
  missing injection point fatal. Check the method name/signature in Entity with `./gradlew genSources`.
- **Loader/Fabric API mismatch:** use Loader >= 0.18 and Fabric API built for 1.21.11.
- **Mod not loading:** wrong jar (`-sources`), wrong profile, or missing Fabric API.
- **Keybind missing:** check `en_us.json` keys, and that the mod loaded (Mods list / latest.log).
- **Camera not locking:** another mod may also cancel or rewrite look input. On servers, anti-cheat or
  server-side rotation checks may snap you back or kick you.
- **Yarn vs Mojang names:** if you move to Minecraft 26.1+, names change and the build needs porting.
