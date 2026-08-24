# MCA Reborn x Vampirism Compat 2.0.18

## MCA village effect bounds

- Vampire village fog now follows the editable MCA village footprint when its faction totem is inside that MCA village.
- Hunter village alignment and Book of Occult Arts discovery use the same compat-effective territory.
- The default safety limit is 256 blocks from the totem in each horizontal direction, for a maximum 512×512 area.
- Capture, raids, spawns, POIs and all other native Vampirism village mechanics continue to use the original Vampirism area.

### Configuration

- `mcaVillageEffectsFollowMcaBounds=true` enables this integration.
- `mcaVillageEffectsMaxHorizontalRadius=256` sets the horizontal safety cap.
