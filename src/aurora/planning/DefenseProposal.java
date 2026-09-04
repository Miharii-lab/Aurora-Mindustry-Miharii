package aurora.planning;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.world.Block;
import aurora.systems.ConstructionSystem;
import mindustry.gen.Unit;
import aurora.core.AuroraProfile;

/** Dynamic local defense proposal. Failed entries stay pending and are retried after other entries. */
public final class DefenseProposal {
    private final Seq<PlanEntry> entries = new Seq<>();
    private int cursor;
    private int blockedPasses;

    public DefenseProposal(float cx, float cy){ buildAround(Math.round(cx / 8f), Math.round(cy / 8f)); }
    public DefenseProposal(float cx, float cy, float enemyX, float enemyY){
        int cxTile = Math.round(cx / 8f), cyTile = Math.round(cy / 8f);
        int ex = Math.round(enemyX / 8f), ey = Math.round(enemyY / 8f);
        int sx = Integer.compare(ex, cxTile), sy = Integer.compare(ey, cyTile);
        int bx = cxTile + sx * 5, by = cyTile + sy * 5;
        for(int i = -3; i <= 3; i++) if(Math.abs(sx) >= Math.abs(sy)) entries.add(new PlanEntry(Blocks.copperWall, bx, by + i, 0)); else entries.add(new PlanEntry(Blocks.copperWall, bx + i, by, 0));
        entries.add(new PlanEntry(Blocks.duo, bx - (sy != 0 ? 2 : 0), by - (sx != 0 ? 2 : 0), 0));
        entries.add(new PlanEntry(Blocks.duo, bx + (sy != 0 ? 2 : 0), by + (sx != 0 ? 2 : 0), 0));
    }
    private void buildAround(int tx, int ty){
        for(int dx = -3; dx <= 3; dx++){
            entries.add(new PlanEntry(Blocks.copperWall, tx + dx, ty - 2, 0));
            entries.add(new PlanEntry(Blocks.copperWall, tx + dx, ty + 2, 0));
        }
        entries.add(new PlanEntry(Blocks.duo, tx - 2, ty, 0));
        entries.add(new PlanEntry(Blocks.duo, tx + 2, ty, 0));
    }

    public boolean isDone(){ return cursor >= entries.size; }
    public int cursor(){ return cursor; }
    public int blockedPasses(){ return blockedPasses; }

    public void write(java.io.DataOutput out) throws java.io.IOException{
        out.writeInt(entries.size); out.writeInt(cursor);
        for(PlanEntry p : entries){ out.writeInt(p.block.id); out.writeInt(p.x); out.writeInt(p.y); out.writeInt(p.rotation); }
    }

    public static DefenseProposal read(java.io.DataInput in) throws java.io.IOException{
        int size = in.readInt();
        int cursor = in.readInt();
        if(size < 0 || size > 4096 || cursor < 0 || cursor > size) throw new java.io.IOException("Invalid Aurora proposal");
        DefenseProposal result = new DefenseProposal(0f, 0f);
        result.entries.clear();
        for(int i = 0; i < size; i++){
            int blockId = in.readInt(), x = in.readInt(), y = in.readInt(), rotation = in.readInt();
            Block block = mindustry.Vars.content.block(blockId);
            if(block != null) result.entries.add(new PlanEntry(block, x, y, rotation));
        }
        result.cursor = Math.min(cursor, result.entries.size);
        return result;
    }

    /**
     * Attempts the current entry, then local positions and material fallbacks.
     * A temporarily blocked entry is rotated to the end instead of being lost.
     */
    public int queueNext(Unit unit, int budget){ return queueNext(unit, budget, null); }

    public int queueNext(Unit unit, int budget, AuroraProfile profile){
        if(unit == null || budget <= 0 || isDone()) return 0;
        int added = 0;
        int inspected = 0;
        int passSize = entries.size - cursor;
        while(cursor < entries.size && added < budget && inspected < Math.max(1, passSize)){
            PlanEntry p = entries.get(cursor);
            if(tryEntry(unit, p, profile)){
                cursor++; added++; blockedPasses = 0;
            }else{
                // Do not delete the mission step. Move it behind the remaining steps so Aurora can
                // make progress on whatever is currently possible, then retry this one later.
                entries.remove(cursor);
                entries.add(p);
                blockedPasses++;
                passSize--;
                if(passSize <= 0) break;
            }
            inspected++;
        }
        return added;
    }

    private boolean tryEntry(Unit unit, PlanEntry p, AuroraProfile profile){
        if(p.block == Blocks.copperWall){
            // leadWall was removed/renamed in v159.7; try copper -> titanium fallback instead
            Block selected = ConstructionSystem.queueWithAlternatives(unit, new Block[]{Blocks.copperWall, Blocks.titaniumWall}, p.x, p.y, p.rotation, 6, profile);
            return selected != null;
        }
        if(p.block == Blocks.duo){
            Block selected = ConstructionSystem.queueWithAlternatives(unit, new Block[]{Blocks.duo}, p.x, p.y, p.rotation, 6, profile);
            return selected != null;
        }
        return ConstructionSystem.queueNearby(unit, p.block, p.x, p.y, p.rotation, 6);
    }

    public String summary(){ return "defensa local (" + entries.size + " planos)"; }
    public Block nextRequiredBlock(){ return isDone() ? null : entries.get(cursor).block; }
    private record PlanEntry(Block block, int x, int y, int rotation){}
}
