# Aurora AI Companion 2.0.4.6

Aurora 2.0.4.6 is a cross-platform Mindustry JVM mod targeting Mindustry 159.7 for desktop and Android.

## What changed from 2.0.0

- Fixed the duplicated `drag` assignment in AuroraContent.
- Stabilized the default unit controller so AuroraBrain remains active while player possession remains available.
- Added automatic ownership assignment on Aurora creation and world load, without overwriting saved owners.
- Commands now prefer the owner's Aurora, preventing cross-owner command mixups.
- Renamed the copper-specific mining task to the generic `MINE_RESOURCE` task while preserving its save ordinal.
- Aurora selects the lowest-stock mineable resource with a nearby ore source instead of assuming copper.
- Prepared the unit sprite at 48x48 for cleaner atlas integration.
- Kept all gameplay behavior on the main Mindustry thread.
- Replaced the nearest-player pseudo-owner with an explicit owner system.
- Moved decisions into a dedicated AuroraBrain with a 0.80s thinking budget.
- Added Spanish chat commands and owner-only order validation.
- Added proposal/confirmation flow for a local defensive wall + turret plan.
- Construction plans are queued incrementally instead of flooding the unit plan queue.
- Added a real task/state model: follow, wait, mine resources, build, repair, defend and return to core.
- Split movement, mining, construction, repair, combat and communication into independent systems.
- Added contextual event -> interpretation -> mood communication with episodic memory.
- Added tactical combat with target scoring, core defense and retreat behavior.
- Added real economy flow analysis using item flow rates and power graph satisfaction.
- Added intelligent construction checks, duplicate-plan prevention and local fallback placement.
- Added coarse exploration memory and autonomous unvisited-sector exploration.
- Kept the external-AI boundary isolated for a future 4.0 implementation.
- Added cleanup at world-load start so save data can restore owners after entities are loaded.
- Added custom save chunks that persist owner IDs, runtime state, episodic memory and exploration memory.
- Removed the per-update AuroraAIContext allocation from the hot path.
- Replaced protected AIController vector access with a reusable MovementSystem vector.
- Switched the packaged mod metadata from mod.json to the current mod.hjson format.

## Aurora 2.0.4.6 - Estabilización profunda y prueba real

- Eliminó el último fallback de ejecución que forzaba cobre cuando Aurora minaba sin un recurso de recuperación explícito.
- Aurora descubre automáticamente todos los minerales que Mindustry registra como minerales de mapa y que caben dentro del rango de dureza de los taladros vanilla.
- La minería directa usa `mineTier = 5` y `mineHardnessScaling`, por lo que cobre, plomo, carbón, grafito, titanio, torio, tungsteno y otros minerales compatibles no están codificados uno por uno.
- Aurora también puede localizar minerales en paredes además de vetas de suelo.
- La construcción autónoma de extracción selecciona el taladro mínimo apropiado por dureza: mechanical, pneumatic, laser o blast, respetando desbloqueo y materiales.
- El analizador económico dejó de tratar el cobre como único recurso: observa el mineral presente con menor stock y mide su flujo real.
- Se añadieron todos los taladros al conteo de infraestructura de extracción.
- Nuevo comando `/aurora estado` para comprobar dueño, tarea, estado, controlador, recurso elegido y taladro requerido durante la prueba.
- Se mantienen los chunks de guardado y los ordinales de tareas para conservar compatibilidad.

## Owner and commands

Aurora can be assigned to a specific allied player:

```text
/aurora dueña PlayerName
```

The owner can then issue:

```text
/aurora seguir
/aurora espera
/aurora mina
/aurora repara
/aurora defiende
/aurora nucleo
/aurora explora
/aurora recuerda
/aurora construye <bloque> <x> <y> [rotación]
/aurora propone muralla
/aurora si
/aurora no
```

Commands deliberately use Mindustry's official `CommandHandler`, so they remain usable from desktop and Android clients without a platform-specific UI dependency.

## Architecture

