# MCA Reborn x Vampirism Compat 2.0.19

## Immediate MCA village bounds updates

- Vampire fog now updates in the same MCA operation that expands, shrinks, merges or removes a village building footprint.
- The integration no longer scans loaded totems every five seconds.
- Hunter village alignment uses the same immediate old-and-new-area reconciliation, so villagers are updated as soon as the editable MCA border changes.
- Capture, raids, POIs, spawns and every other native Vampirism village mechanic remain bound to Vampirism's original territory.

### Configuration

The existing settings are unchanged:

- `mcaVillageEffectsFollowMcaBounds=true` enables the integration.
- `mcaVillageEffectsMaxHorizontalRadius=256` sets the horizontal safety cap.
