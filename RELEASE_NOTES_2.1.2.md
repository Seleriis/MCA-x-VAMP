# MCA Reborn x Vampirism Compat 2.1.2

Requires **Minecraft 1.21.1**, **NeoForge 21.1.x**, **Java 21** and
**MCA Reborn 7.7.36-beta.3+1.21.1**.

## Stability and localisation polish

- Active village missions now resolve their assigned resident directly instead
  of scanning the entire MCA village every check. Large edited villages keep
  the same gameplay with lower server work.
- Temporary fog, outline and inherited-birth tracking is cleared safely when a
  server stops.
- Diagnostic cooldown caches are bounded during long-running worlds.
- Completes Portuguese (Portugal) messages for cure, trade, social, command
  and capture interactions.

No villager traits, capabilities, saved mission data, faction rules or trade
prices were changed.

If Easy Villagers is installed, keep using
`mca-ev-compat-1.21.1-2.1.0.jar` alongside this release.
