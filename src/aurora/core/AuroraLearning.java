package aurora.core;

import arc.struct.ObjectMap;
import arc.struct.Seq;

/** Bounded reinforcement memory. Aurora learns from outcomes, while separating real failures from temporary blockers. */
public final class AuroraLearning {
    public static final int MAX_LESSONS = 48;

    public enum Outcome { SUCCESS, STRATEGY_FAILURE }

    private final ObjectMap<String, Lesson> lessons = new ObjectMap<>();

    public void record(String key, boolean success){
        record(key, success ? Outcome.SUCCESS : Outcome.STRATEGY_FAILURE);
    }

    public void record(String key, Outcome outcome){
        if(key == null || key.isEmpty() || outcome == null) return;
        Lesson l = lessons.get(key);
        if(l == null){
            if(lessons.size >= MAX_LESSONS){
                String oldest = lessons.keys().next();
                lessons.remove(oldest);
            }
            l = new Lesson();
            lessons.put(key, l);
        }
        l.attempts++;
        if(outcome == Outcome.SUCCESS) l.successes++;
        else l.failures++;
    }

    public float confidence(String key){
        Lesson l = lessons.get(key);
        if(l == null || l.attempts == 0) return 0.5f;
        // Small-sample smoothing prevents Aurora from declaring a strategy "best" after one lucky try.
        return (l.successes + 1f) / (l.attempts + 2f);
    }

    public int attempts(String key){ Lesson l = lessons.get(key); return l == null ? 0 : l.attempts; }
    public int successes(String key){ Lesson l = lessons.get(key); return l == null ? 0 : l.successes; }
    public int failures(String key){ Lesson l = lessons.get(key); return l == null ? 0 : l.failures; }
    public int size(){ return lessons.size; }

    public Seq<Entry> entries(){
        Seq<Entry> result = new Seq<>();
        for(ObjectMap.Entry<String, Lesson> e : lessons) result.add(new Entry(e.key, e.value.attempts, e.value.successes, e.value.failures));
        return result;
    }

    public void restore(Seq<Entry> entries){
        lessons.clear();
        if(entries == null) return;
        for(Entry e : entries){
            if(e == null || e.key() == null || e.key().isEmpty()) continue;
            Lesson l = new Lesson();
            l.attempts = Math.max(0, e.attempts());
            l.successes = Math.max(0, Math.min(l.attempts, e.successes()));
            l.failures = Math.max(0, Math.min(l.attempts - l.successes, e.failures()));
            lessons.put(e.key(), l);
            if(lessons.size >= MAX_LESSONS) break;
        }
    }

    public record Entry(String key, int attempts, int successes, int failures){}
    private static final class Lesson { int attempts, successes, failures; }
}
