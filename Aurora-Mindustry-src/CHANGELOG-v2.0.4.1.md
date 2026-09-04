# Aurora AI Companion v2.0.4.1

## Stabilization and intelligence repair

### Fixed
- Fixed a compilation error in `DefenseProposal` where the Aurora profile was referenced outside its scope.
- Generalized nearby drill detection so resource recovery checks for a drill that can actually mine the requested item instead of only looking for copper drills.
- Removed premature learning success when a resource-recovery mission merely starts. Recovery is now rewarded only after Aurora returns to the core and deposits the recovered stack.
- Construction learning no longer penalizes a block when the only blocker is temporary lack of materials.
- Added repair-mission interruption memory so Aurora can repair a damaged structure and then resume the task she was doing before the repair detour.
- Increased learning stability with small-sample confidence smoothing.

### Intelligence adjustments
- Aurora now distinguishes a strategy failure from a temporary resource blocker.
- Adaptive construction preferences are less likely to overreact to one bad placement.
- Resource recovery decisions are now tied to the exact requested item.
- Repair behavior behaves as a temporary priority rather than silently abandoning the previous mission.
- Save data version increased to 5 while retaining the existing `aurora.state.v2` chunk name and backwards reads for earlier state versions.

## Compatibility
- Target: Mindustry 159.7
