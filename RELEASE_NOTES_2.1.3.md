# MCA Reborn x Vampirism Compat 2.1.3

Requires **Minecraft 1.21.1**, **NeoForge 21.1.x**, **Java 21** and
**MCA Reborn 7.7.36-beta.3+1.21.1**.

## Fixed player combat

- Normal melee hits by Vampire players no longer count as blood-drinking
  bites. This includes fists, weapons and Better Combat attacks.
- MCA villagers now receive normal damage from those attacks and Hunter blood
  only poisons a player during a real Vampirism blood-drinking attempt.
- Player bite damage compatibility keys are retained in existing TOML files,
  but are now obsolete because genuine bites use Vampirism's own flow.
