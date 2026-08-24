# MCA Reborn x Vampirism Compat 2.0.28

## Release Validation

- The GameTest suite now respects the optional Werewolves dependency, allowing every supported dependency profile to validate cleanly.
- Optional Easy Villagers and mca-ev-compat mixins are skipped before their targets are resolved when those mods are absent, keeping optional-profile logs clean.
- Documentation now correctly explains that faction prices are a temporary overlay on the villager's real offers, restored when the trade session closes.

## Compatibility

Minecraft 1.21.1, NeoForge 21.1.234, MCA 7.7.25+1.21.1 and Vampirism 1.10.12.
