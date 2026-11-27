package com.guilh.mca_vampirism_compat.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Checks the external bytecode contracts that keep faction data across EV operations. */
class McaEvLifecycleContractTest {
    private static final String ROOT = "com/guilh/mcaevcompat/";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String VILLAGER = "Lnet/minecraft/world/entity/npc/Villager;";
    private static final String SPAWN_DESCRIPTOR =
            "(Lnet/minecraft/server/level/ServerLevel;" + STACK
                    + "Lnet/minecraft/core/BlockPos;FLjava/lang/String;)Z";

    @Test
    void captureAndRestoreHooksMatchTheInstalledAddon() throws Exception {
        ClassNode logic = readOptionalClass(ROOT + "CompatLogic");
        requireStaticMethod(logic, "captureMcaVillager",
                "(" + VILLAGER + "Lnet/minecraft/world/entity/player/Player;)V");
        requireStaticMethod(logic, "saveMcaVillagerToStack", "(" + STACK + VILLAGER + "J)Z");
        requireStaticMethod(logic, "createMcaVillagerStack", "(" + VILLAGER + "J)" + STACK);
        requireStaticMethod(logic, "createStoredMcaVillagerEntity",
                "(Lnet/minecraft/world/level/Level;" + STACK + ")" + VILLAGER);
        requireStaticMethod(logic, "copyStoredMcaVillagerData",
                "(" + STACK + ")Lnet/minecraft/nbt/CompoundTag;");
        requireStaticMethod(logic, "getStoredMcaEntityTypeId", "(" + STACK + ")Ljava/lang/String;");
    }

    @Test
    void placementStillHasOneWorldJoinForTheFactionRestoreHook() throws Exception {
        MethodNode spawn = requireStaticMethod(readOptionalClass(ROOT + "CompatLogic"),
                "spawnStoredMcaVillager", SPAWN_DESCRIPTOR);
        long joins = Arrays.stream(spawn.instructions.toArray())
                .filter(MethodInsnNode.class::isInstance)
                .map(MethodInsnNode.class::cast)
                .filter(call -> call.owner.equals("net/minecraft/server/level/ServerLevel")
                        && (call.name.equals("addFreshEntity") || call.name.equals("m_7967_"))
                        && call.desc.equals("(Lnet/minecraft/world/entity/Entity;)Z"))
                .count();
        assertEquals(1, joins,
                "Placement changed: review restoration before joining the world, including dispenser placement");
    }

    @Test
    void breederStillSupportsInfertilityCancellationAndChildInheritance() throws Exception {
        ClassNode breeder = readOptionalClass(ROOT + "EasyVillagersBlockCompat");
        requireStaticMethod(breeder, "tryBreedMcaChild",
                "(Lnet/minecraft/world/level/block/entity/BlockEntity;" + STACK + STACK
                        + "Lnet/minecraft/core/NonNullList;Lnet/minecraft/core/NonNullList;)"
                        + "L" + ROOT + "EasyVillagersBlockCompat$BreederResult;");
        ClassNode result = readOptionalClass(ROOT + "EasyVillagersBlockCompat$BreederResult");
        for (String name : new String[]{"CANCEL", "SUCCESS"}) {
            assertTrue(result.fields.stream().anyMatch(field ->
                            field.name.equals(name) && (field.access & Opcodes.ACC_ENUM) != 0),
                    "The breeder result no longer exposes " + name);
        }
    }

    private ClassNode readOptionalClass(String name) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            if (Boolean.getBoolean("mca.compat.requireMcaEvApi")) {
                assertNotNull(input, "The requested MCA x EV profile must contain " + name);
            }
            assumeTrue(input != null, "MCA x EV is absent from this optional profile");
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        }
    }

    private static MethodNode requireStaticMethod(ClassNode owner, String name, String descriptor) {
        MethodNode method = owner.methods.stream()
                .filter(candidate -> candidate.name.equals(name) && candidate.desc.equals(descriptor))
                .findFirst().orElse(null);
        assertNotNull(method, owner.name + " must expose " + name + descriptor);
        assertTrue((method.access & Opcodes.ACC_STATIC) != 0, name + " must remain static");
        return method;
    }
}

