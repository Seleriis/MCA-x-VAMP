# MCA Reborn x Vampirism Compat 2.0.15

## Placed Guard Cycle Trades hotfix

- Fixes MCA Guards that had never traded losing Cycle Trades after being captured and placed back into the world through Easy Villagers.
- Cycle Trades now uses a server-authoritative completed-trade marker and used offers, never the professional XP that mca-ev-compat restores for a Guard.
- The normal MCA merchant screen receives the same Cycle Trades state as faction-aware merchants; after the first real trade, the control is permanently disabled and later reroll requests are ignored.
- Restores the saved canonical Vampire, Hunter or Werewolf trait before an mca-ev-compat item places an MCA villager, so a manually assigned special villager keeps its faction behavior after placement.

No prices, offers, faction rules or capture permissions were changed.
