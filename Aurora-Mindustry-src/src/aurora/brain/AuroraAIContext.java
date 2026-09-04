package aurora.brain;

import mindustry.ai.types.AIController;
import mindustry.ai.types.BuilderAI;
import mindustry.ai.types.MinerAI;

/** Dependency bundle keeping AuroraBrain independent from controller internals. */
public record AuroraAIContext(AIController controller, MinerAI miner, BuilderAI builder) {}
