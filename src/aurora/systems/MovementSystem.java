package aurora.systems;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.ai.types.AIController;
import mindustry.gen.Player;
import mindustry.gen.Unit;

/** All Aurora movement decisions live here. */
public final class MovementSystem {
    public static final float FOLLOW_DISTANCE = 42f;
    public static final float RETURN_DISTANCE = 34f;
    public static final float DEFEND_RADIUS = 52f;
    private static final Vec2 target = new Vec2();

    private MovementSystem(){}

    public static void follow(AIController ai, Unit unit, Player owner){
        if(owner == null || owner.unit() == null || !owner.unit().isValid()) return;
        Unit ownerUnit = owner.unit();
        float distance = unit.dst(ownerUnit);
        if(distance > FOLLOW_DISTANCE){
            ai.moveTo(ownerUnit, FOLLOW_DISTANCE, 20f, true, null, true);
        }else{
            float angle = ownerUnit.angleTo(unit) + 90f;
            float x = ownerUnit.x + Mathf.cosDeg(angle) * 24f;
            float y = ownerUnit.y + Mathf.sinDeg(angle) * 24f;
            ai.moveTo(target.set(x, y), 1f, 18f);
        }
    }

    public static void moveTo(AIController ai, Unit unit, float x, float y, float radius){
        ai.moveTo(target.set(x, y), radius, 18f);
    }

    public static boolean atCore(Unit unit){
        var core = unit.closestCore();
        return core != null && unit.within(core, RETURN_DISTANCE);
    }

    public static void returnToCore(AIController ai, Unit unit){
        var core = unit.closestCore();
        if(core != null && !unit.within(core, RETURN_DISTANCE)){
            ai.moveTo(core, RETURN_DISTANCE, 20f, true, null, true);
        }
    }
}
