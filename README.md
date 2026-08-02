# MCA Reborn x Vampirism Compat

Compatibility addon for Minecraft 1.21.1, Java 21 and NeoForge 21.1.234+ that integrates MCA Reborn villagers with Vampirism, Werewolves and Easy Villagers while keeping MCA villagers as real `VillagerEntityMCA` entities.

## Overview

This mod adds vampire, hunter and werewolf faction behavior to MCA Reborn villagers without replacing the MCA entity. The intent is compatibility first: villagers keep their MCA identity, appearance data, social data, family links and relationship data while gaining faction-aware combat, curing, trade, social, pickup and command rules.

## Main Features

- Preserves MCA villagers as `VillagerEntityMCA`.
- MCA vampire villagers.
- MCA hunter-aligned villagers.
- MCA werewolf-aligned villagers.
- Vampire and werewolf curing.
- Faction-aware trade and social rules.
- Faction-aware Easy Villagers pickup permissions.
- Faction-aware MCA command permissions.
- Special AI respects MCA follow/stay commands.
- Safe Vampirism village capture handling.
- Monster ignore and panic suppression for special villagers.
- MCA guards that become special villagers can still defend villages against common hostile mobs when configured.
- Player vampires can drain blood from MCA villagers through Vampirism's standard bite/feed flow, with a compat fallback whenever no valid 1.21.1 blood entry was initialized.
- Balanced configurable attributes and combat movement.
- Easy Villagers support through mca-ev-compat.
- An MCA-style Book of Occult Arts that explains the compatibility gameplay to players.

## Book of Occult Arts

The non-stackable guide uses MCA's native book screen and contains 30 short pages in Brazilian Portuguese, European Portuguese and English. Each player receives it once after entering an active Vampire-, Hunter- or Werewolf-controlled village. The discovery also reveals its replacement recipe: a normal book, a vampire fang and garlic.

Automatic delivery can be disabled without disabling the item or recipe:

```toml
[guide_book]
giveOccultArtsBookOnSpecialVillageDiscovery = true
```

## Vampire MCA Villagers

- MCA villagers can carry Vampirism infection and become MCA vampire villagers through compat-controlled state.
- Converted vampire villagers keep their MCA entity and MCA data.
- Vampire AI can bite valid human targets when configured.
- Vampire villagers can avoid sunlight or burn in sunlight depending on config.
- Vampire villagers use MCA 7.7's native iris color pipeline with bright red RGB and maximum brightness.
- Vampire skin is derived one MCA tone lighter from the saved original skin, including compatible lightening of custom skin dyes.
- Vampire appearance is restored safely after cure.
- Vampire players can drain MCA villagers through the standard Vampirism bite/feed flow. A valid registered blood value is preferred; otherwise the mod repairs MCA villagers initialized without blood with a fallback value of `15`, even when an optional integration add-on is present.

## Hunter MCA Villagers

- MCA villagers can become hunter-aligned through hunter-controlled villages or explicit hunter state.
- Hunter villagers can use poisonous blood against vampire biters.
- Hunter village sync is handled without replacing MCA villagers.
- Defensive villager behavior is limited by the hunter aggression policy.
- In 1.0.2, MCA guards that become vampire, hunter or werewolf villagers can keep defending villages against common hostile mobs when guard defense is enabled.
- Ordinary special villagers still ignore common monsters as before, and guard panic/flee behavior remains suppressed.

## Werewolf MCA Villagers

- MCA villagers can become werewolf-aligned through compat-controlled state.
- Werewolf villagers keep MCA identity and use partial visual features such as ears/claws.
- Full werewolf entity/model replacement is intentionally not used.
- Werewolf trade, social, AI and curing rules are faction-aware.

## Faction Trade & Social

- Vampire, hunter and werewolf villagers apply faction-aware trade and social behavior.
- Player disguise and werewolf transformation state are handled separately.
- Non-transformed werewolf players are not treated as transformed werewolves.
- Trade price changes are applied only to copied temporary offers.
- Existing discounts such as zombie-cure/reputation discounts stack with faction modifiers.

## Easy Villagers Compatibility

Easy Villagers base MCA handling remains delegated to mca-ev-compat. This mod does not replace its MCA snapshot, render Easy Villagers items/blocks, perform technical pickup, or own duplication prevention.

