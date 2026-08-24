# MCA Reborn x Vampirism Compat 2.0.36

## Village Mission Fixes

- Crimson Reserve can now be completed with full Vampirism Blood Bottles held separately in the player's inventory. Blood bottles do not need to stack.
- New village requests expire after two Minecraft days by default. Server owners can change this with `occultVillageMissionDeadlineTicks`.
- Accepted requests can be canceled from the chat button or with `/mca_vamp_compat mission cancel` while in the request's village.
- A canceled or expired request grants no reward and starts the normal village cooldown before another request is offered.

## Save Compatibility

- Existing active village requests remain accepted and have no retroactive deadline.
- No villager NBT, traits, faction data, UUIDs or existing configuration values were changed.
