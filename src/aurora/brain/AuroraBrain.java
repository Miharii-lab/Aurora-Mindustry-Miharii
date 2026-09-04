package aurora.brain;

import arc.util.Time;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import aurora.analysis.BottleneckAnalyzer;
import aurora.analysis.EconomyAnalyzer;
import aurora.core.AuroraProfile;
import aurora.core.AuroraLearning;
import aurora.core.AuroraRegistry;
import aurora.core.AuroraState;
import aurora.core.AuroraTask;
import aurora.personality.AuroraMood;
import aurora.planning.DefenseProposal;
import aurora.systems.CombatSystem;
import aurora.systems.CommunicationSystem;
import aurora.systems.ConstructionSystem;
import aurora.systems.MiningSystem;
import aurora.systems.MovementSystem;
import aurora.systems.RepairSystem;
import aurora.systems.ThreatAnalyzer;

/** Decision layer: survival -> assigned work -> economy -> exploration. */
public final class AuroraBrain {
    private static final float THINK_INTERVAL = 0.80f;
    private static final float ECONOMY_INTERVAL = 5f;
    private static final float MESSAGE_INTERVAL = 12f;
    private static final float EXPLORE_INTERVAL = 22f;
    private float timer, economyTimer, messageTimer, exploreTimer;

    public boolean think(Unit unit, AuroraProfile profile){
        timer -= Time.delta; economyTimer -= Time.delta; messageTimer -= Time.delta; exploreTimer -= Time.delta;
        if(timer > 0f) return false;
        timer = THINK_INTERVAL;
        Player owner = AuroraRegistry.owner(unit);
        if(owner == null) return true;

        profile.exploration.record(unit);

        Unit threat = ThreatAnalyzer.nearestEnemy(unit, 150f);
        Unit dangerous = ThreatAnalyzer.dangerousEnemy(unit, 150f);
        if(threat != null && profile.task != AuroraTask.DEFEND){
            profile.taskBeforeDefense = profile.task;
            profile.manualOrderBeforeDefense = profile.manualOrder;
            profile.autonomousDefense = true;
            profile.task = AuroraTask.DEFEND;
            profile.setTarget(threat.x, threat.y);
            profile.memory.remember("combat", dangerous != null ? "Amenaza fuerte detectada." : "Enemigo detectado cerca.", threat.x, threat.y);
            CommunicationSystem.event(owner, profile, "threat:" + threat.id(), "combat",
                dangerous != null ? "Detecté una amenaza fuerte. Interrumpo mi tarea y cubro la base." : "Detecté enemigos cerca. Interrumpo mi tarea para cubrir este sector.", AuroraMood.WORRIED);
        }else if(threat == null && profile.autonomousDefense && profile.task == AuroraTask.DEFEND){
            profile.task = profile.taskBeforeDefense;
            profile.manualOrder = profile.manualOrderBeforeDefense;
            profile.autonomousDefense = false;
            CommunicationSystem.event(owner, profile, "combat:clear", "combat", "El sector volvió a estar tranquilo. Retomo lo que estaba haciendo.", AuroraMood.CALM);
        }

        if(profile.task == AuroraTask.MINE_RESOURCE && MiningSystem.inventoryNearlyFull(unit)) profile.returningForInventory = true;
        if(profile.returningForInventory){
            profile.state = AuroraState.RETURNING;
            if(MovementSystem.atCore(unit) && MiningSystem.depositAtCore(unit)){
                profile.returningForInventory = false;
                profile.memory.remember("logistics", "Regresé al núcleo y descargué recursos.", unit.x, unit.y);
                if(profile.recoveryTask != AuroraTask.FOLLOW && profile.recoveryItemId >= 0){
                    mindustry.type.Item recovered = mindustry.Vars.content.item(profile.recoveryItemId);
                    if(recovered != null) profile.learning.record("recovery:" + recovered.name, AuroraLearning.Outcome.SUCCESS);
                }
                if(profile.recoveryTask != AuroraTask.FOLLOW){
                    profile.task = profile.recoveryTask;
                    profile.recoveryTask = AuroraTask.FOLLOW;
                    profile.recoveryItemId = -1;
                    profile.manualOrder = false;
                }else if(profile.task == AuroraTask.MINE_RESOURCE) profile.resumeAutonomy();
            }else return true;
        }

        if(profile.task == AuroraTask.REPAIR){
            if(RepairSystem.findTarget(unit, 180f) == null){
                AuroraTask resume = profile.taskBeforeRepair;
                boolean manual = profile.manualOrderBeforeRepair;
                profile.task = resume;
                profile.manualOrder = manual;
                profile.taskBeforeRepair = AuroraTask.FOLLOW;
                profile.manualOrderBeforeRepair = false;
            }
        }
        if(profile.task == AuroraTask.MINE_RESOURCE){
            mindustry.type.Item targetItem = profile.recoveryItemId >= 0 ? mindustry.Vars.content.item(profile.recoveryItemId) : MiningSystem.preferredResource(unit);
            if(targetItem == null || MiningSystem.ore(unit, targetItem) == null){
                if(profile.recoveryTask != AuroraTask.FOLLOW){
                    profile.learning.record("recovery:" + (targetItem == null ? "unknown" : targetItem.name), false);
                }
                if(profile.recoveryTask != AuroraTask.FOLLOW){
                    profile.memory.remember("recovery", "No encontré una fuente cercana del recurso que necesitaba.", unit.x, unit.y);
                    profile.task = profile.recoveryTask;
                    profile.recoveryTask = AuroraTask.FOLLOW;
                    profile.recoveryItemId = -1;
                }else { profile.task = AuroraTask.FOLLOW; profile.resumeAutonomy(); }
            }else if(!MiningSystem.hasNearbyDrillForItem(unit, targetItem, 32f) && !ConstructionSystem.hasWork(unit)){
                if(MiningSystem.queueResourceDrill(unit, targetItem)){
                    profile.memory.remember("recovery", "Preparé una extracción para el recurso que necesitaba.", unit.x, unit.y);
                }
            }
        }
        if(profile.task == AuroraTask.BUILD){
            if(profile.pendingProposal != null){
                int queued = profile.pendingProposal.queueNext(unit, 8, profile);
                if(profile.pendingProposal.isDone()){
                    profile.pendingProposal = null;
                    CommunicationSystem.event(owner, profile, "build:complete", "planning", "La propuesta de construcción quedó resuelta. Continúo con la siguiente tarea.", AuroraMood.HAPPY);
                }else if(queued == 0 && !ConstructionSystem.hasWork(unit)){
                    // The mission is still alive. If materials are the blocker, temporarily gather
                    // the missing resource and return to the same proposal instead of abandoning it.
                    if(profile.pendingProposal.nextRequiredBlock() != null){
                        mindustry.type.Item needed = MiningSystem.missingRequirement(unit, profile.pendingProposal.nextRequiredBlock());
                        if(needed != null && MiningSystem.ore(unit, needed) != null){
                            profile.recoveryTask = AuroraTask.BUILD;
                            profile.recoveryItemId = needed.id;
                            profile.task = AuroraTask.MINE_RESOURCE;
                            profile.manualOrder = false;
                            CommunicationSystem.event(owner, profile, "build:resource-recovery:" + needed.id, "planning", "Me falta " + needed.localizedName + ". Voy a conseguirlo y volveré al mismo plano.", AuroraMood.FOCUSED);
                        }else if(profile.pendingProposal.blockedPasses() >= 3){
                            CommunicationSystem.event(owner, profile, "build:blocked", "planning", "El plano sigue bloqueado en las posiciones probadas. Mantengo la misión y buscaré otra oportunidad cuando cambie la situación.", AuroraMood.WORRIED);
                        }
                    }
                }
            }
            if(!ConstructionSystem.hasWork(unit) && profile.pendingProposal == null){ profile.task = AuroraTask.FOLLOW; profile.clearBuild(); profile.resumeAutonomy(); }
        }

        if(profile.task != AuroraTask.DEFEND){
            Building damaged = RepairSystem.findTarget(unit, 180f);
            if(damaged != null && damaged.health() / Math.max(1f, damaged.maxHealth()) < 0.55f && profile.task != AuroraTask.REPAIR){
                profile.taskBeforeRepair = profile.task;
                profile.manualOrderBeforeRepair = profile.manualOrder;
                profile.task = AuroraTask.REPAIR;
                profile.memory.remember("repair", "Encontré una estructura importante muy dañada.", damaged.x, damaged.y);
                CommunicationSystem.event(owner, profile, "repair:" + damaged.id, "repair", "Encontré una estructura importante muy dañada. Voy a repararla antes de seguir.", AuroraMood.FOCUSED);
            }
        }

        if(economyTimer <= 0f){
            economyTimer = ECONOMY_INTERVAL;
            BottleneckAnalyzer.Observation obs = new EconomyAnalyzer(unit.team()).analyze();
            profile.lastObservationCategory = obs.category();
            if(!obs.category().equals("ok")) profile.memory.remember("economy", obs.message(), owner.x, owner.y);
            if(messageTimer <= 0f && !obs.category().equals("ok")){
                AuroraMood mood = obs.category().equals("survival") || obs.category().equals("power") ? AuroraMood.WORRIED : AuroraMood.FOCUSED;
                CommunicationSystem.event(owner, profile, "economy:" + obs.category(), "economy", obs.message(), mood);
                messageTimer = MESSAGE_INTERVAL;
            }
            if(!profile.manualOrder){
                if(obs.category().equals("resources") && profile.task == AuroraTask.FOLLOW){
                    profile.task = AuroraTask.MINE_RESOURCE;
                    CommunicationSystem.event(owner, profile, "task:mine", "decision", "El flujo de recursos está corto. Voy a reforzar la extracción del recurso que más necesite la base.", AuroraMood.FOCUSED);
                }else if(obs.category().equals("defense") && profile.pendingProposal == null){
                    Unit enemy = ThreatAnalyzer.nearestEnemy(unit, 280f);
                    profile.pendingProposal = enemy == null ? new DefenseProposal(owner.x, owner.y) : new DefenseProposal(owner.x, owner.y, enemy.x, enemy.y);
                    profile.memory.remember("planning", "Preparé una propuesta defensiva local.", owner.x, owner.y);
                    CommunicationSystem.event(owner, profile, "plan:defense", "planning", "Detecté una debilidad defensiva y preparé una propuesta local. Puedes aprobarla con /aurora si.", AuroraMood.FOCUSED);
                }else if(obs.category().equals("logistics")){
                    CommunicationSystem.event(owner, profile, "economy:logistics", "economy", "La logística puede estar frenando el flujo. Conviene mejorar el transporte antes de seguir expandiendo fábricas.", AuroraMood.FOCUSED);
                }
            }
        }

        if(!profile.manualOrder && profile.task == AuroraTask.FOLLOW && threat == null && exploreTimer <= 0f){
            Tile target = profile.exploration.nextTarget(unit);
            if(target != null){
                profile.setTarget(target.worldx(), target.worldy());
                profile.task = AuroraTask.EXPLORE;
                profile.memory.remember("exploration", "Decidí inspeccionar un sector todavía no visitado.", target.worldx(), target.worldy());
                CommunicationSystem.event(owner, profile, "explore:" + target.x + ":" + target.y, "exploration", "Voy a inspeccionar un sector que todavía no conozco.", AuroraMood.CALM);
                exploreTimer = EXPLORE_INTERVAL;
            }
        }

        if(profile.task == AuroraTask.EXPLORE && unit.within(profile.targetX, profile.targetY, 20f)){
            profile.exploration.record(unit);
            Tile next = profile.exploration.nextTarget(unit);
            if(next != null){ profile.learning.record("explore:sector", true); profile.setTarget(next.worldx(), next.worldy()); }
            else { profile.learning.record("explore:complete", true); profile.task = AuroraTask.FOLLOW; profile.resumeAutonomy(); }
        }

        switch(profile.task){
            case FOLLOW -> profile.state = AuroraState.FOLLOWING;
            case WAIT -> profile.state = AuroraState.WAITING;
            case MINE_RESOURCE -> profile.state = AuroraState.MINING;
            case BUILD -> profile.state = AuroraState.BUILDING;
            case REPAIR -> profile.state = AuroraState.REPAIRING;
            case DEFEND -> profile.state = AuroraState.DEFENDING;
            case RETURN_TO_CORE -> profile.state = AuroraState.RETURNING;
            case EXPLORE -> profile.state = AuroraState.EXPLORING;
        }
        return true;
    }