This mod only applies faction-aware policy around that flow:

- pickup permission by faction;
- pickup block during curing;
- versioned `mca_vamp_compat_state` payload stored alongside the owner snapshot;
- payload-first stored villager faction detection for Trader block behavior;
- faction-aware Trader block trade when stored MCA data can be resolved safely.
- direct Trader tile interception so faction blocks and Hunter discounts also apply inside the block;
- Easy Villagers' Cycle Trades button in every permitted faction-aware Trader menu, available only before that villager's first completed trade;
- inherited vampire/werewolf state preserved through Breeder and Incubator output.

The state bridge is automatic when mca-ev-compat is present. Only the dedicated compat payload in the vanilla custom-data component is modified; the proprietary mca-ev-compat snapshot is left untouched. Legacy 1.20.1 capability/NBT detection remains available as migration fallback.

Cycle Trades still follows Easy Villagers' own client/server enable settings, artwork, position and input. Faction wrappers deliberately accept special villagers whose MCA metadata reports trade progress or XP so vampires, hunters and werewolves are not hidden by the vanilla-human eligibility check. The first completed trade permanently disables rerolls for that villager, including after a world restart.

## MCA Commands & Faction Permissions

- Special AI respects MCA follow/stay commands.
- Follow keeps MCA movement control; the compat suppresses autonomous hunting without stopping the follow command.
- Stay blocks autonomous combat movement.
- Enemy factions cannot command special villagers by default.
- Disguise does not bypass command or pickup permission because real player faction is used for permissions.

## Village Capture Safety

- Vampirism village capture can affect MCA villagers without replacing them.
- `MakeAggressive` replacement paths are blocked for MCA villagers.
- Hunter-controlled villages can grant hunter state where configured.
- Vampire capture can infect MCA villagers by config, but does not force entity replacement.

## Curing

Converted MCA vampire and werewolf villagers use a three-requirement cure ritual:

- Weakness;
- golden apple;
- `vampirism:injection_garlic`.

Cure progress is tracked separately for vampire and werewolf cure flows, and the final cure path removes faction state/visual data without replacing the MCA entity.

## Configuration

The NeoForge common config is generated at:

```text
config/mca_vamp_compat-common.toml
```

Release-facing categories:

- `guide_book`
- `general`
- `balance`
- `vampire`
- `hunter`
- `werewolf`
- `factions`
- `easy_villagers`
- `mca_commands`
- `monster_behavior`
- `village_capture`
- `debug`
- `advanced`

Useful defaults:

```toml
[balance]
specialVillagerMeleeGoalSpeed = 0.65
specialVillagerChaseGoalSpeed = 0.70
specialVillagerMaxNavigationSpeed = 0.75
specialVillagerMaxChaseDistance = 20
specialVillagerCombatRepathIntervalTicks = 10
specialVillagersTargetFactionNpcs = false

[easy_villagers]
enableEasyVillagersFactionRules = true
enableEasyVillagersFactionPickupRules = true
blockPickupOfCuringVillagers = true
showPickupBlockedMessage = true

[mca_commands]
respectMcaPlayerCommands = true
enableFactionCommandRestrictions = true
showCommandBlockedMessage = true

[monster_behavior]
specialMcaGuardsDefendAgainstHostileMobs = true
specialMcaGuardsStillSuppressPanic = true
specialMcaGuardsAttackCreepers = false

[vampire]
vampireEyesEnabled = true
vampirePlayerBiteDealsDamageToMcaVillagers = false
vampirePlayerBiteDamageToMcaVillagers = 0.0

[werewolf]
oneWerewolfParentInheritanceChance = 0.45
twoWerewolfParentsInheritanceChance = 1.0

[debug]
enableDebugLogs = false
enableVerboseAiDebugLogs = false
enableVerboseNbtDebugLogs = false
```

`vampirePlayerBiteDealsDamageToMcaVillagers=false` makes player vampire bite interactions avoid normal physical attack damage against MCA villagers. The bite can still drain blood through Vampirism and apply the compat infection/poison rules, but it will not accidentally kill the villager as a regular attack. If enabled, the physical bite damage is allowed and can be capped with `vampirePlayerBiteDamageToMcaVillagers`.

