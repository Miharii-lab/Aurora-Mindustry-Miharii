package aurora.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.io.SaveFileReader;
import aurora.personality.AuroraMood;
import aurora.planning.DefenseProposal;

/** Persists Aurora's runtime state, episodic memory and exploration memory. */
public final class AuroraStateSaveData implements SaveFileReader.CustomChunk {
    public static final String CHUNK_NAME = "aurora.state.v2";
    private static final int VERSION = 5;
    private static final int MAX_ENTRIES = 10000;

    @Override
    public void write(DataOutput out) throws IOException{
        int count = 0;
        for(Unit unit : Groups.unit) if(AuroraRegistry.isAurora(unit)) count++;
        out.writeInt(VERSION); out.writeInt(count);
        for(Unit unit : Groups.unit){
            if(!AuroraRegistry.isAurora(unit)) continue;
            AuroraProfile p = AuroraRegistry.profile(unit);
            out.writeInt(unit.id()); out.writeInt(p.ownerId); out.writeInt(p.task.ordinal()); out.writeInt(p.state.ordinal());
            out.writeFloat(p.targetX); out.writeFloat(p.targetY); out.writeInt(p.buildBlockId);
            out.writeBoolean(p.manualOrder); out.writeBoolean(p.autonomousDefense); out.writeInt(p.taskBeforeDefense.ordinal());
            out.writeBoolean(p.manualOrderBeforeDefense); out.writeBoolean(p.returningForInventory);
            out.writeInt(p.taskBeforeRepair.ordinal()); out.writeBoolean(p.manualOrderBeforeRepair);
            out.writeInt(p.recoveryTask.ordinal()); out.writeInt(p.recoveryItemId);
            out.writeUTF(p.lastObservationCategory == null ? "ok" : p.lastObservationCategory); out.writeInt(p.mood.ordinal());
            out.writeBoolean(p.pendingProposal != null); if(p.pendingProposal != null) p.pendingProposal.write(out);
            out.writeInt(p.memory.size());
            for(int i = 0; i < p.memory.size(); i++){
                AuroraMemory.Episode e = p.memory.get(i);
                out.writeUTF(e.category()); out.writeUTF(e.text()); out.writeFloat(e.x()); out.writeFloat(e.y());
            }
            out.writeInt(p.learning.size());
            for(AuroraLearning.Entry e : p.learning.entries()){
                out.writeUTF(e.key()); out.writeInt(e.attempts()); out.writeInt(e.successes()); out.writeInt(e.failures());
            }
            int[] visited = p.exploration.raw().toArray();
            out.writeInt(Math.min(visited.length, 512));
            for(int i = 0; i < visited.length && i < 512; i++) out.writeInt(visited[i]);
        }
    }

