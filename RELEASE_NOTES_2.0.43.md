# MCA Reborn x Vampirism Compat 2.0.43

## MCA identity protection from native Werewolves

This hotfix prevents the native Werewolves villager transformation from replacing an MCA villager with a beast. Native full-moon, rage and time-limited transformations now leave the original MCA villager in place, retaining its UUID, name, profession, offers, social data, genetics, appearance and Easy Villagers capture data.

Old MCA villagers carrying only the native `werewolves.werewolf` marker are normalized on load. The marker is discarded rather than guessed as a real compat conversion, so a human MCA villager stays human.

MCA werewolves created by this addon through a valid bite, inherited state or the MCA Editor continue to work exactly as before, including their cure. Vanilla Werewolves entities and the native Werewolves village-population flow are not changed.

This release also restores quoted display-name support for the existing OP `inspect` and `resync` commands.
