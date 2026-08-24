# MCA Reborn x Vampirism Compat 2.0.29

## MCA Village Bounds

- MCA-derived compat effects now use the complete editable MCA village footprint, without the former horizontal radius cap.
- Active Vampire village fog can extend beyond that border with the new `mcaVillageFogBorderMarginBlocks` setting, defaulting to four blocks.
- The fog margin is visual only: Hunter control, Book of Occult Arts discovery and `/mca_vamp_compat village outline` remain on the exact MCA village boundary.
- Changing the common configuration reapplies the fog immediately to loaded totems.

## Compatibility

Minecraft 1.21.1, NeoForge 21.1.234, MCA 7.7.25+1.21.1 and Vampirism 1.10.12.
