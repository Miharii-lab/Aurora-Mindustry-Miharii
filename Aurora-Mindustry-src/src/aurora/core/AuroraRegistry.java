package aurora.core;

import arc.struct.IntMap;
import arc.struct.IntSeq;
import mindustry.gen.Player;
import mindustry.gen.Unit;

import static mindustry.gen.Groups.unit;

/** Runtime owner/task registry. The unit ID is the stable key while the unit exists. */
public final class AuroraRegistry {
    private static final IntMap<AuroraProfile> profiles = new IntMap<>();
    private AuroraRegistry(){}

    public static AuroraProfile profile(Unit unit){
        int id = unit.id();
        AuroraProfile result = profiles.get(id);
        if(result == null){
            result = new AuroraProfile();
            profiles.put(id, result);
        }
        return result;
    }

    public static boolean isAurora(Unit unit){
        return unit != null && unit.type != null && "aurora".equals(unit.type.name);
    }

    public static void assignOwner(Unit unit, Player player){
        if(unit == null || player == null || !isAurora(unit)) return;
        profile(unit).ownerId = player.id();
    }

    /** Assigns an owner only when Aurora does not already have one. */
    public static Player assignOwnerIfNeeded(Unit unit){
        if(mindustry.Vars.net.client()) return null;
        if(unit == null || !isAurora(unit)) return null;
        AuroraProfile profile = profile(unit);
        // Keep an explicit owner ID even if that player is temporarily unavailable.
        // Ownership must not silently jump to another player.
        if(profile.hasOwner()) return owner(unit);

        Player best = null;
        float bestDst = Float.MAX_VALUE;
        int alliedPlayers = 0;
        Player onlyAllied = null;
        for(Player p : mindustry.gen.Groups.player){
            if(!p.isValid() || p.team() != unit.team()) continue;
            alliedPlayers++;
            onlyAllied = p;
            float dx = p.x - unit.x, dy = p.y - unit.y;
            float dst = dx * dx + dy * dy;
            if(dst < bestDst){
                bestDst = dst;
                best = p;
            }
        }
        // In the common single-player case there is no ambiguity: the sole allied
        // player is always the owner, regardless of where the factory spawned Aurora.
        if(alliedPlayers == 1) best = onlyAllied;
        if(best != null) assignOwner(unit, best);
        return best;
    }

    /** Repairs ownerless Auroras after save/world loading without overwriting valid ownership. */
    public static void assignUnownedToNearestPlayers(){
        if(mindustry.Vars.net.client()) return;
        for(Unit u : unit){
            if(isAurora(u) && u.isValid() && !profile(u).hasOwner()) assignOwnerIfNeeded(u);
        }
    }

    public static Player owner(Unit unit){
        if(unit == null) return null;
        int id = profile(unit).ownerId;
        if(id < 0) return null;
        for(Player p : mindustry.gen.Groups.player){
            if(p.isValid() && p.id() == id) return p;
        }
        return null;
    }

    public static Unit closestTo(Player player){
        if(player == null) return null;
        Unit best = null;
        float bestDst = Float.MAX_VALUE;
        for(Unit u : unit){
            if(!isAurora(u) || u.team() != player.team() || !u.isValid()) continue;
            float dx = u.x - player.x;
            float dy = u.y - player.y;
            float d = dx * dx + dy * dy;
            if(d < bestDst){ bestDst = d; best = u; }
        }
        return best;
    }

    /** Returns the closest ownerless Aurora on the player's team. */
    public static Unit closestUnownedTo(Player player){
        if(player == null) return null;
        Unit best = null;
        float bestDst = Float.MAX_VALUE;
        for(Unit u : unit){
            if(!isAurora(u) || !u.isValid() || profile(u).hasOwner() || u.team() != player.team()) continue;
            float dx = u.x - player.x, dy = u.y - player.y;
            float d = dx * dx + dy * dy;
            if(d < bestDst){ bestDst = d; best = u; }
        }
        return best;
    }

    /** Returns the closest Aurora owned by the player, avoiding cross-owner command mixups. */
    public static Unit closestOwnedTo(Player player){
        if(player == null) return null;
        Unit best = null;
        float bestDst = Float.MAX_VALUE;
        for(Unit u : unit){
            if(!isAurora(u) || !u.isValid() || !profile(u).isOwner(player)) continue;
            float dx = u.x - player.x, dy = u.y - player.y;
            float d = dx * dx + dy * dy;
            if(d < bestDst){ bestDst = d; best = u; }
        }
        return best;
    }

    public static void cleanup(){
        IntSeq dead = new IntSeq();
        profiles.each((id, ignored) -> {
            Unit u = unit.getByID(id);
            if(u == null || !u.isValid() || !isAurora(u)) dead.add(id);
        });
        for(int i = 0; i < dead.size; i++) profiles.remove(dead.get(i));
    }

    public static void clear(){ profiles.clear(); }
}
