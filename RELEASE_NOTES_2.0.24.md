# MCA Reborn x Vampirism Compat 2.0.24

## Administrative tools

- Adds the OP level-2 `/mca_vamp_compat` command.
- `inspect <villager>` reports effective faction, canonical traits, state source, infection/cure, appearance, night schedule, Hunter pitchfork state, profession and Cycle Trades eligibility.
- `resync <targets>` reruns only the existing safe lifecycle reconciliation. It does not manually assign or remove a faction.
- `village info [totem_position]` shows the native and effective special-village bounds.
- `village outline [totem_position] [seconds]` draws a private, temporary particle outline for the same area used by fog, Hunter control and Book of Occult Arts discovery. It updates every 10 ticks, defaults to 30 seconds and accepts 5–60 seconds.

## Faction dialogue

- Adds `enableFactionContextualDialogues = true`.
- Vampire, Hunter and Werewolf villagers can add short localized ambient lines to ordinary MCA greetings, existing faction trade/social refusals and cure milestones.
- These lines never replace the functional message or change friendship, romance, prices, commands, combat or chances.

No faction rules, Easy Villagers behavior, Cycle Trades, inheritance, village capture, fog rules or Book of Occult Arts content were changed.
