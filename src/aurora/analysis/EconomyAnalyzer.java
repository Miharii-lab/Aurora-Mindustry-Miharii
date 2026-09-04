package aurora.analysis;

import arc.struct.IntSet;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Category;

/** Measures actual item flow, resource availability and power balance. */
public final class EconomyAnalyzer implements BottleneckAnalyzer{
    private final Team team;
    public EconomyAnalyzer(Team team){ this.team = team; }

    @Override
    public Observation analyze(){
        if(team == null || !team.isAlive()) return new Observation("survival", "No hay núcleo operativo.", 1f);

        int buildings = 0, production = 0, distribution = 0, power = 0, turrets = 0, drills = 0;
        int storages = 0, nearFull = 0;
        float powerProduced = 0f, powerNeeded = 0f, satisfactionSum = 0f;
        int powerGraphs = 0;
        IntSet seenGraphs = new IntSet();

        for(Building b : team.data().buildings){
            if(b == null || !b.isValid()) continue;
            buildings++;
            Block block = b.block;
            if(block.category == Category.production) production++;
            if(block.category == Category.distribution) distribution++;
            if(block.category == Category.power) power++;
            if(block.flags.contains(BlockFlag.turret)) turrets++;
            if(block instanceof Drill) drills++;
            if(block.flags.contains(BlockFlag.storage)){
                storages++;
                int cap = Math.max(0, block.itemCapacity);
                int stored = b.items == null ? 0 : b.items.total();
                if(cap > 0 && stored / (float)cap >= 0.85f) nearFull++;
            }
            if(b.power != null && b.power.graph != null && b.power.graph.hasPowerBalanceSamples()){
                int graphId = b.power.graph.getID();
                if(seenGraphs.add(graphId) && powerGraphs < 64){
                    powerGraphs++;
                    powerProduced += b.power.graph.getPowerProduced();
                    powerNeeded += b.power.graph.getPowerNeeded();
                    satisfactionSum += b.power.graph.getSatisfaction();
                }
            }
        }

        ResourceSnapshot resource = findCriticalResource();
        float satisfaction = powerGraphs == 0 ? 1f : satisfactionSum / powerGraphs;

        if(satisfaction < 0.55f && powerNeeded > powerProduced * 1.05f)
            return new Observation("power", "La red eléctrica está trabajando con poca satisfacción: la demanda supera la producción. Conviene reforzar energía antes de seguir expandiendo.", 0.94f);
        if(resource != null && resource.stock < 20 && resource.flow < 0.25f)
            return new Observation("resources", "El recurso más escaso es " + resource.item.localizedName + ": stock " + resource.stock + ", entrada medida " + format(resource.flow) + "/s. La extracción puede ser el cuello de botella.", 0.92f);
        if(resource != null && resource.stock > 140 && resource.flow > 1.50f)
            return new Observation("storage", "Las reservas de " + resource.item.localizedName + " son altas y la entrada sigue siendo fuerte. Conviene transformar, consumir o almacenar mejor antes de producir más.", 0.88f);
        if(production > 0 && distribution == 0 && resource != null && resource.flow > 0.2f && resource.stock < 100)
            return new Observation("logistics", "Hay producción real, pero casi no hay infraestructura de distribución. El flujo de " + resource.item.localizedName + " puede estar frenándose entre máquinas.", 0.86f);
        if(storages > 0 && nearFull > 0)
            return new Observation("storage", "Parte del almacenamiento está cerca de su capacidad real (" + nearFull + "/" + storages + "). Conviene consumir, mover o ampliar reservas.", 0.90f);
        if(turrets == 0 && buildings > 12)
            return new Observation("defense", "La base creció sin una defensa proporcional. Recomiendo asegurar primero los accesos vulnerables.", 0.84f);
        if(resource != null && resource.stock < 20 && drills == 0 && resource.flow < 0.50f)
            return new Observation("resources", "El stock de " + resource.item.localizedName + " está bajo, no detecto taladros y la entrada medida es pequeña.", 0.96f);

        String resourceText = resource == null
            ? "sin mineral disponible"
            : resource.item.localizedName + " " + format(resource.flow) + "/s";
        return new Observation("ok", "Flujos estables: " + resourceText + "; energía " + format(satisfaction * 100f) + "% de satisfacción.", 0.72f);
    }

    /** Finds the lowest-stock ore currently present on this map, then measures its flow. */
    private ResourceSnapshot findCriticalResource(){
        Item best = null;
        int bestStock = Integer.MAX_VALUE;
        for(Item item : Item.getAllOres()){
            if(item == null || best == item) continue;
            if(!mindustry.Vars.indexer.hasOre(item) && !mindustry.Vars.indexer.hasWallOre(item)) continue;
            int stock = team.items().get(item);
            if(best == null || stock < bestStock){
                best = item;
                bestStock = stock;
            }
        }
        if(best == null) return null;

        float flow = 0f;
        for(Building b : team.data().buildings){
            if(b != null && b.isValid() && b.items != null){
                float sample = b.items.getFlowRate(best);
                if(sample >= 0f) flow += sample;
            }
        }
        return new ResourceSnapshot(best, bestStock, flow);
    }

    private static String format(float value){
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private record ResourceSnapshot(Item item, int stock, float flow){}
}
