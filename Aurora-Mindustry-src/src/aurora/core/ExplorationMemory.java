package aurora.core;

import arc.struct.IntSet;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.gen.Unit;

/** Remembers coarse map sectors Aurora has already inspected. */
public final class ExplorationMemory {
    private final IntSet visited = new IntSet();
    private static final int SECTOR = 12;

    private int key(int sx, int sy){ return (sx << 16) ^ (sy & 0xffff); }

    public void record(Unit unit){
        if(unit == null || Vars.world == null) return;
        int sx = Math.round(unit.x / 8f) / SECTOR;
        int sy = Math.round(unit.y / 8f) / SECTOR;
        visited.add(key(sx, sy));
    }

    public boolean visited(int sx, int sy){ return visited.contains(key(sx, sy)); }
    public int size(){ return visited.size; }
    public IntSet raw(){ return visited; }
    public void restore(int[] keys){ if(keys == null) return; for(int key : keys) visited.add(key); }

    /** Finds the nearest unvisited valid sector center. */
    public Tile nextTarget(Unit unit){
        if(unit == null || Vars.world == null) return null;
        int width = (Vars.world.width() + SECTOR - 1) / SECTOR;
        int height = (Vars.world.height() + SECTOR - 1) / SECTOR;
        if(width <= 0 || height <= 0) return null;
        int baseX = Math.round(unit.x / 8f) / SECTOR;
        int baseY = Math.round(unit.y / 8f) / SECTOR;
        Tile best = null;
        float bestDst = Float.MAX_VALUE;
        for(int sx = 0; sx < width; sx++) for(int sy = 0; sy < height; sy++){
            if(visited(sx, sy)) continue;
            int tx = Math.min(Vars.world.width() - 1, sx * SECTOR + SECTOR / 2);
            int ty = Math.min(Vars.world.height() - 1, sy * SECTOR + SECTOR / 2);
            Tile tile = Vars.world.tile(tx, ty);
            if(tile == null || tile.floor().isDeep()) continue;
            float dx = sx - baseX, dy = sy - baseY;
            float dst = dx * dx + dy * dy;
            if(dst < bestDst){ bestDst = dst; best = tile; }
        }
        return best;
    }
}
