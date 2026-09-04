package aurora.core;

import arc.struct.Seq;

/** Small episodic memory: recent events that can influence future decisions and speech. */
public final class AuroraMemory {
    public static final int MAX_EVENTS = 24;
    private final Seq<Episode> events = new Seq<>();

    public void remember(String category, String text, float x, float y){
        if(text == null || text.isEmpty()) return;
        if(events.size >= MAX_EVENTS) events.remove(0);
        events.add(new Episode(category == null ? "general" : category, text, x, y));
    }

    public int size(){ return events.size; }
    public Episode get(int index){ return events.get(index); }
    public Episode latest(){ return events.isEmpty() ? null : events.peek(); }
    public void clear(){ events.clear(); }

    public record Episode(String category, String text, float x, float y){}
}
