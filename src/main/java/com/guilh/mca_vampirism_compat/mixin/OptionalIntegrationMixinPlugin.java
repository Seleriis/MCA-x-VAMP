package com.guilh.mca_vampirism_compat.mixin;

import net.minecraftforge.fml.ModList;
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
    private static final String EASY_VILLAGERS_CLIENT_MIXIN_PREFIX = MIXIN_PACKAGE + "client.EasyVillagers";
    private static final String MCA_EV_COMPAT_MIXIN_PREFIX = MIXIN_PACKAGE + "McaEvCompat";
    private static final String MCA_WEREWOLVES_MIXIN_PREFIX = MIXIN_PACKAGE + "McaWerewolves";
    private static final String EASY_VILLAGERS_MOD_ID = "easy_villagers";
    private static final String[] MCA_EV_COMPAT_MOD_IDS = {"mca_ev_compat", "mcaevcompat"};
    private static final String WEREWOLVES_MOD_ID = "werewolves";
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
            return optionalIntegrationAvailable(WEREWOLF_TRANSFORMABLE_RESOURCE, WEREWOLVES_MOD_ID);
        }
        if (mixinClassName.startsWith(EASY_VILLAGERS_MIXIN_PREFIX)
                || mixinClassName.startsWith(EASY_VILLAGERS_CLIENT_MIXIN_PREFIX)) {
            return optionalIntegrationAvailable(targetClassResource(targetClassName), EASY_VILLAGERS_MOD_ID);
        }
        if (mixinClassName.startsWith(MCA_EV_COMPAT_MIXIN_PREFIX)) {
            return optionalIntegrationAvailable(targetClassResource(targetClassName), MCA_EV_COMPAT_MOD_IDS);
        }
        return true;
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

    /**
     * Requires both the Forge mod-list entry and the expected target class.
     * Mixin plugins can run before {@link ModList} is ready in some tooling
     * paths, so a class-resource check is retained as a no-classloading
     * fallback only for that early bootstrap window.
     */
    private static boolean optionalIntegrationAvailable(String targetResource, String... modIds) {
        Boolean modLoaded = anyModLoaded(modIds);
        return (modLoaded == null || modLoaded) && classResourceExists(targetResource);
    }

    private static Boolean anyModLoaded(String... modIds) {
        try {
            ModList modList = ModList.get();
            if (modList == null) {
                return null;
            }
            for (String modId : modIds) {
                if (modList.isLoaded(modId)) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static String targetClassResource(String targetClassName) {
        return targetClassName.replace('.', '/') + ".class";
    }

    private static boolean classResourceExists(String classResource) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (resourceExists(contextLoader, classResource)) {
            return true;
        }
        return resourceExists(OptionalIntegrationMixinPlugin.class.getClassLoader(), classResource);
    }

    private static boolean resourceExists(ClassLoader loader, String classResource) {
        try {
            return loader != null && loader.getResource(classResource) != null;
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }
}
