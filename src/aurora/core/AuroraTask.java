package aurora.core;

/** High-level tasks understood by Aurora. The mining task keeps its ordinal position for save compatibility. */
public enum AuroraTask {
    FOLLOW,
    WAIT,
    MINE_RESOURCE,
    BUILD,
    REPAIR,
    DEFEND,
    RETURN_TO_CORE,
    EXPLORE
}
