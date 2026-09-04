package aurora.command;

import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;
import aurora.core.AuroraProfile;
import aurora.core.AuroraRegistry;
import aurora.core.AuroraTask;
import aurora.planning.DefenseProposal;
import aurora.systems.CommunicationSystem;
import aurora.systems.ConstructionSystem;
import aurora.systems.MiningSystem;
import aurora.content.AuroraContent;

/** Spanish chat command surface. English aliases are intentionally not required. */
public final class AuroraCommands {
    private AuroraCommands(){}

    public static void register(arc.util.CommandHandler handler){
        handler.<Player>register("aurora", "<orden> [valores...]", "Controla a tu compañera Aurora.", (args, player) -> {
            if(args.length == 0){ help(player); return; }

            String action = normalize(args[0]);

            // Test/convenience path: invocation does not require an existing Aurora or
            // prior research. This is intentionally handled before owner lookup so the
            // first Aurora can always be created in a custom/offline test world.
            if(action.equals("invoca") || action.equals("invocar") || action.equals("llama") || action.equals("llamar")){
                invokeAurora(player);
                return;
            }

            boolean assigning = action.equals("duena") || action.equals("dueño");
            Unit unit = AuroraRegistry.closestOwnedTo(player);
            if(assigning && unit == null) unit = AuroraRegistry.closestUnownedTo(player);
            if(unit == null && player.admin) unit = AuroraRegistry.closestTo(player);
            if(unit == null){
                player.sendMessage("[scarlet]No hay una Aurora disponible en tu equipo.");
                return;
            }
            AuroraProfile profile = AuroraRegistry.profile(unit);

            if(action.equals("duena") || action.equals("dueño")){
                Player currentOwner = AuroraRegistry.owner(unit);
                if(profile.hasOwner() && currentOwner == null && !player.admin){
                    player.sendMessage("[scarlet]El dueño de Aurora no está disponible en este momento.");
                    return;
                }
                if(currentOwner != null && currentOwner != player && !player.admin){
                    player.sendMessage("[scarlet]Solo el dueño actual puede reasignar Aurora.");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("[scarlet]Uso: /aurora dueña <jugador>");
                    return;
                }
                Player target = findPlayer(args[1]);
                if(target == null || target.team() != player.team()){
                    player.sendMessage("[scarlet]No encontré a ese jugador en tu equipo.");
                    return;
                }
                AuroraRegistry.assignOwner(unit, target);
                CommunicationSystem.say(player, "Ahora soy de " + target.name + ".");
                return;
            }

            Player owner = AuroraRegistry.owner(unit);
            if(owner == null && !profile.hasOwner()){
                AuroraRegistry.assignOwner(unit, player);
                owner = player;
            }
            if(owner == null && profile.hasOwner() && !player.admin){
                player.sendMessage("[scarlet]El dueño de Aurora no está disponible en este momento.");
                return;
            }
            if(owner != null && owner != player && !player.admin){
                player.sendMessage("[scarlet]Solo el dueño de Aurora puede darle órdenes.");
                return;
            }

            switch(action){
                case "si", "sí", "confirmar", "aceptar" -> confirmProposal(unit, profile, player);
                case "no", "rechazar", "cancelar" -> rejectProposal(profile, player);
                case "seguir" -> setTask(profile, AuroraTask.FOLLOW, player, "Te sigo.");
                case "espera", "esperar" -> {
                    profile.task = AuroraTask.WAIT;
                    profile.setTarget(player.x, player.y);
                    profile.manualOrder = true;
                    CommunicationSystem.say(player, "Me quedo aquí.");
                }
                case "mina", "minar" -> {
                    profile.recoveryItemId = -1;
                    setTask(profile, AuroraTask.MINE_RESOURCE, player, "Buscaré el recurso que más necesite la base.");
                }
                case "repara", "reparar" -> setTask(profile, AuroraTask.REPAIR, player, "Buscaré estructuras dañadas.");
                case "defiende", "defender" -> {
                    profile.task = AuroraTask.DEFEND;
                    profile.setTarget(player.x, player.y);
                    profile.manualOrder = true;
                    CommunicationSystem.say(player, "Defenderé esta zona.");
                }
                case "nucleo", "núcleo", "core" -> setTask(profile, AuroraTask.RETURN_TO_CORE, player, "Regreso al núcleo.");
                case "explora", "explorar" -> {
                    profile.task = AuroraTask.EXPLORE;
                    profile.manualOrder = true;
                    Tile target = profile.exploration.nextTarget(unit);
                    if(target != null) profile.setTarget(target.worldx(), target.worldy());
                    CommunicationSystem.say(player, target == null ? "No encuentro un sector nuevo que explorar." : "Exploraré el sector y recordaré lo que encuentre.");
                }
                case "estado", "status" -> {
                    mindustry.type.Item resource = MiningSystem.preferredResource(unit);
                    String resourceText = resource == null ? "ninguno disponible" : resource.localizedName + " | " + MiningSystem.drillName(resource);
                    String controller = unit.controller() == null ? "ninguno" : unit.controller().getClass().getSimpleName();
                    CommunicationSystem.say(player, "Estado: dueño=" + (owner == null ? "no disponible" : owner.name)
                        + ", tarea=" + profile.task + ", estado=" + profile.state + ", control=" + controller
                        + ", recurso=" + resourceText + ".");
                }
                case "recuerda", "memoria" -> CommunicationSystem.say(player, CommunicationSystem.recall(profile));
                case "construye", "construir" -> handleBuild(args, unit, profile, player);
                case "propone", "proponer", "plan" -> propose(args, unit, profile, player);
                default -> help(player);
            }
        });
    }

