package aurora.systems;

import arc.struct.Seq;
import mindustry.ai.types.MinerAI;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;

/**
 * Generic mining helpers. Actual extraction remains Mindustry's official MinerAI.
 * Aurora can mine every ore supported by the current content up to the highest
 * vanilla drill tier, while automated drill construction selects the proper tier.
 */
public final class MiningSystem{
    private MiningSystem(){}

    public static void update(Unit unit, MinerAI miner){
        if(unit == null || miner == null) return;
        miner.unit(unit);
        miner.targetItem = preferredResource(unit);
        miner.updateMovement();
    }

    public static void update(Unit unit, MinerAI miner, Item target){
        if(unit == null || miner == null) return;
        miner.unit(unit);
        miner.targetItem = target != null && canMine(unit, target) ? target : preferredResource(unit);
        miner.updateMovement();
    }

    /** Finds the closest floor ore, falling back to a wall ore when enabled. */
    public static Tile ore(Unit unit, Item item){
        if(unit == null || item == null || !canMine(unit, item)) return null;
        Tile floor = unit.type.mineFloor ? mindustry.Vars.indexer.findClosestOre(unit, item) : null;
        if(floor != null) return floor;
        return unit.type.mineWalls ? mindustry.Vars.indexer.findClosestWallOre(unit, item) : null;
    }

    /** Whether Aurora's mining tool can handle this item's hardness. */
    public static boolean canMine(Unit unit, Item item){
        return unit != null && item != null && unit.type != null && item.hardness <= unit.type.mineTier;
    }

    /** Picks the currently reachable ore with the lowest team stock. */
    public static Item preferredResource(Unit unit){
        if(unit == null || unit.type == null || unit.type.mineItems == null || unit.type.mineItems.isEmpty()) return null;
        Item best = null;
        int bestStock = Integer.MAX_VALUE;
        for(Item item : unit.type.mineItems){
            if(item == null || !canMine(unit, item) || ore(unit, item) == null) continue;
            int stock = unit.team().items().get(item);
            if(best == null || stock < bestStock){
                best = item;
                bestStock = stock;
            }
        }
        return best;
    }

    /** Returns every ore known to Mindustry, including ores added by other mods. */
    public static Seq<Item> allMineableOres(){
        Seq<Item> ores = new Seq<>();
        for(Item item : Item.getAllOres()){
            if(item != null && item.hardness >= 0 && !ores.contains(item)) ores.add(item);
        }
        return ores;
    }

    /** Returns the lowest drill tier currently capable of extracting an item. */
    public static int requiredDrillTier(Item item){
        Drill drill = drillForItem(item);
        return drill == null ? Integer.MAX_VALUE : drill.tier;
    }

    /**
     * Selects the lowest-tier unlocked Drill capable of extracting the item.
     * This intentionally scans the content registry instead of hard-coding the
     * four Serpulo drills, so Erekir and compatible modded drills are eligible too.
     */
    public static Drill drillForItem(Item item){
        if(item == null) return null;
        Drill best = null;
        for(Block block : mindustry.Vars.content.blocks()){
            if(!(block instanceof Drill drill)) continue;
            if(!drill.unlockedNow()) continue;
            if(item.hardness > drill.tier) continue;
            if(drill.blockedItems != null && drill.blockedItems.contains(item)) continue;
            if(mindustry.Vars.state != null && !drill.supportsEnv(mindustry.Vars.state.rules.env)) continue;
            if(best == null || drill.tier < best.tier || (drill.tier == best.tier && drill.id < best.id)) best = drill;
        }
        return best;
    }

    /** Queues only a drill currently unlocked and capable of extracting the resource. */
    public static boolean queueResourceDrill(Unit unit, Item item){
        Tile ore = ore(unit, item);
        Drill drill = drillForItem(item);
        if(ore == null || drill == null) return false;
        return ConstructionSystem.queueNearby(unit, drill, ore.x, ore.y, 0, 6);
    }

    public static boolean inventoryNearlyFull(Unit unit){
        return unit != null && unit.stack != null && unit.stack.amount >= Math.max(1, unit.type.itemCapacity - 1);
    }

    /** Deposits the carried stack into the team's core when the unit reaches it. */
    public static boolean depositAtCore(Unit unit){
        if(unit == null || unit.stack == null || unit.stack.amount <= 0) return true;
        Building core = unit.team().core();
        if(core == null || !unit.within(core, 28f)) return false;
        if(unit.stack.item != null){
            int before = unit.stack.amount;
            int accepted = core.acceptStack(unit.stack.item, before, unit);
            if(accepted > 0){
                Call.transferItemTo(unit, unit.stack.item, Math.min(accepted, before), unit.x, unit.y, core);
            }
            return unit.stack == null || unit.stack.amount <= 0;
        }
        return true;
    }

    public static boolean hasNearbyDrillForItem(Unit unit, Item item, float range){
        if(unit == null || item == null || !canMine(unit, item)) return false;
        return mindustry.entities.Units.findAllyTile(unit.team(), unit.x, unit.y, range,
            b -> b.block instanceof Drill drill && item.hardness <= drill.tier &&
                (drill.blockedItems == null || !drill.blockedItems.contains(item))) != null;
    }

    public static String drillName(Item item){
        Drill drill = drillForItem(item);
        return drill == null ? "ningún taladro disponible" : drill.localizedName;
    }

    /** Returns the first required item the team cannot currently afford. */
    public static Item missingRequirement(Unit unit, Block block){
        if(unit == null || block == null || block.requirements == null) return null;
        for(ItemStack req : block.requirements){
            int available = unit.team().items().get(req.item) - ConstructionSystem.reservedForAurora(unit, req.item);
            if(available < req.amount) return req.item;
        }
        return null;
    }
}
