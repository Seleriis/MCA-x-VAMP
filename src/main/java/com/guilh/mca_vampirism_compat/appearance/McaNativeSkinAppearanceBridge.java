package com.guilh.mca_vampirism_compat.appearance;

import forge.net.mca.entity.VillagerEntityMCA;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional bridge for MCA 7.6.28's explicit skin-dye API.
 *
 * <p>In that MCA version an explicit skin dye takes precedence over the
 * melanin and hemoglobin genes in the renderer. Clearing it while the
 * villager is a vampire lets the existing genetic pale-skin transformation
 * render normally. The original dye is restored when the trait is removed.
 * The stable MCA 7.6.16 API has none of these methods, so every operation is
 * a safe no-op there.</p>
 */
public final class McaNativeSkinAppearanceBridge {
    /** MCA's sentinel for no explicit skin dye; it enables genetic colouring. */
    public static final int NO_SKIN_DYE = 0xFF000000;

    private static final SkinMethods UNSUPPORTED = new SkinMethods(null, null, null);
    private static final Map<Class<?>, SkinMethods> METHODS = new ConcurrentHashMap<>();

    private McaNativeSkinAppearanceBridge() {
    }

    public static SkinSnapshot capture(VillagerEntityMCA villager) {
        return capture((Object) villager);
    }

    public static boolean clearForVampireAppearance(VillagerEntityMCA villager) {
        return clearForVampireAppearance((Object) villager);
    }

    public static boolean restoreOriginalSkinDye(VillagerEntityMCA villager, int skinDye) {
        return restoreOriginalSkinDye((Object) villager, skinDye);
    }

    static boolean supportsNativeSkinDye(Object villager) {
        return villager != null && methodsFor(villager).supported();
    }

    static SkinSnapshot capture(Object villager) {
        SkinMethods methods = methodsFor(villager);
        if (!methods.supported()) {
            return SkinSnapshot.unsupported();
        }
        Integer skinDye = invokeInt(methods.getSkinDye(), villager);
        return skinDye == null ? SkinSnapshot.unsupported() : new SkinSnapshot(true, skinDye);
    }

    static boolean clearForVampireAppearance(Object villager) {
        SkinMethods methods = methodsFor(villager);
        if (!methods.supported()) {
            return false;
        }
        Integer current = invokeInt(methods.getSkinDye(), villager);
        if (current == null) {
            return false;
        }
        if (current == NO_SKIN_DYE) {
            return true;
        }
        return invokeVoid(methods.clearSkinDye(), villager);
    }

    static boolean restoreOriginalSkinDye(Object villager, int skinDye) {
        SkinMethods methods = methodsFor(villager);
        if (!methods.supported()) {
            return false;
        }
        if (skinDye == NO_SKIN_DYE) {
            return invokeVoid(methods.clearSkinDye(), villager);
        }
        try {
            methods.setSkinDye().invoke(villager, skinDye);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static SkinMethods methodsFor(Object villager) {
        if (villager == null) {
            return UNSUPPORTED;
        }
        return METHODS.computeIfAbsent(villager.getClass(), McaNativeSkinAppearanceBridge::resolveMethods);
    }

    private static SkinMethods resolveMethods(Class<?> type) {
        try {
            return new SkinMethods(
                    type.getMethod("getSkinDye"),
                    type.getMethod("setSkinDye", int.class),
                    type.getMethod("clearSkinDye")
            );
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return UNSUPPORTED;
        }
    }

    private static Integer invokeInt(Method method, Object target) {
        try {
            Object value = method.invoke(target);
            return value instanceof Integer number ? number : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean invokeVoid(Method method, Object target) {
        try {
            method.invoke(target);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    public record SkinSnapshot(boolean nativeSupported, int skinDye) {
        static SkinSnapshot unsupported() {
            return new SkinSnapshot(false, NO_SKIN_DYE);
        }
    }

    private record SkinMethods(Method getSkinDye, Method setSkinDye, Method clearSkinDye) {
        boolean supported() {
            return getSkinDye != null && setSkinDye != null && clearSkinDye != null;
        }
    }
}
