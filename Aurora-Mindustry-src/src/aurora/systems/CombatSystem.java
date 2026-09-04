package aurora.systems;

import mindustry.gen.Unit;
import mindustry.gen.Building;
import mindustry.entities.Units;
import mindustry.world.meta.BlockFlag;

/** Tactical local combat: target scoring, core defense, retreat and controlled engagement. */
public final class CombatSystem {
    private CombatSystem(){}

    public static Unit target(Unit self, float range){
        if(self == null) return null;
        final Unit[] best = {null};
        final float[] score = {-Float.MAX_VALUE};
        Units.nearbyEnemies(self.team(), self.x, self.y, range, enemy -> {
            if(enemy == null || !enemy.isValid()) return;
            float d = self.dst(enemy);
            float s = 0f;
            if(enemy.maxHealth > self.maxHealth * 1.35f) s += 35f;
            if(enemy.hasWeapons()) s += 18f;
            s += Math.max(0f, 18f - d * 0.12f);
            s += Math.max(0f, (1f - enemy.health / Math.max(1f, enemy.maxHealth)) * 20f);
            if(s > score[0]){ score[0] = s; best[0] = enemy; }
        });
        return best[0];
    }

    public static boolean shouldRetreat(Unit self, float range){
        if(self == null || self.health / Math.max(1f, self.maxHealth) > 0.30f) return false;
        Unit dangerous = ThreatAnalyzer.dangerousEnemy(self, range);
        return dangerous != null;
    }

    public static boolean enemyNearCore(Unit self, float range){
        Building core = self.team().core();
        if(core == null) return false;
        return Units.closestEnemy(self.team(), core.x, core.y, range, u -> u != null && u.isValid()) != null;
    }

    public static boolean engage(Unit self, float range){
        Unit enemy = target(self, range);
        if(enemy == null || !self.hasWeapons()){
            self.controlWeapons(false, false);
            return false;
        }
        self.aimLook(enemy);
        self.controlWeapons(true, true);
        return true;
    }

    public static Unit dangerousEnemy(Unit self, float range){ return ThreatAnalyzer.dangerousEnemy(self, range); }
}
