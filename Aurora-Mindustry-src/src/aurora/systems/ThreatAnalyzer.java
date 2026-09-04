package aurora.systems;

import mindustry.entities.Units;
import mindustry.gen.Unit;

/** Finds the most relevant local enemy without a full-map scan. */
public final class ThreatAnalyzer {
    private ThreatAnalyzer(){}

    public static Unit nearestEnemy(Unit self, float range){
        return Units.closestEnemy(self.team(), self.x, self.y, range, u -> u != null && u.isValid());
    }

    public static Unit dangerousEnemy(Unit self, float range){
        return Units.closestEnemy(self.team(), self.x, self.y, range, enemy -> enemy != null && enemy.isValid() &&
            enemy.maxHealth > self.maxHealth * 1.35f);
    }

    public static boolean needsDefense(Unit self, float range){
        return nearestEnemy(self, range) != null;
    }
}
