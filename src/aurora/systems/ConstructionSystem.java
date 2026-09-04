package aurora.systems;

import mindustry.ai.types.BuilderAI;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.entities.units.BuildPlan;
import mindustry.type.ItemStack;
import aurora.core.AuroraProfile;
import aurora.core.AuroraLearning;

/** Construction mechanics with reservation-aware affordability and adaptive local search. */
public final class ConstructionSystem {
    private ConstructionSystem(){}

    public static void update(Unit unit, BuilderAI builder){
        builder.unit(unit);
        builder.updateMovement();
    }

    public static boolean affordable(Unit unit, Block block){
        if(unit == null || block == null) return false;
        for(ItemStack req : block.requirements){
            int reserved = reserved(unit, req.item);
            if(unit.team().items().get(req.item) - reserved < req.amount) return false;
        }
        return true;
    }

    /** Exposes Aurora's own queued reservation so planning and recovery share the same accounting. */
    public static int reservedForAurora(Unit unit, mindustry.type.Item item){
        return reserved(unit, item);
    }

    private static int reserved(Unit unit, mindustry.type.Item item){
        int total = 0;
        if(unit.plans == null) return 0;
        for(BuildPlan plan : unit.plans){
            if(plan.block == null || plan.block.requirements == null) continue;
            for(ItemStack req : plan.block.requirements) if(req.item == item) total += req.amount;
        }
        return total;
    }

    public static boolean queuedAt(Unit unit, Block block, int x, int y){
        if(unit == null || unit.plans == null) return false;
        for(BuildPlan plan : unit.plans){
            if(plan.x == x && plan.y == y && plan.block == block) return true;
        }
        return false;
    }

    public static boolean place(Unit unit, Block block, int x, int y, int rotation){
        return queue(unit, block, x, y, rotation);
    }

    public static boolean queue(Unit unit, Block block, int x, int y, int rotation){
        if(unit == null || block == null) return false;
        if(queuedAt(unit, block, x, y)) return true;
        if(!mindustry.world.Build.validPlace(block, unit.team(), x, y, rotation)) return false;
        if(!affordable(unit, block)) return false;
        unit.plans.add(new BuildPlan(x, y, rotation, block));
        return true;
    }

    public static boolean queueNearby(Unit unit, Block block, int x, int y, int rotation, int radius){
        if(unit == null || block == null || radius < 0) return false;
        if(queue(unit, block, x, y, rotation)) return true;
        for(int r = 1; r <= radius; r++){
            for(int dx = -r; dx <= r; dx++){
                int dy = r - Math.abs(dx);
                if(queue(unit, block, x + dx, y + dy, rotation)) return true;
                if(dy != 0 && queue(unit, block, x + dx, y - dy, rotation)) return true;
            }
        }
        return false;
    }

    public static Block queueWithAlternatives(Unit unit, Block[] blocks, int x, int y, int rotation, int radius){
        return queueWithAlternatives(unit, blocks, x, y, rotation, radius, null);
    }

    /** Learns which alternative tends to succeed and tries the best-known option first. */
    public static Block queueWithAlternatives(Unit unit, Block[] blocks, int x, int y, int rotation, int radius, AuroraProfile profile){
        if(blocks == null) return null;
        Block[] ordered = blocks.clone();
        if(profile != null){
            java.util.Arrays.sort(ordered, (a, b) -> Float.compare(
                profile.learning.confidence("build:" + b.name), profile.learning.confidence("build:" + a.name)));
        }
        for(Block block : ordered){
            if(block == null) continue;
            if(queueNearby(unit, block, x, y, rotation, radius)){
                if(profile != null) profile.learning.record("build:" + block.name, AuroraLearning.Outcome.SUCCESS);
                return block;
            }
            // Do not punish a strategy for temporary resource shortage. Only learn a negative
            // lesson when Aurora actually had the materials and placement was the blocker.
            if(profile != null && affordable(unit, block)) profile.learning.record("build:" + block.name, AuroraLearning.Outcome.STRATEGY_FAILURE);
        }
        return null;
    }

    public static boolean hasWork(Unit unit){ return unit != null && unit.plans != null && unit.plans.size > 0; }

}
