# Aurora AI Companion 2.0.3.0

Major intelligence pass focused on fixing remaining reliability issues and making Aurora feel continuous rather than reactive.

## Fixed
- Defense proposal entries now try nearby fallback positions before being skipped.
- Construction queue refuses duplicate plans and checks real team affordability.
- Resource deposit no longer risks deleting a partially accepted stack.

## New: episodic memory
- Aurora keeps a bounded journal of recent economy, combat, repair, planning and exploration events.
- Memory is persisted in the Aurora save chunk.
- `/aurora recuerda` exposes the latest remembered episode.

## New: exploration memory
- Aurora divides the map into coarse sectors and remembers inspected sectors.
- Autonomous exploration chooses the nearest unvisited valid sector.
- Exploration progress is persisted between saves.
- `/aurora explora` can explicitly start exploration.

## New: tactical combat
- Target selection is scored instead of purely nearest-target.
- Armed and high-health enemies receive higher priority.
- Aurora retreats to the core when critically damaged against a dangerous enemy.
- Core-near threats receive immediate combat priority.

## New: real economy analysis
- Uses item flow rates when available instead of structure counts alone.
- Detects copper drain, copper surplus, logistics starvation and power satisfaction problems.
- Power graph demand/production is considered before recommending expansion.

## New: intelligent construction
- Placement checks remain authoritative through Mindustry's Build.validPlace.
- Construction requires available team resources.
- Duplicate queued plans are ignored.
- Defense plans try local fallback positions when the preferred tile is blocked.

## New: contextual communication
- Communication follows an event -> interpretation -> mood pipeline.
- Important observations are stored in episodic memory.
- Messages are less generic and can explain why Aurora changed priorities.

## Compatibility
- Target remains Mindustry v159.7.
- The custom state chunk keeps its existing name (`aurora.state.v2`) and advances its internal format version to 3 so older saves can still be read.