    private static void invokeAurora(Player player){
        if(AuroraContent.aurora == null){
            player.sendMessage("[scarlet]Aurora todavía no está cargada. Revisa el JAR del mod.");
            return;
        }
        Unit existing = AuroraRegistry.closestOwnedTo(player);
        if(existing != null){
            CommunicationSystem.say(player, "Ya estoy aquí. No necesito otra copia.");
            return;
        }

        Unit unowned = AuroraRegistry.closestUnownedTo(player);
        if(unowned != null){
            AuroraRegistry.assignOwner(unowned, player);
            AuroraProfile profile = AuroraRegistry.profile(unowned);
            profile.task = AuroraTask.FOLLOW;
            profile.manualOrder = false;
            profile.state = aurora.core.AuroraState.FOLLOWING;
            CommunicationSystem.say(player, "Ya tengo un cuerpo disponible. Ahora soy tu Aurora.");
            return;
        }

        float x = player.x;
        float y = player.y;
        Unit spawned = AuroraContent.aurora.spawn(player.team(), x, y, 0f, unit -> {
            AuroraRegistry.assignOwner(unit, player);
            AuroraProfile profile = AuroraRegistry.profile(unit);
            profile.task = AuroraTask.FOLLOW;
            profile.manualOrder = false;
            profile.state = aurora.core.AuroraState.FOLLOWING;
            profile.setTarget(player.x, player.y);
        });

        if(spawned == null || !spawned.isValid()){
            player.sendMessage("[scarlet]No pude invocar a Aurora.");
            return;
        }
        CommunicationSystem.say(player, "Estoy aquí. Te pertenezco y ya puedo empezar a trabajar.");
    }

    private static void handleBuild(String[] args, Unit unit, AuroraProfile profile, Player player){
        if(args.length >= 2 && normalize(args[1]).equals("muralla")){
            propose(new String[]{"propone", "muralla"}, unit, profile, player);
            return;
        }
        if(args.length < 4){
            player.sendMessage("[scarlet]Uso: /aurora construye <bloque> <x> <y> [rotación]");
            return;
        }
        Block block = mindustry.Vars.content.block(args[1]);
        if(block == null){ player.sendMessage("[scarlet]No conozco ese bloque: " + args[1]); return; }
        int x = Strings.parseInt(args[2]);
        int y = Strings.parseInt(args[3]);
        int rotation = args.length > 4 ? Strings.parseInt(args[4]) : 0;
        if(!ConstructionSystem.queue(unit, block, x, y, rotation)){
            player.sendMessage("[scarlet]No puedo colocar ese bloque ahí.");
            return;
        }
        profile.task = AuroraTask.BUILD;
        profile.manualOrder = true;
        CommunicationSystem.say(player, "Plano añadido a mi cola de construcción.");
    }

    private static void propose(String[] args, Unit unit, AuroraProfile profile, Player player){
        if(args.length < 2 || !normalize(args[1]).equals("muralla")){
            player.sendMessage("[scarlet]Por ahora puedo proponer: /aurora propone muralla");
            return;
        }
        profile.pendingProposal = new DefenseProposal(player.x, player.y);
        CommunicationSystem.say(player, "Propongo " + profile.pendingProposal.summary() + ". ¿La construyo? Escribe [green]/aurora si[white] o [scarlet]/aurora no[white].");
    }

    private static void confirmProposal(Unit unit, AuroraProfile profile, Player player){
        if(profile.pendingProposal == null){
            player.sendMessage("[scarlet]No tengo ningún plano pendiente.");
            return;
        }
        profile.pendingProposal.queueNext(unit, 8);
        profile.task = AuroraTask.BUILD;
        profile.manualOrder = true;
        CommunicationSystem.say(player, "Aceptado. Empiezo a construir el plan.");
    }

    private static void rejectProposal(AuroraProfile profile, Player player){
        profile.pendingProposal = null;
        CommunicationSystem.say(player, "De acuerdo. No construiré ese plano.");
    }

    private static void setTask(AuroraProfile profile, AuroraTask task, Player player, String message){
        profile.task = task;
        profile.manualOrder = true;
        CommunicationSystem.say(player, message);
    }

    private static String normalize(String s){
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static Player findPlayer(String name){
        for(Player p : mindustry.gen.Groups.player){
            if(p.isAdded() && p.name.equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    private static void help(Player player){
        CommunicationSystem.say(player, "/aurora invoca | dueña <jugador> | estado | seguir | espera | mina | repara | defiende | nucleo | explora | recuerda | construye | propone muralla | si | no");
    }
}
