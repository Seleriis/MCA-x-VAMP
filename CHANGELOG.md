# Changelog

## 2.0.12 - Hunter Pitchfork Equipment Task Hotfix

- Reapplies a valid Hunter's pitchfork immediately after MCA's `EquipmentTask` refreshes the villager's dominant hand, preventing the visual hand flicker most visible on villagers with active trades or professions.
- Leaves MCA recovery-food use untouched until the villager has finished consuming the temporary item.
- Keeps original-hand preservation, faction state, trade offers and Easy Villagers snapshots unchanged.

## 2.0.11 - Capture, Pitchfork and Cycle Trades Hotfix

- Safely serializes a saved empty main hand, so an allied Hunter whose temporary pitchfork replaced an empty hand can be captured, saved and restored without losing data.
- Reads old empty-hand state safely and reports permitted pickup failures clearly when the inventory is full, the villager is sleeping or a snapshot cannot be created.
- Centralizes Hunter pitchfork reconciliation so it is equipped only while a valid Hunter/defensive source exists and the original hand is restored once when that source ends.
- Makes the Cycle Trades lock authoritative and persistent after the first trade for both MCA villagers and Easy Villagers Trader sessions; the button is disabled immediately and later reroll requests are ignored.
- Keeps pre-trade rerolls safe by resetting the selected offer before the new offer list is sent.

## 2.0.10 - Merchant Wrapper Trade Completion Hotfix

- Fixes a `ClassCastException` when Shift-clicking a result in faction-aware merchant menus.
- Routes the vanilla trade confirmation sound through the real MCA/stored villager instead of casting the temporary merchant wrapper to an entity.
- Applies the fix to live MCA vampire/hunter/werewolf trades and Easy Villagers Trader-block sessions.
- Adds an integration regression test that completes a discounted Hunter trade by Shift-click and verifies the use on the real stored offer.

## 2.0.9 - Cycle Trades for Every Faction and Release Polish

- Makes Easy Villagers' Cycle Trades control available in every faction-aware merchant wrapper, including vampire, hunter and werewolf MCA villagers.
- Keeps the original Easy Villagers feature toggles, artwork, position and input handling while routing rerolls to the real villager behind the wrapper.
- Allows faction villagers whose MCA/merchant metadata exposes progress or experience to use the control instead of being incorrectly treated as ineligible humans.
- Removes inactive legacy pickup settings, obsolete eye-overlay code, unused future toggles and the unregistered Vampirism Integrations mixin.
- Makes the mca-ev-compat state bridge automatic whenever that optional integration is available, preserving the owner snapshot and legacy world migration.
- Refreshes release metadata, configuration documentation, CurseForge copy and distributable checksums.

## 2.0.8 - Easy Villagers Cycle Trades

- Restores Easy Villagers' native Cycle Trades button on faction-aware merchant wrappers.
- Routes the cycle packet to the real stored villager instead of letting Easy Villagers discard the wrapper merchant.
- Regenerates and persists the stored villager's offers while preserving faction refusals, reputation modifiers and faction discounts.
- Keeps the feature optional and honors Easy Villagers' server and client settings, novice-only restriction and original button artwork.

## 2.0.7 - Authoritative Hunter Trade Fix

- Routes MCA's direct right-click trade path through the same Hunter refusal and price wrapper used by its dialogue command.
- Intercepts the concrete Easy Villagers Trader block before its original GUI can open, while retaining the tile-entity hook as a fallback.
- Validates both authoritative entry points against real MCA Hunter traits and mca-ev-compat visual snapshots.

## 2.0.6 - Book Arrow and Author Style Fix

- Places the four supplied page-turn sprites in MCA's exact 23×13 atlas cells.
- Makes Seleris' title-page credit regular italic instead of inheriting the title's bold style.

## 2.0.5 - Book of Occult Arts Visual and Content Refresh

- Replaces the item icon and page atlas with the supplied purple artwork.
- Uses a centered, wrapped title page with the correct author and readable dark colors.
- Replaces all 30 pages with the revised player-facing guide in Brazilian Portuguese, European Portuguese and English.
- Scales only pages that exceed MCA's native 14-line area so no text is clipped.
- Removes the redundant main-hand tooltip while preserving the safe offhand warning.

## 2.0.4 - Stored Hunter Trades and Occult Arts Guide

- Reads the authoritative 1.21.1 mca-ev-compat visual snapshot component when classifying villagers stored in Trader blocks.
- Recognizes MCA 7.7 canonical faction traits in their real boolean-key NBT format, including old v2 payloads whose `Hunter=false` flag previously hid a true Hunter trait.
- Treats false legacy payload flags as missing evidence so canonical MCA faction traits remain authoritative.
- Writes mutually exclusive version 3 faction payloads from both synchronized state and canonical traits.
- Validates the exact Easy Villagers `openTradingGUI(Player)Z` hook and replaces the direct Trader GUI before offers are opened.
- Adds the MCA-style Book of Occult Arts with a player-facing guide to factions, infection, curing, inheritance, villages and Easy Villagers integration.
- Gives the guide once when a player discovers an active vampire, hunter or werewolf-controlled village and unlocks its replacement recipe.

