package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaVillagerInfo;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

/** Faction validation shared by natural job-site acquisition and EV Traders. */
public final class McaSpecialistProfessionService {
    public static final ResourceLocation VAMPIRE_EXPERT =
            new ResourceLocation("vampirism", "vampire_expert");
    public static final ResourceLocation HUNTER_EXPERT =
            new ResourceLocation("vampirism", "hunter_expert");
    public static final ResourceLocation WEREWOLF_EXPERT =
            new ResourceLocation("werewolves", "werewolf_expert");

    private static final ResourceLocation VAMPIRE_ALTAR =
            new ResourceLocation("vampirism", "altar_inspiration");
    private static final ResourceLocation HUNTER_TABLE =
            new ResourceLocation("vampirism", "hunter_table");
    private static final ResourceLocation WEREWOLF_ALTAR =
            new ResourceLocation("werewolves", "stone_altar");

    private McaSpecialistProfessionService() {
    }

    public static boolean canUseProfession(VillagerEntityMCA villager, VillagerProfession profession) {
        return specializationFor(profession).map(specialization -> specialization.matches(villager)).orElse(true);
    }

    public static boolean shouldRejectMcaProfessionChange(VillagerEntityMCA villager, VillagerProfession profession) {
        Optional<Specialization> specialization = specializationFor(profession);
        if (villager == null || specialization.isEmpty() || !isAlternativeWorldWorkstation(villager, specialization.get())
                || specialization.get().matches(villager)) {
            return false;
        }
        releaseRejectedWorkstation(villager);
        McaVampirismCompat.debugLog(
                "[SPECIALIST-PROFESSION] rejected world assignment villager={} profession={} reason=WRONG_FACTION",
                villager.getUUID(), professionId(profession)
        );
        return true;
    }

    /**
     * Totems remain Vampirism's and Werewolves' native profession route. This
     * guard applies only when MCA acquired one of the new alternative blocks.
     */
    private static boolean isAlternativeWorldWorkstation(
            VillagerEntityMCA villager,
            Specialization specialization
    ) {
        return villager.getBrain().getMemory(MemoryModuleType.JOB_SITE)
                .filter(jobSite -> jobSite.dimension().equals(villager.level().dimension()))
                .map(jobSite -> specializationFor(villager.level().getBlockState(jobSite.pos()).getBlock())
                        .filter(specialization::equals)
                        .isPresent())
                .orElse(false);
    }

