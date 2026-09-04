package aurora.bridge;

/**
 * v4 boundary for an optional external AI service.
 * Keep this interface free of Mindustry-specific networking details.
 */
public interface ExternalAIProvider {
    String analyze(String compactGameState);
}
