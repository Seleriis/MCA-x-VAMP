# MCA Reborn x Vampirism Compat

Compatibility addon for Minecraft 1.20.1 Forge that integrates MCA Reborn villagers with Vampirism, Werewolves, Vampirism Integrations and Easy Villagers while keeping MCA villagers as real `VillagerEntityMCA` entities.

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
- Balanced configurable attributes and combat movement.
- Easy Villagers support through mca-ev-compat.

## Vampire MCA Villagers

- MCA villagers can carry Vampirism infection and become MCA vampire villagers through compat-controlled state.
- Converted vampire villagers keep their MCA entity and MCA data.
- Vampire AI can bite valid human targets when configured.
- Vampire villagers can avoid sunlight or burn in sunlight depending on config.
- Vampire appearance is restored safely after cure.

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

Easy Villagers base MCA handling is delegated to mca-ev-compat. This mod does not preserve MCA NBT, restore villagers, render Easy Villagers items/blocks, perform technical pickup, or prevent duplication in the Easy Villagers base flow.

This mod only applies faction-aware policy around that flow:

- pickup permission by faction;
- pickup block during curing;
- read-only stored villager faction detection for Trader block behavior;
- faction-aware Trader block trade when stored MCA data can be resolved safely.

Stored Easy Villagers NBT is inspected read-only. It is not modified by this mod.

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

The Forge common config is generated at:

```text
config/mca_vamp_compat-common.toml
```

Release-facing categories:

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
delegateBaseEasyVillagersCompatToMcaEvCompat = true
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

[debug]
enableDebugLogs = false
enableVerboseAiDebugLogs = false
enableVerboseNbtDebugLogs = false
```

If an existing config file was generated before a release polish, Forge will not overwrite old values automatically. Adjust values in Configured/TOML or regenerate the config when testing new defaults.

## Requirements

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- MCA Reborn 7.6.16 or compatible 1.20.1 build
- Vampirism 1.20.1-1.10.15 or compatible 1.20.1 build

Optional:

- Werewolves
- Vampirism Integrations
- Easy Villagers
- mca-ev-compat, recommended when Easy Villagers is installed
- VampiricAgeing
- GodlyVampirism

## Known Notes

- Java packages still use the historical internal package name `com.guilh.mca_vampirism_compat`; this is internal and not the public mod id.
- MCA trait ids still use the legacy `vampirism_mca_compat` namespace to preserve existing villager data.
- The public mod id is `mca_vamp_compat`.
- The final jar name is `mca-vamp-compat-1.20.1-1.0.3.jar`.
- Guide book integration is not included in this release. It may be explored again in a future update.

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

MCA Reborn x Vampirism Compat integra aldeões do MCA Reborn com Vampirism, Werewolves, Vampirism Integrations e Easy Villagers. Os aldeões MCA podem se tornar vampiros, caçadores ou lobisomens sem serem substituídos, mantendo identidade, aparência, família, relacionamentos e dados sociais do MCA.

O mod adiciona trocas, interações sociais, cura, permissões de pickup, permissões de comandos do MCA, comportamento contra monstros, captura de vila segura e suporte ao Easy Villagers via mca-ev-compat.

## Build

This repository does not version third-party mod jars. Before building, create a local `local-mods/` folder in the project root and place these compile-only dependency jars inside it:

- `Vampirism-1.20.1-1.10.15.jar`
- `minecraft-comes-alive-7.6.16+1.20.1-universal.jar`

The `local-mods/` folder is intentionally ignored by Git.

From the project root:

```bat
gradlew.bat clean build
```

The final jar is expected at:

```text
build/libs/mca-vamp-compat-1.20.1-1.0.3.jar
```

## Runtime Marker

Current implementation marker:

```text
BUILD_MARKER = 1.0.3-stability-cleanup
```

The log should show:

```text
[MCA Reborn x Vampirism Compat] BUILD_MARKER = 1.0.3-stability-cleanup
```

## Feedback and Bug Reports

This mod interacts with several complex villager and faction systems, so bugs may still happen. Feedback, bug reports and compatibility suggestions are welcome.

When reporting an issue, include your mod list, `latest.log`, crash report if available, config file and the build marker shown in the log.

## License

GPL-3.0-only
