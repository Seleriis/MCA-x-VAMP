# Changelog

## 1.0.3 - Stability Cleanup

- Removed the unfinished in-game guide book integration.
- Removed optional guide registration code, guide lang keys and guide config.
- Stabilized local ForgeGradle deobfuscation for optional mod jars to avoid inherited vanilla methods being emitted with unmapped names.
- Hotfix: removed the temporary `VillagerEntityMCA` vanilla bridge mixin used during testing.
- Hotfix: restored visual synced-data code to the stable runtime path and fixed the production owner mapping through the build.
- Hotfix: removed a fragile `Mob` goal selector accessor that could crash during mixin apply in production.
- Runtime behavior remains aligned with 1.0.2.
- No gameplay, balance, AI, curing, trade/social, pickup, command or capture rules were changed.

## 1.0.2 - Guard Defense / Release Polish

- Added configurable guard-defense exception for MCA guards that are vampire, hunter, werewolf or defensive villagers.
- MCA special guards now keep defending villages against common hostile mobs.
- Works for vampire, hunter and werewolf MCA guards.
- Normal special villagers still ignore common monsters as before.
- Common hostile mobs may target special MCA guards when guard defense is enabled, so guard combat is not silently canceled by monster-ignore.
- Villager panic/flee behavior remains suppressed for special villagers.
- Creeper auto-targeting is disabled by default through `monster_behavior.specialMcaGuardsAttackCreepers=false`.
- Added/updated configs for special guard defense behavior.
- Final release polish, config comments and documentation cleanup.
- Follow/stay command respect, trade/social, curing, pickup, Easy Villagers delegation and village capture behavior were not changed.

## 1.0.1 - Release cleanup

- Public mod identity changed to `MCA Reborn x Vampirism Compat`.
- Public mod id changed to `mca_vamp_compat`.
- Final jar name changed to `mca-vamp-compat-1.20.1-1.0.1.jar`.
- License set to `GPL-3.0-only`.
- Final release cleanup removed old local `javap` investigation dumps.
- README feature text was rewritten for release/CurseForge usage.
- Configured categories reorganized into broader release-facing sections: `vampire`, `hunter`, `werewolf`, `factions`, `easy_villagers`, `mca_commands`, `village_capture`, `monster_behavior`, `balance`, `debug`, and `advanced`.
- Added `balance.specialVillagersTargetFactionNpcs`, default `false`, so MCA special villagers do not automatically start faction wars with non-MCA Vampirism/Werewolves NPCs by default.
- Polished special villager movement: normal MCA navigation is preserved outside combat, combat chase speed is moderated, and default base movement multipliers stay close to normal MCA villagers.
- Polished combat movement/focus: lowered chase defaults, added combat repath cooldown, and forced look control toward the active combat target while the combat goal is running.
- Faction-aware Easy Villagers pickup permissions are enabled while base pickup/restore/render/NBT handling remains delegated to mca-ev-compat.
- Faction-aware MCA command permissions are enabled and special AI respects MCA follow/stay.
- Vampire/werewolf curing, werewolf support, price stacking, trade/social rules, monster behavior and safe village capture are included in the release feature set.
- Existing gameplay defaults from the balance polish were preserved.
- No gameplay rules were changed in the final cleanup pass.