Version 2.0.9 removed settings that had no active implementation, including the old pickup matrix, obsolete Vampirism-eye renderer switches and architectural mca-ev-compat bridge toggles. Existing worlds and legacy villager payloads remain supported. Regenerate `mca_vamp_compat-common.toml` if you want a compact file containing only current options.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.234+
- Java 21
- MCA Reborn 7.7.25+1.21.1 or newer compatible build
- Vampirism 1.21-1.10.12 or newer compatible build

Optional:

- Werewolves
- Vampirism Integrations, optional; if installed, its MCA blood values are used. If it is absent, this mod provides a safe runtime fallback for MCA villager blood.
- Easy Villagers
- mca-ev-compat, recommended when Easy Villagers is installed
- VampiricAgeing
- GodlyVampirism

## Known Notes

- Java packages still use the historical internal package name `com.guilh.mca_vampirism_compat`; this is internal and not the public mod id.
- MCA trait ids still use the legacy `vampirism_mca_compat` namespace to preserve existing villager data.
- The public mod id is `mca_vamp_compat`.
- The final jar name is `mca-vamp-compat-1.21.1-2.0.12.jar`.

## Safety Guarantees

This addon does not intentionally use:

- `replaceEntity`
- `entityRegistry().convert`
- `convertTo`
- `discard + addFreshEntity`
- `ConvertedVillagerEntity`
- `AggressiveVillagerEntity`
- manual FamilyTree edits
- manual Relationship edits
- persistent mutation of real villager `MerchantOffers`

## CurseForge Summary

MCA Reborn x Vampirism Compat is a compatibility addon that makes MCA Reborn villagers work safely with Vampirism, Werewolves, Vampirism Integrations and Easy Villagers. MCA villagers can become vampires, hunters or werewolves without being replaced, keeping their identity, appearance, family, relationships and MCA social data.

The mod adds faction-aware trade, social interactions, curing, pickup permissions, MCA command permissions, monster behavior, village capture safety and Easy Villagers support through mca-ev-compat.

## Resumo em Português

MCA Reborn x Vampirism Compat integra aldeões do MCA Reborn com Vampirism, Werewolves e Easy Villagers. Os aldeões MCA podem se tornar vampiros, caçadores ou lobisomens sem serem substituídos, mantendo identidade, aparência, família, relacionamentos e dados sociais do MCA.

O mod adiciona trocas e interações sociais por facção, cura, herança, permissões de captura, comandos do MCA, comportamento contra monstros, captura segura de vilas e suporte ao Easy Villagers via mca-ev-compat.

## Build

This repository does not version third-party mod jars. Before building, create a local `local-mods/` folder in the project root and download these 1.21.1 development dependencies from their official project pages:

- `Vampirism-1.21-1.10.12.jar`
- `mca-neoforge-7.7.25+1.21.1.jar`
- `Werewolves-1.21-2.0.3.3.jar`
- `easy-villagers-neoforge-1.21.1-1.1.42.jar`
- `mca-ev-compat-1.21.1-2.0.0.jar`

Werewolves, Easy Villagers and mca-ev-compat remain optional at runtime, but their development jars are needed to compile and test the optional compatibility hooks. The `local-mods/` folder, generated JARs and local test worlds are intentionally ignored by Git.

From the project root:

```bat
gradlew.bat clean test releaseBundle
```

The distributable jar is generated at:

```text
build/libs/mca-vamp-compat-1.21.1-2.0.12.jar
```

The complete release bundle, including sources, notes and SHA-256 hashes, is generated at:

```text
build/release/mca-vamp-compat-1.21.1-2.0.12/
```

## Runtime Marker

Current implementation marker:

```text
BUILD_MARKER = 2.0.12-neoforge-1.21.1
```

The log should show:

```text
[MCA Reborn x Vampirism Compat] BUILD_MARKER = 2.0.12-neoforge-1.21.1
```

## Feedback and Bug Reports

This mod interacts with several complex villager and faction systems, so bugs may still happen. Feedback, bug reports and compatibility suggestions are welcome.

When reporting an issue, include your mod list, `latest.log`, crash report if available, config file and the build marker shown in the log.

## License

GPL-3.0-only
