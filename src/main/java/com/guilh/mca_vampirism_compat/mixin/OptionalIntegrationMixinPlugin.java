package com.guilh.mca_vampirism_compat.mixin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Prevents Mixin from resolving targets owned by optional integrations that
 * are not installed. The affected mixins remain {@code @Pseudo} as a second
 * layer of compatibility protection.
 */
public final class OptionalIntegrationMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "com.guilh.mca_vampirism_compat.mixin.";
    private static final String EASY_VILLAGERS_MIXIN_PREFIX = MIXIN_PACKAGE + "EasyVillagers";
    private static final String MCA_EV_COMPAT_MIXIN_PREFIX = MIXIN_PACKAGE + "McaEvCompat";
    private static final String MCA_WEREWOLVES_MIXIN_PREFIX = MIXIN_PACKAGE + "McaWerewolves";
    private static final String BLOODLINES_MIXIN_PREFIX = MIXIN_PACKAGE + "Bloodlines";
    private static final String BLOODLINES_V2_MIXIN = MIXIN_PACKAGE + "BloodlinesBloodknightMixin";
    private static final String BLOODLINES_V3_MIXIN = MIXIN_PACKAGE + "BloodlinesBloodknightV3Mixin";
    private static final String WEREWOLF_TRANSFORMABLE_RESOURCE =
            "de/teamlapen/werewolves/api/entities/werewolf/WerewolfTransformable.class";
    private static final String BLOODLINES_EVENT_HANDLER_RESOURCE =
            "com/thedrofdoctoring/bloodlines/capabilities/bloodlines/BloodlineEventHandler.class";
    private static final String BLOODLINES_BLOODKNIGHT_RESOURCE =
            "com/thedrofdoctoring/bloodlines/capabilities/bloodlines/vamp/BloodlineBloodknight.class";
    private static final String BLOODLINES_V2_DRINK_HANDLER = "lambda$onBloodDrink$2";
    private static final String BLOODLINES_V2_DRINK_HANDLER_DESCRIPTOR =
            "(Lde/teamlapen/vampirism/api/event/BloodDrinkEvent$PlayerDrinkBloodEvent;"
                    + "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;"
                    + "Lcom/thedrofdoctoring/bloodlines/capabilities/bloodlines/BloodlineManager;)V";
    private static final String BLOODLINES_V3_DRINK_HANDLER = "onEntityBloodDrink";
    private static final String BLOODLINES_V3_DRINK_HANDLER_DESCRIPTOR =
            "(Lnet/minecraft/world/entity/LivingEntity;"
                    + "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;I"
                    + "Lde/teamlapen/vampirism/api/event/BloodDrinkEvent$PlayerDrinkBloodEvent;)V";

    @Override
    public void onLoad(String mixinPackage) {
        // No initialization is needed before the optional target check.
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (BLOODLINES_V2_MIXIN.equals(mixinClassName)) {
            return classHasMethod(
                    BLOODLINES_EVENT_HANDLER_RESOURCE,
                    BLOODLINES_V2_DRINK_HANDLER,
                    BLOODLINES_V2_DRINK_HANDLER_DESCRIPTOR
            );
        }
        if (BLOODLINES_V3_MIXIN.equals(mixinClassName)) {
            return classHasMethod(
                    BLOODLINES_BLOODKNIGHT_RESOURCE,
                    BLOODLINES_V3_DRINK_HANDLER,
                    BLOODLINES_V3_DRINK_HANDLER_DESCRIPTOR
            );
        }
        if (mixinClassName.startsWith(MCA_WEREWOLVES_MIXIN_PREFIX)) {
            return classResourceExists(WEREWOLF_TRANSFORMABLE_RESOURCE);
        }
        if (!isOptionalIntegrationMixin(mixinClassName)) {
            return true;
        }
        return targetClassResourceExists(targetClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // Target selection is fully decided by shouldApplyMixin.
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No bytecode changes are needed before application.
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No bytecode changes are needed after application.
    }

    private static boolean isOptionalIntegrationMixin(String mixinClassName) {
        return mixinClassName.startsWith(EASY_VILLAGERS_MIXIN_PREFIX)
                || mixinClassName.startsWith(MCA_EV_COMPAT_MIXIN_PREFIX)
                || mixinClassName.startsWith(BLOODLINES_MIXIN_PREFIX);
    }

    private static boolean targetClassResourceExists(String targetClassName) {
        return classResourceExists(targetClassName.replace('.', '/') + ".class");
    }

    private static boolean classResourceExists(String classResource) {
        ClassLoader loader = OptionalIntegrationMixinPlugin.class.getClassLoader();
        return loader != null && loader.getResource(classResource) != null;
    }

    /**
     * Bloodlines moved the Bloodknight drink logic from an event-handler
     * lambda in 2.x to the bloodline implementation in 3.x. Selecting the
     * mixin from the bytecode shape means an unsupported future layout safely
     * skips this optional enhancement instead of preventing Minecraft from
     * starting.
     */
    private static boolean classHasMethod(String classResource, String methodName, String descriptor) {
        ClassLoader loader = OptionalIntegrationMixinPlugin.class.getClassLoader();
        if (loader == null) {
            return false;
        }
        try (InputStream classBytes = loader.getResourceAsStream(classResource)) {
            if (classBytes == null) {
                return false;
            }
            ClassNode classNode = new ClassNode();
            new ClassReader(classBytes).accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            for (MethodNode method : classNode.methods) {
                if (methodName.equals(method.name) && descriptor.equals(method.desc)) {
                    return true;
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // Optional integrations must not make a missing or changed addon fatal.
        }
        return false;
    }
}
