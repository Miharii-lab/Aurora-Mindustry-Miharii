package aurora.core;

import mindustry.gen.Building;
import mindustry.gen.Player;
import aurora.personality.AuroraMood;
import aurora.planning.DefenseProposal;

/** Persistent memory/profile for one Aurora. Transient runtime objects are rebuilt after loading. */
public final class AuroraProfile {
    public int ownerId = -1;
    public AuroraTask task = AuroraTask.FOLLOW;
    public AuroraState state = AuroraState.IDLE;
    public float targetX;
    public float targetY;
    public Building buildTarget;
    public int buildBlockId = -1;
    /** A player order is a priority, not a permanent shutdown of Aurora's autonomy. */
    public boolean manualOrder;
    public boolean autonomousDefense;
    public AuroraTask taskBeforeDefense = AuroraTask.FOLLOW;
    public boolean manualOrderBeforeDefense;
    public boolean returningForInventory;
    /** Task interrupted by a repair detour; restored when repairs are finished. */
    public AuroraTask taskBeforeRepair = AuroraTask.FOLLOW;
    public boolean manualOrderBeforeRepair;
    /** If a mission needs a resource, Aurora temporarily mines it and returns to this task. */
    public AuroraTask recoveryTask = AuroraTask.FOLLOW;
    public int recoveryItemId = -1;
    public DefenseProposal pendingProposal;
    public String lastObservationCategory = "ok";
    public AuroraMood mood = AuroraMood.CALM;
    public final AuroraMemory memory = new AuroraMemory();
    public final AuroraLearning learning = new AuroraLearning();
    public final ExplorationMemory exploration = new ExplorationMemory();
    public float lastCommunicationTime = -9999f;
    public String lastEventKey = "";

    public boolean hasOwner(){ return ownerId >= 0; }
    public boolean isOwner(Player player){ return player != null && player.id() == ownerId; }

    public void clearBuild(){
        buildTarget = null;
        buildBlockId = -1;
    }

    public void setTarget(float x, float y){
        targetX = x;
        targetY = y;
    }

    /** Returns the profile to normal autonomous control after a completed manual task. */
    public void resumeAutonomy(){
        manualOrder = false;
        returningForInventory = false;
        recoveryTask = AuroraTask.FOLLOW;
        recoveryItemId = -1;
        taskBeforeRepair = AuroraTask.FOLLOW;
        manualOrderBeforeRepair = false;
    }
}