    @Override
    public void read(DataInput in) throws IOException{
        int version = in.readInt();
        if(version < 1 || version > VERSION) throw new IOException("Unsupported Aurora state version: " + version);
        int count = in.readInt();
        if(count < 0 || count > MAX_ENTRIES) throw new IOException("Invalid Aurora state count: " + count);
        for(int i = 0; i < count; i++){
            int unitId = in.readInt();
            AuroraProfile p = profileFor(unitId);
            int ownerId = in.readInt(), task = in.readInt(), state = in.readInt();
            float targetX = in.readFloat(), targetY = in.readFloat();
            int buildBlockId = in.readInt();
            boolean manualOrder = in.readBoolean(), autonomousDefense = in.readBoolean();
            int taskBeforeDefense = in.readInt();
            boolean manualOrderBeforeDefense = in.readBoolean(), returning = in.readBoolean();
            int taskBeforeRepair = version >= 5 ? in.readInt() : AuroraTask.FOLLOW.ordinal();
            boolean manualOrderBeforeRepair = version >= 5 && in.readBoolean();
            int recoveryTask = version >= 4 ? in.readInt() : AuroraTask.FOLLOW.ordinal();
            int recoveryItemId = version >= 4 ? in.readInt() : -1;
            String observation = in.readUTF(); int mood = version >= 2 ? in.readInt() : 0;
            boolean hasProposal = version >= 2 && in.readBoolean();

            int memoryCount = 0, visitCount = 0, learningCount = 0;
            if(version >= 3){
                memoryCount = in.readInt();
                if(memoryCount < 0 || memoryCount > AuroraMemory.MAX_EVENTS) throw new IOException("Invalid Aurora memory count");
            }
            if(p == null){
                if(hasProposal) DefenseProposal.read(in);
                for(int m = 0; m < memoryCount; m++){ in.readUTF(); in.readUTF(); in.readFloat(); in.readFloat(); }
                if(version >= 4){ learningCount = in.readInt(); for(int l = 0; l < learningCount; l++){ in.readUTF(); in.readInt(); in.readInt(); in.readInt(); } }
                if(version >= 3){ visitCount = in.readInt(); if(visitCount < 0 || visitCount > 512) throw new IOException("Invalid exploration memory count"); for(int v=0; v<visitCount; v++) in.readInt(); }
                continue;
            }
            p.ownerId = ownerId; p.task = safeTask(task); p.state = safeState(state); p.targetX = targetX; p.targetY = targetY;
            p.buildBlockId = buildBlockId; p.buildTarget = null; p.manualOrder = manualOrder; p.autonomousDefense = autonomousDefense;
            p.taskBeforeDefense = safeTask(taskBeforeDefense); p.manualOrderBeforeDefense = manualOrderBeforeDefense; p.returningForInventory = returning;
            p.taskBeforeRepair = safeTask(taskBeforeRepair); p.manualOrderBeforeRepair = manualOrderBeforeRepair;
            p.recoveryTask = safeTask(recoveryTask); p.recoveryItemId = recoveryItemId;
            p.lastObservationCategory = observation == null || observation.isEmpty() ? "ok" : observation; p.mood = safeMood(mood);
            p.pendingProposal = hasProposal ? DefenseProposal.read(in) : null;
            if(version >= 3){
                p.memory.clear();
                for(int m = 0; m < memoryCount; m++) p.memory.remember(in.readUTF(), in.readUTF(), in.readFloat(), in.readFloat());
                if(version >= 4){
                    learningCount = in.readInt();
                    if(learningCount < 0 || learningCount > AuroraLearning.MAX_LESSONS) throw new IOException("Invalid Aurora learning count");
                    arc.struct.Seq<AuroraLearning.Entry> lessons = new arc.struct.Seq<>();
                    for(int l = 0; l < learningCount; l++) lessons.add(new AuroraLearning.Entry(in.readUTF(), in.readInt(), in.readInt(), in.readInt()));
                    p.learning.restore(lessons);
                }
                visitCount = in.readInt();
                if(visitCount < 0 || visitCount > 512) throw new IOException("Invalid exploration memory count");
                int[] keys = new int[visitCount]; for(int v=0; v<visitCount; v++) keys[v] = in.readInt();
                p.exploration.restore(keys);
            }
        }
    }

    private static AuroraProfile profileFor(int unitId){
        Unit unit = Groups.unit.getByID(unitId);
        return unit != null && AuroraRegistry.isAurora(unit) ? AuroraRegistry.profile(unit) : null;
    }
    private static AuroraTask safeTask(int ordinal){ AuroraTask[] v = AuroraTask.values(); return ordinal >= 0 && ordinal < v.length ? v[ordinal] : AuroraTask.FOLLOW; }
    private static AuroraState safeState(int ordinal){ AuroraState[] v = AuroraState.values(); return ordinal >= 0 && ordinal < v.length ? v[ordinal] : AuroraState.IDLE; }
    private static AuroraMood safeMood(int ordinal){ AuroraMood[] v = AuroraMood.values(); return ordinal >= 0 && ordinal < v.length ? v[ordinal] : AuroraMood.CALM; }
    @Override public boolean writeNet(){ return false; }
}
