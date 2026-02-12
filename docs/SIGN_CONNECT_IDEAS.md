# Sign Connect Mod — Ideas

## Summary

Make vanilla (and mod) signs visually connect when placed adjacent/stacked so they render as a single, contiguous multiblock sign. Remove poles between connected sign faces and keep poles only at lower extremities. Allow adding/removing poles by right-clicking a sign with a stick.

## Goals
- Seamlessly connect sign textures/models when signs are adjacent (vertical and horizontal groups).
- Support large multiblock signs (e.g., 4x3) where only the lower edge(s) render poles.
- Preserve compatibility with vanilla sign data and other mods' sign blocks where possible.
- Provide player interaction: toggle pole presence by right-clicking with a stick.

## Behavior Details

- Adjacent signs (sharing edges or stacked vertically) should visually merge: the connecting gap/pole is removed and textures/models align to appear as one sign face.
- For vertically stacked signs, only the lowest row shows poles. For horizontally adjacent signs, poles appear at the left/right extremities depending on orientation and configuration.
- The connect logic should consider world placement (block positions and facing). Signs of different text or content remain separate logically but visually connected.
- Right-clicking any connected sign with a stick toggles the pole presence for the entire contiguous sign group or only the clicked sign (decide in implementation). Default: toggle local pole for whole multiblock group.

## Examples

- Single-column: stacking 3 sign blocks vertically produces a single 1x3 sign; only bottom sign shows pole(s).
- Large sign area: placing an array 4x3 of signs forms a 4x3 sign; poles only at the lower edge(s) of the whole connected region.

## Technical Approach (high level)

1. Detection
   - When a sign is placed/removed or its blockstate updates, scan neighbouring sign blocks to compute the connected region (flood-fill limited to signs with compatible facing/orientation).
   - Maintain a lightweight grouping (no heavy persistent tile data required if we can compute on demand), but cache results for rendering if needed.

2. Data model
   - Prefer to avoid mandatory new tile entities for each sign; use block states or an optional `SignGroup` capability/attached data to store minimal metadata (e.g., group bounds, which edges have poles suppressed).
   - Consider storing a per-sign bitmask for which edges/poles to render.

3. Rendering
   - Use custom block model/baked model or a model override to hide pole geometry when a pole is suppressed.
   - For larger connected regions consider a multi-part baked model that stretches textures, or keep individual sign faces but adjust their models so seams match.
   - Ensure mipmaps/texture alignment look correct when signs are visually merged.

4. Interaction
   - Right-click with stick: find group, toggle pole visibility flag for group (or per-sign depending on chosen UX), mark blocks for model update, and send packets to clients.

5. Networking / Sync
   - Sync pole visibility and any group metadata to clients. Keep network messages minimal; recompute on client if possible.

6. Compatibility
   - Detect vanilla `SignBlock` and common mod sign blocks by checking class or interfaces; provide a small API to opt-in other mods.
   - Fall back gracefully if a sign block does not expose a compatible model (leave it unchanged).

## Edge Cases & Notes

- Signs placed with different facings (rotations) should not connect unless intentionally supported (e.g., same facing and alignment).
- If a connected group changes shape (a sign removed), recalc affected group and restore poles where needed.
- Consider chunk boundaries and chunk load/unload for recalculation performance.

## UX Decisions to confirm

- Toggle behavior: should stick toggle the entire connected group or just the clicked sign? (Recommendation: group toggle.)
- Visual smoothing vs. exact texture tiling across sign boundaries. (Recommendation: align textures per-sign first, then consider larger combined model if artifacts remain.)

## Next Implementation Steps

1. Design block/tile-entity model and decide whether to store group metadata persistently.
2. Prototype connection detection and group computation (server-side).
3. Create/hide pole model variant; implement client model baking to hide poles dynamically.
4. Implement stick interaction and network sync.
5. Test with vanilla and a few mod sign types.

---

Notes: This mod targets Forge 1.20.1. Initial work should add the ideas document, then create a small prototype for detection and rendering toggles.
