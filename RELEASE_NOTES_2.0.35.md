# MCA Reborn x Vampirism Compat 2.0.35

## Legacy Snapshot Cleanup

- Cleans invalid empty item entries left by older saves before stored MCA villagers are restored or rendered inside Easy Villagers blocks.
- Prevents repeated client log warnings such as `Tried to load invalid item: No key id in MapLike[{}]`.
- Valid equipment and inventories are untouched. An original empty hand remains correctly remembered as empty.

## Compatibility

- The repair applies only to malformed empty item compounds in known item fields.
- No traits, faction state, villager identity, UUID, configuration key or owner snapshot format was changed.
