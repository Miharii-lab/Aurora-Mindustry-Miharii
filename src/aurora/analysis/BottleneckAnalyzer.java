package aurora.analysis;

/** Contract for observations. Implementations must not mutate the map. */
public interface BottleneckAnalyzer {
    Observation analyze();
    record Observation(String category, String message, float confidence) {}
}
