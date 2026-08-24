# MCA Reborn x Vampirism Compat 2.1.6

## Required startup hotfix

Version 2.1.5 can crash during Minecraft startup because its special-trade
integration used a Mixin accessor for an inherited vanilla method. Replace it
with this 2.1.6 JAR.

The special Vampirism offer feature is unchanged: professional MCA Vampires
receive Vampirism's native one-in-three chance to add a heart, emerald or
blood-bottle offer. The implementation now uses the public item-listing API,
so it does not add any new Mixin targets.