```text
src/aurora/
├── AuroraMod.java
├── ai/
│   └── AuroraAI.java
├── command/
│   └── AuroraCommands.java
├── content/
│   └── AuroraContent.java
├── core/
│   ├── AuroraMemory.java
│   ├── ExplorationMemory.java
│   ├── AuroraProfile.java
│   ├── AuroraRegistry.java
│   ├── AuroraState.java
│   └── AuroraTask.java
├── systems/
│   ├── CombatSystem.java
│   ├── CommunicationSystem.java
│   ├── ConstructionSystem.java
│   ├── MiningSystem.java
│   ├── MovementSystem.java
│   └── RepairSystem.java
├── analysis/
│   ├── BottleneckAnalyzer.java
│   └── EconomyAnalyzer.java
├── personality/
│   └── AuroraMood.java
└── bridge/
    └── ExternalAIProvider.java
```

## Design notes

The AI controller is an orchestrator only. Every behavior is kept behind a small system so later versions can replace local heuristics without rewriting Aurora's content layer.

Aurora ownership is stored in a named custom save chunk inside each Mindustry save. The chunk restores owner IDs after saved entities are loaded, so different save slots can retain different Aurora owners. Network transmission of this chunk is disabled because 2.0.4.6 keeps the save chunk local and is designed primarily for single-player use.

Mining and construction delegate mechanics to Mindustry's official `MinerAI`, `BuilderAI` and `Build` APIs. This avoids reimplementing core game mechanics.

The threat model is deliberately conservative: Aurora treats an enemy with more than twice its maximum health as dangerous and falls back rather than charging it.

## Performance rules

- Decision evaluation is throttled to 1.25 times per second (0.80s interval) to reduce CPU pressure on mobile devices.
- No full-map scans are performed.
- Searches use Mindustry's indexed helpers where available.
- No blocking networking or file I/O occurs during gameplay.
- No external AI is contacted by 2.0.
- Communication is rate-limited.

## Future versions

### 2.1
Targeted build-plan queue, better order UI, owner management and broader resource planning.

### 2.5
Bottleneck analyzer with budgeted sampling and actionable recommendations.

### 3.0
Richer personality, preferences, long-term habits and player-configurable tone.

### 4.0
Optional external AI provider behind a strict asynchronous bridge, cached prompts, timeouts and local fallback.


## Important: runtime artifact

Aurora is a Java/JVM mod. The project ZIP contains source code and assets; Mindustry must receive the compiled `AuroraAICompanionDesktop.jar` to execute the Java entry point. The 2.0.4.6 build script now packages `src/assets` correctly, and `.github/workflows/build.yml` can compile the JAR automatically with JDK 17.

For the exact installation/test sequence, see `BUILD-AND-INSTALL-v2.0.4.6.md`.

## Build

```text
./gradlew jar
```

The project uses JDK 17 and keeps Mindustry dependencies compile-only.

## Refined development prompt

See `PROMPT-v2.0.0-refined.md` for the tightened specification used for the 2.0 implementation.

## Verification status

The source was also checked for the main compile risks found during review: protected `AIController` vector access, unnecessary hot-path context allocation, save-load clear timing, and packaged metadata. The custom save chunk uses Mindustry's official `SaveVersion.addCustomChunk` mechanism.

The project source was statically reviewed against the public Mindustry 159.7 API documentation. A local Gradle wrapper is not included in the original 1.0 project, and this environment could not fetch external Gradle dependencies, so a final dependency-resolved compilation could not be performed here.



## 2.0.4.6: disponibilidad física y prueba rápida
- La unidad física también tiene una definición `content/aurora.hjson`. Esta capa garantiza que el cuerpo y la receta de Air Factory sean visibles como contenido de Mindustry incluso antes de ejecutar la lógica Java.
- El JAR sigue siendo necesario para AuroraBrain, dueño, memoria, aprendizaje y comandos.
- En una partida personalizada, usa `/aurora invoca` para crearla sin depender de la fábrica ni de la investigación.
