package com.guilh.mca_vampirism_compat.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
    private static final String WEREWOLF_TRANSFORMABLE_RESOURCE =
            "de/teamlapen/werewolves/api/entities/werewolf/WerewolfTransformable.class";

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
                || mixinClassName.startsWith(MCA_EV_COMPAT_MIXIN_PREFIX);
    }

    private static boolean targetClassResourceExists(String targetClassName) {
        return classResourceExists(targetClassName.replace('.', '/') + ".class");
    }

    private static boolean classResourceExists(String classResource) {
        ClassLoader loader = OptionalIntegrationMixinPlugin.class.getClassLoader();
        return loader != null && loader.getResource(classResource) != null;
    }
}
