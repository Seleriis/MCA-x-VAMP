package com.guilh.mca_vampirism_compat.service;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import net.conczin.mca.Config;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.Set;
import java.util.UUID;

/** Reads MCA's current family tree and memories; never creates a relative or a memory. */
public final class McaRelationshipCombatPolicy {
    private McaRelationshipCombatPolicy() {}
    public record FamilyLink(UUID id, UUID partner, boolean married, UUID father, UUID mother, Set<UUID> children) {}
    static boolean related(FamilyLink a, FamilyLink b) {
        if (a==null || b==null || a.id().equals(b.id())) return false;
        return direct(a,b.id()) || direct(b,a.id()) || shared(a.father(),b.father()) || shared(a.father(),b.mother())
                || shared(a.mother(),b.father()) || shared(a.mother(),b.mother());
    }
    private static boolean shared(UUID a, UUID b) { return a!=null && a.equals(b) && !a.equals(new UUID(0L,0L)); }
    private static boolean direct(FamilyLink node, UUID target) {
        return node.married() && shared(node.partner(),target) || shared(node.father(),target)
                || shared(node.mother(),target) || node.children().contains(target);
    }
    private static FamilyLink link(FamilyTreeNode node) {
        return new FamilyLink(node.id(),node.partner(),node.getRelationshipState().isMarried(),node.father(),node.mother(),node.children());
    }
    public static boolean protectedRelationship(VillagerEntityMCA villager, LivingEntity target) {
        if (!McaVampirismCompatConfig.respectMcaRelationshipsInCombat() || target==null || target==villager
                || !(villager.level() instanceof ServerLevel level)
                || !(target instanceof VillagerEntityMCA || target instanceof Player)) return false;
        FamilyTree tree=FamilyTree.get(level);
        FamilyLink a=tree.getOrEmpty(villager.getUUID()).map(McaRelationshipCombatPolicy::link).orElse(null);
        FamilyLink b=tree.getOrEmpty(target.getUUID()).map(McaRelationshipCombatPolicy::link).orElse(null);
        if (related(a,b) || a!=null && direct(a,target.getUUID()) || b!=null && direct(b,villager.getUUID())) return true;
        if (target instanceof Player) {
            var memory=villager.getVillagerBrain().getMemories().get(target.getUUID());
            return memory!=null && memory.getHearts()>=Config.getInstance().heartsToBeConsideredAsFriend;
        }
        return false;
    }
    public static boolean blocksAutomaticAttack(LivingEntity source, LivingEntity target) {
        return source instanceof VillagerEntityMCA villager && protectedRelationship(villager,target) && !retaliating(villager,target);
    }
    public static void confirmedAggression(VillagerEntityMCA villager, LivingEntity attacker) {
        if (!protectedRelationship(villager,attacker) || villager.getAgeState()!=AgeState.ADULT
                || McaVampireCureService.isCuring(villager) || !McaCommandRespectService.canRetaliateWhileCommanded(villager,attacker)) return;
        long duration=McaVampireStateService.isVampire(villager) ? McaVampirismCompatConfig.retaliationMemoryTicks()
                : McaHunterAlignmentService.isMcaHunterVillager(villager) ? McaVampirismCompatConfig.hunterVillagerRetaliationMemoryTicks()
                : McaVampirismCompatConfig.werewolfVillagerTargetMemoryTicks();
        long adjusted=McaCommandRespectService.retaliationMemoryTicksFor(villager,duration);
        ModCapabilities.get(villager).ifPresent(state->state.markRetaliation(attacker.getUUID(),villager.level().getGameTime(),adjusted));
    }
    private static boolean retaliating(VillagerEntityMCA villager, LivingEntity target) {
        return target!=null && ModCapabilities.get(villager).map(state -> state.isRetaliatingAgainst(target.getUUID(),villager.level().getGameTime())).orElse(false);
    }
    public static boolean protectedRetaliation(VillagerEntityMCA villager, LivingEntity target) {
        return protectedRelationship(villager,target) && retaliating(villager,target)
                && villager.getAgeState()==AgeState.ADULT && !McaVampireCureService.isCuring(villager)
                && (!(target instanceof VillagerEntityMCA relative) || relative.getAgeState()==AgeState.ADULT && !McaVampireCureService.isCuring(relative));
    }
}