    /**
     * A villager rejected for faction reasons must not retain a lease on the
     * new workstation, otherwise it could prevent an eligible villager from
     * acquiring the profession.
     */
    private static void releaseRejectedWorkstation(VillagerEntityMCA villager) {
        villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).ifPresent(jobSite -> {
            if (villager.level() instanceof ServerLevel level
                    && jobSite.dimension().equals(level.dimension())) {
                level.getPoiManager().release(jobSite.pos());
            }
        });
        villager.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
    }

    /**
     * Returns true only when a direct Trader insertion must be refused. A
     * non-MCA stack is intentionally left to Easy Villagers' native behavior.
     */
    public static boolean rejectIncompatibleTraderWorkstation(
            StoredMcaVillagerInfo info,
            Block workstation,
            Player player
    ) {
        Optional<Specialization> specialization = specializationFor(workstation);
        if (specialization.isEmpty() || info == null || !info.resolved() || !info.isMca()) {
            return false;
        }
        if (specialization.get().matches(info)) {
            return false;
        }
        if (player != null && !player.level().isClientSide) {
            player.displayClientMessage(Component.translatable(specialization.get().requiredFactionMessage()), true);
        }
        McaVampirismCompat.debugLog(
                "[SPECIALIST-PROFESSION] rejected Trader workstation player={} block={} expectedFaction={} source={} vampire={} hunter={} werewolf={}",
                player == null ? null : player.getName().getString(),
                workstationId(workstation),
                specialization.get().name(),
                info.stackSource(),
                info.isVampire(), info.isHunter(), info.isWerewolf()
        );
        return true;
    }

    public static Optional<VillagerProfession> professionForWorkstation(Block workstation) {
        return specializationFor(workstation)
                .map(Specialization::professionId)
                .map(BuiltInRegistries.VILLAGER_PROFESSION::get);
    }

    public static Optional<Specialization> specializationFor(VillagerProfession profession) {
        if (profession == null) {
            return Optional.empty();
        }
        ResourceLocation id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
        return Specialization.byProfessionId(id);
    }

    public static Optional<Specialization> specializationFor(Block workstation) {
        return Specialization.byWorkstationId(workstationId(workstation));
    }

    /** Returns true for one of the three optional faction-specialist stations. */
    public static boolean isSpecialistWorkstation(Block workstation) {
        return specializationFor(workstation).isPresent();
    }

    /** Called after Easy Villagers has authoritatively installed a Trader workstation. */
    public static void awardTraderSpecialistIfApplied(StoredMcaVillagerInfo info, Block workstation, Player player) {
        if (info == null || !info.resolved() || !info.isMca() || player == null || player.level().isClientSide
                || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        specializationFor(workstation).ifPresent(specialization -> {
            if (specialization.matches(info)
                    && info.villagerEntity() != null
                    && specialization.professionId().equals(professionId(info.villagerEntity().getVillagerData().getProfession()))) {
                McaAdvancementService.award(serverPlayer, switch (specialization) {
                    case VAMPIRE -> McaCompatAdvancements.VAMPIRE_SPECIALIST;
                    case HUNTER -> McaCompatAdvancements.HUNTER_SPECIALIST;
                    case WEREWOLF -> McaCompatAdvancements.WEREWOLF_SPECIALIST;
                });
            }
        });
    }

    private static ResourceLocation workstationId(Block workstation) {
        return workstation == null ? null : BuiltInRegistries.BLOCK.getKey(workstation);
    }

    private static ResourceLocation professionId(VillagerProfession profession) {
        return BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
    }

    public enum Specialization {
        VAMPIRE(VAMPIRE_EXPERT, VAMPIRE_ALTAR, "mca_vamp_compat.specialist.requires_vampire"),
        HUNTER(HUNTER_EXPERT, HUNTER_TABLE, "mca_vamp_compat.specialist.requires_hunter"),
        WEREWOLF(WEREWOLF_EXPERT, WEREWOLF_ALTAR, "mca_vamp_compat.specialist.requires_werewolf");

        private final ResourceLocation professionId;
        private final ResourceLocation workstationId;
        private final String requiredFactionMessage;

        Specialization(ResourceLocation professionId, ResourceLocation workstationId, String requiredFactionMessage) {
            this.professionId = professionId;
            this.workstationId = workstationId;
            this.requiredFactionMessage = requiredFactionMessage;
        }

        private static Optional<Specialization> byProfessionId(ResourceLocation professionId) {
            for (Specialization specialization : values()) {
                if (specialization.professionId.equals(professionId)) {
                    return Optional.of(specialization);
                }
            }
            return Optional.empty();
        }

        private static Optional<Specialization> byWorkstationId(ResourceLocation workstationId) {
            for (Specialization specialization : values()) {
                if (specialization.workstationId.equals(workstationId)) {
                    return Optional.of(specialization);
                }
            }
            return Optional.empty();
        }

        private boolean matches(VillagerEntityMCA villager) {
            return switch (this) {
                case VAMPIRE -> McaVampireStateService.isVampire(villager);
                case HUNTER -> McaHunterAlignmentService.isMcaHunterVillager(villager);
                case WEREWOLF -> McaWerewolfStateService.isWerewolf(villager);
            };
        }

        private boolean matches(StoredMcaVillagerInfo villager) {
            return switch (this) {
                case VAMPIRE -> villager.isVampire();
                case HUNTER -> villager.isHunter();
                case WEREWOLF -> villager.isWerewolf();
            };
        }

        public ResourceLocation professionId() {
            return professionId;
        }

        public String requiredFactionMessage() {
            return requiredFactionMessage;
        }

    }
}
