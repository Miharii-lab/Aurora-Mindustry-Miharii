package aurora.core;

/** Runtime states. New values are appended for save compatibility. */
public enum AuroraState {
    IDLE,
    FOLLOWING,
    WAITING,
    MINING,
    BUILDING,
    REPAIRING,
    DEFENDING,
    RETURNING,
    EXPLORING
}
