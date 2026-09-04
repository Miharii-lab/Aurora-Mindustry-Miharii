package aurora.ai;

import mindustry.entities.units.AIController;
import mindustry.ai.types.BuilderAI;
import mindustry.ai.types.MinerAI;
import mindustry.gen.Unit;
import aurora.brain.AuroraAIContext;
import aurora.brain.AuroraBrain;
import aurora.core.AuroraProfile;
import aurora.core.AuroraRegistry;

/** Thin Mindustry adapter. Decision-making lives in AuroraBrain. */
public class AuroraAI extends AIController {
    private final AuroraBrain brain = new AuroraBrain();
    private final MinerAI miner = new MinerAI();
    private final BuilderAI builder = new BuilderAI();
    private final AuroraAIContext context = new AuroraAIContext(this, miner, builder);

    @Override
    public void updateMovement(){
        if(unit == null || !unit.isValid()) return;
        AuroraProfile profile = AuroraRegistry.profile(unit);
        if(AuroraRegistry.owner(unit) == null) AuroraRegistry.assignOwnerIfNeeded(unit);
        brain.think(unit, profile);
        brain.execute(context, unit, profile);
    }
}