## 2.0.3 - Stored Hunter and Werewolf Inheritance Fixes

- Prioritizes the mca-ev-compat visual villager snapshot for stored faction classification.
- Keeps inherited werewolf children valid when child infection by bites is disabled.
- Reads Breeder parents from their visual snapshots and preserves inherited state through Incubator.

## 2.0.2 - Trader, Sunlight and Inheritance Fixes

- Intercepts the Easy Villagers Trader tile directly so Hunter trade blocks and faction discounts cannot be bypassed by its original GUI.
- Replaces the conflicting vanilla flee-sun goal with MCA Brain `WALK_TARGET` shelter navigation.
- Applies faction inheritance in MCA's actual `Pregnancy.createChild` path, which does not fire NeoForge's `BabyEntitySpawnEvent`.
- Persists inheritance decisions through MCA Baby Items and the mca-ev-compat Breeder/Incubator item flow.
- Adds werewolf inheritance chances: 45% for one werewolf parent and 100% for two.
- Makes inheritance idempotent and mutually exclusive between vampire and werewolf state.

## 2.0.1 - NeoForge 1.21.1 Compatibility Hotfix

- Fixed MCA villagers with uninitialized Vampirism blood data being impossible to drain (`max_blood` now uses the correct 1.21.1 key).
- Fixed inherited upper-case faction traits that could make otherwise human newborns count as vampires.
- Migrates the accidental 2.0.0 upper-case trait ids to the canonical legacy namespace without converting human villagers.
- Uses MCA 7.7's native eye color, brightness and skin genetics instead of obsolete face overlays and late `SynchedEntityData` fields.
- Vampire eyes are native bright red; vampire skin is derived one MCA tone lighter from the saved original appearance.
- Fixed the Villager Editor crash caused by querying trade offers on the logical client.
- Added Brazilian and European Portuguese migration-safe trait translations.

## 2.0.0 - NeoForge 1.21.1 Port

- Ported the project to Minecraft 1.21.1, Java 21 and NeoForge 21.1.234.
- Updated MCA packages and hooks for MCA 7.7.25+1.21.1 and Vampirism 1.10.12.
- Replaced Forge capabilities with a serializable NeoForge attachment while retaining the legacy `vampirism_mca_compat` trait namespace.
- Added automatic migration of the 1.0.3 `ForgeCaps` state without replacing MCA villagers.
- Updated synced entity data, damage/tick events, registries, effects, render buffers and item data components for 1.21.1.
- Preserved vampire, hunter, werewolf, infection, cure, AI, combat, sunlight, guard, trade, social, command and village-capture rules.
- Added a versioned `mca_vamp_compat_state` Easy Villagers payload and payload-first faction resolution.
- Added optional save/restore hooks for mca-ev-compat 2.0.0; the owner snapshot remains untouched.
- Disabled unvalidated Vampirism Integrations mixins; core blood handling remains available without that add-on.
- Added unit tests for state round-trip, 1.0.3 migration and Easy Villagers item payload classification.

## 1.0.3 - Red Eyes, Blood Values and Stability Cleanup

- Stabilized local ForgeGradle deobfuscation for optional mod jars to avoid inherited vanilla methods being emitted with unmapped names.
- Hotfix: removed the temporary `VillagerEntityMCA` vanilla bridge mixin used during testing.
- Hotfix: restored visual synced-data code to the stable runtime path and fixed the production owner mapping through the build.
- Hotfix: removed a fragile `Mob` goal selector accessor that could crash during mixin apply in production.
- MCA vampire villagers now use MCA's native red custom eye face overlays by default.
- The default MCA red-eye mode chooses a stable per-villager variant from face indices `1`, `8`, `12` and `19`.
- Custom red eyes are supplied through MCA's own normal `FaceLayer`, so the normal face layer does not cover them.
- Added `vampire.useMcaRedEyesForMcaVampires`, default `true`.
- Changed `vampire.useVampirismEyesForMcaVampires` default to `false`; enabling it keeps the previous Vampirism eye overlay behavior.
- The normal MCA face layer is only hidden when the Vampirism eye overlay is active.
- The new eye rendering is client-side only and does not alter MCA genetics, NBT, traits, family or relationship data.
- Added MCA villager blood support without requiring Vampirism Integrations.
- When Vampirism Integrations is installed, its own MCA blood values are used; this mod no longer ships duplicate `bloodvalues` JSON.
- Without Vampirism Integrations, MCA male and female villagers use a runtime fallback blood value of `15` through Vampirism's core `ExtendedCreature` capability.
- Added a small repair for MCA villagers whose Vampirism blood capability was created or loaded without valid blood.
- Player vampire blood drain uses the standard Vampirism core bite/feed flow, including the normal UI and feedback.
- Vampirism Integrations is no longer required just to make MCA villagers valid blood sources.
- Existing blocks for MCA vampire targets and hunter poisonous blood remain in the same event flow.
- Sanguinare infection behavior for valid player bites is preserved.
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
