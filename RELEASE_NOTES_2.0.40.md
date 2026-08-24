# MCA Reborn x Vampirism Compat 2.0.40

## Legacy Village Favor Recovery

Earlier versions created some unanswered village favors automatically and tied them to a resident without showing the player who that resident was. After moving favors to MCA conversations, those old records could hide the new conversation choice.

This hotfix recovers only those legacy unanswered requests. When the player asks a compatible resident **Could you use a hand?**, that resident adopts the existing request. Its requested item and quantity are preserved, and its deadline starts only if the player chooses to help.

Requests created by the conversation system remain tied to their original resident. No villager, trait, faction, inventory or world payload is changed.
