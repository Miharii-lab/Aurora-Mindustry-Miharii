package aurora.systems;

import arc.util.Time;
import mindustry.gen.Player;
import aurora.core.AuroraMemory;
import aurora.core.AuroraProfile;
import aurora.personality.AuroraMood;

/** Event -> interpretation -> personality communication layer. Aurora uses a pastel-pink signature. */
public final class CommunicationSystem {
    private static final float COOLDOWN = 3f;
    public static final String AURORA_PINK = "[#F6C1D8]";
    private CommunicationSystem(){}

    public static void event(Player player, AuroraProfile profile, String key, String category, String detail, AuroraMood mood){
        if(player == null || !player.isAdded() || profile == null) return;
        if(key == null) key = category + ":" + detail;
        if(!key.equals(profile.lastEventKey)){
            profile.memory.remember(category, detail, player.x, player.y);
            profile.lastEventKey = key;
        }
        profile.mood = mood == null ? AuroraMood.CALM : mood;
        if(Time.time - profile.lastCommunicationTime < COOLDOWN) return;
        String prefix = switch(profile.mood){
            case WORRIED -> "Necesito que prestemos atención: ";
            case FOCUSED -> "Estoy concentrada en esto: ";
            case HAPPY -> "Buenas noticias: ";
            default -> "He observado algo: ";
        };
        player.sendMessage(AURORA_PINK + "Aurora:[white] " + prefix + detail);
        profile.lastCommunicationTime = Time.time;
    }

    public static void say(Player player, String message){
        if(player == null || !player.isAdded()) return;
        player.sendMessage(AURORA_PINK + "Aurora:[white] " + message);
    }

    public static String recall(AuroraProfile profile){
        AuroraMemory.Episode e = profile == null ? null : profile.memory.latest();
        return e == null ? "Todavía no tengo episodios recientes registrados." : "Recuerdo: " + e.text();
    }
}
