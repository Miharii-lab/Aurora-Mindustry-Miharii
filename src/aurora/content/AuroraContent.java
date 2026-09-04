package aurora.content;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.content.TechTree;
import mindustry.content.Planets;
import mindustry.content.SectorPresets;
import mindustry.game.Objectives.OnSector;
import mindustry.content.TechTree.TechNode;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import aurora.systems.MiningSystem;
import mindustry.content.Bullets;
import mindustry.gen.UnitEntity;
import arc.graphics.Color;
import aurora.ai.AuroraAI;

/** Defines Aurora-specific content. */
public final class AuroraContent {
    private AuroraContent(){}
    public static UnitType aurora;

    public static void load(){
        aurora = new UnitType("aurora") {{
            // Physical identity: a small autonomous flying companion.
            constructor = UnitEntity::create;
            aiController = AuroraAI::new;
            // Keep Aurora's brain as the default controller. Mindustry may still replace it
            // with Player during possession; resetController() will restore this controller.
            controller = u -> new AuroraAI();
            // Campaign-ready: Aurora is researched through the Air Factory branch.
            // In custom/sandbox games, unlockedNow() is true, so the factory remains immediately testable.
            alwaysUnlocked = false;
            playerControllable = true;
            logicControllable = true;
            hovering = true;
            omniMovement = true;
            flying = true;
            speed = 1.35f;
            rotateSpeed = 6f;
            drag = 0.08f;
            accel = 0.10f;
            hitSize = 8f;
            health = 220f;
            description = "Una compañera aérea autónoma que aprende, protege y trabaja junto a su dueño.";
            details = "Aurora combina una mente adaptativa con un cuerpo ligero de asistencia. Su núcleo rosa pastel funciona como identificador visual y su comportamiento está gobernado por AuroraBrain.";
            armor = 1f;
            range = 140f;
            targetAir = true;
            targetGround = true;
            buildSpeed = 0.8f;
            // Universal mining tool: Aurora can mine every current ore up to the
            // highest vanilla drill hardness. MinerAI still validates the actual ore.
            // Capability is intentionally broader than the current vanilla drill tiers.
            // Actual base extraction still requires a suitable unlocked Drill.
            mineTier = 99;
            mineSpeed = 1.5f;
            mineHardnessScaling = true;
            mineWalls = true;
            mineFloor = true;
            itemCapacity = 20;
            // Visual personality: pastel-pink core, cool engines and a soft trail.
            outlineColor = Color.valueOf("302B38");
            outlineRadius = 2;
            engineColor = Color.valueOf("F6C1D8");
            engineColorInner = Color.valueOf("D8FFFF");
            trailColor = Color.valueOf("F6C1D8");
            trailLength = 12;
            engineSize = 1.2f;
            lightColor = Color.valueOf("F6C1D8");
            lightRadius = 28f;
            lightOpacity = 0.35f;
            abilities.add(new RepairFieldAbility(8f, 180f, 55f));
            weapons.add(new Weapon("aurora-cannon") {{
                reload = 28f;
                x = 4f;
                y = 0f;
                shootY = 2f;
                recoil = 1f;
                shake = 0.2f;
            // v159.7 no longer exposes standardCopper; use placeholder as a safe default
            bullet = Bullets.placeholder;
            }});
        }};

        // Populate mining targets from the actual ore registry instead of hard-coding
        // copper/lead. This also picks up compatible modded ores automatically.
        aurora.mineItems.clear();
        for(Item item : MiningSystem.allMineableOres()) aurora.mineItems.add(item);

        // The canonical factory recipe lives in content/aurora.hjson. Keeping it in data
        // makes the physical unit visible even if the Java entry point is not executed.

        // Give Aurora explicit planet-specific research branches. The vanilla tech trees
        // are built before mod content is loaded, so their nodes are already present here.
        // Serpulo: Air Factory -> Aurora. Erekir has no Air Factory, so Aurora is attached
        // to Ship Fabricator as a research/access node; actual creation is intentionally
        // provided by /aurora invoca because Aurora is an air companion, not an Erekir
        // tank/ship/mech factory unit.
        ItemStack[] serpuloCost = ItemStack.with(Items.copper, 150, Items.lead, 100);
        ItemStack[] erekirCost = ItemStack.with(Items.beryllium, 200, Items.silicon, 150);
        TechTree.all.each(parent -> {
            if(parent.content == Blocks.airFactory && parent.planet == Planets.serpulo && !parent.children.contains(n -> n.content == aurora)){
                new TechNode(parent, aurora, serpuloCost);
            }
            if(parent.content == Blocks.shipFabricator && parent.planet == Planets.erekir && !parent.children.contains(n -> n.content == aurora)){
                TechNode node = new TechNode(parent, aurora, erekirCost);
                node.objectives.add(new OnSector(SectorPresets.lake));
            }
        });
    }
}
