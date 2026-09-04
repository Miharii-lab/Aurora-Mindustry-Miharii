package aurora;

import arc.Events;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.game.EventType.UnitCreateEvent;
import mindustry.game.EventType.UnitSpawnEvent;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;
import aurora.command.AuroraCommands;
import aurora.content.AuroraContent;
import aurora.core.AuroraRegistry;
import aurora.core.AuroraSaveData;
import aurora.core.AuroraStateSaveData;

/** Main entry point for Aurora AI Companion 2.0.4.6. */
public class AuroraMod extends Mod {
    private static boolean saveChunkRegistered;

    @Override
    public void loadContent(){
        AuroraContent.load();
        if(!saveChunkRegistered){
            SaveVersion.addCustomChunk(AuroraSaveData.CHUNK_NAME, new AuroraSaveData());
            SaveVersion.addCustomChunk(AuroraStateSaveData.CHUNK_NAME, new AuroraStateSaveData());
            saveChunkRegistered = true;
        }
        Log.info("Aurora AI Companion 2.0.4.6 loaded.");

        // Clear before a new world starts loading. The save custom chunk is read after entities load.
        Events.on(EventType.WorldLoadBeginEvent.class, e -> AuroraRegistry.clear());
        Events.on(EventType.ResetEvent.class, e -> AuroraRegistry.clear());

        // A factory does not expose the player who initiated production, so ownership is
        // assigned from the spawned unit's team. In single-player this is unambiguous; in
        // allied multiplayer the nearest allied player becomes the initial owner. Existing
        // owners are never overwritten, including owners restored from a save.
        Events.on(UnitCreateEvent.class, e -> AuroraRegistry.assignOwnerIfNeeded(e.unit));
        Events.on(UnitSpawnEvent.class, e -> AuroraRegistry.assignOwnerIfNeeded(e.unit));
        Events.on(EventType.WorldLoadEvent.class, e -> AuroraRegistry.assignUnownedToNearestPlayers());
    }

    @Override
    public void registerClientCommands(arc.util.CommandHandler handler){
        AuroraCommands.register(handler);
    }
}