    public void execute(AuroraAIContext ctx, Unit unit, AuroraProfile profile){
        Player owner = AuroraRegistry.owner(unit);
        switch(profile.state){
            case FOLLOWING -> MovementSystem.follow(ctx.controller(), unit, owner);
            case WAITING -> MovementSystem.moveTo(ctx.controller(), unit, profile.targetX, profile.targetY, 5f);
            case MINING -> MiningSystem.update(unit, ctx.miner(), profile.recoveryItemId >= 0 ? mindustry.Vars.content.item(profile.recoveryItemId) : MiningSystem.preferredResource(unit));
            case BUILDING -> ConstructionSystem.update(unit, ctx.builder());
            case REPAIRING -> RepairSystem.moveToDamaged(ctx.controller(), unit, 180f);
            case DEFENDING -> {
                if(CombatSystem.shouldRetreat(unit, 190f)){
                    ctx.controller().stopShooting();
                    MovementSystem.returnToCore(ctx.controller(), unit);
                    if(owner != null) CommunicationSystem.event(owner, profile, "combat:retreat", "combat", "Estoy muy dañada para mantener el frente. Retrocedo al núcleo.", AuroraMood.WORRIED);
                }else if(CombatSystem.enemyNearCore(unit, 190f)){
                    CombatSystem.engage(unit, 190f);
                }else{
                    Unit enemy = CombatSystem.target(unit, 190f);
                    if(enemy != null && !CombatSystem.engage(unit, 190f)) MovementSystem.moveTo(ctx.controller(), unit, enemy.x, enemy.y, 20f);
                    else if(enemy == null){ ctx.controller().stopShooting(); MovementSystem.moveTo(ctx.controller(), unit, profile.targetX, profile.targetY, MovementSystem.DEFEND_RADIUS); }
                }
            }
            case RETURNING -> MovementSystem.returnToCore(ctx.controller(), unit);
            case EXPLORING -> MovementSystem.moveTo(ctx.controller(), unit, profile.targetX, profile.targetY, 12f);
            default -> { if(owner != null) MovementSystem.follow(ctx.controller(), unit, owner); }
        }
    }
}
