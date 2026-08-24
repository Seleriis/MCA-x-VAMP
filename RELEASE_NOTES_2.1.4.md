# MCA Reborn x Vampirism Compat 2.1.4

Requires **Minecraft 1.21.1**, **NeoForge 21.1.x**, **Java 21** and
**MCA Reborn 7.7.36-beta.3+1.21.1**.

## Active Hunter patrols

- Hunter-aligned MCA villagers now actively pursue revealed Vampire players
  on fresh installations.
- Their default melee damage is now **2.0** (one heart) per hit before armor,
  resistance and other normal damage modifiers.
- A Vampire disguise remains respected by default, and a player who is no
  longer a Vampire is never selected as a Hunter target.

## Existing worlds

Existing configuration files are never overwritten. To use the new defaults,
set the following values in `config/mca_vamp_compat-common.toml`:

```toml
hunterVillagersAttackOnlyWhileDefensive = false
hunterVillagerAttackDamage = 2.0
```
