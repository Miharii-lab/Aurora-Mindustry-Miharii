package aurora.systems;

import mindustry.ai.types.AIController;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.meta.BlockFlag;

/** Finds and prioritizes repair targets. */
public final class RepairSystem {
    private RepairSystem(){}

    public static Building findTarget(Unit unit, float range){
        Building best = null;
        float bestScore = -1f;
        for(Building b : unit.team().data().buildings){
            if(b == null || !b.isValid() || b.health() >= b.maxHealth() || !unit.within(b, range)) continue;
            float healthRatio = 1f - b.health() / Math.max(1f, b.maxHealth());
            float priority = healthRatio;
            if(b.block.flags.contains(BlockFlag.core)) priority += 5f;
            if(b.block.flags.contains(BlockFlag.turret)) priority += 4f;
            if(b.block.flags.contains(BlockFlag.generator)) priority += 3f;
            if(b.block.flags.contains(BlockFlag.factory)) priority += 2f;
            if(b.block.flags.contains(BlockFlag.drill)) priority += 1.5f;
            if(priority > bestScore){ bestScore = priority; best = b; }
        }
        return best;
    }

    public static boolean moveToDamaged(AIController ai, Unit unit, float range){
        Building damaged = findTarget(unit, range);
        if(damaged == null) return false;
        ai.moveTo(damaged, 7f, 16f);
        return true;
    }
}
