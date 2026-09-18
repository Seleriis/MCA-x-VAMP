package com.guilh.mca_vampirism_compat.appearance;

import forge.net.mca.entity.VillagerEntityMCA;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional bridge for MCA 7.6.28's per-eye dye API.
 *
 * <p>The stable MCA 7.6.16 API used to compile this mod has no such methods.
 * Keeping the calls reflective lets the same jar use MCA's native eye renderer
 * when it is available, while retaining the legacy overlay on 7.6.16.</p>
 */
public final class McaNativeEyeAppearanceBridge {
    /** Opaque red, matching the former Vampirism eye overlay. */
    public static final int VAMPIRE_EYE_DYE = 0xFFFF0000;

    private static final EyeMethods UNSUPPORTED = new EyeMethods(null, null, null, null, null, null);
    private static final Map<Class<?>, EyeMethods> METHODS = new ConcurrentHashMap<>();

    private McaNativeEyeAppearanceBridge() {
    }

    public static boolean supportsNativeEyeDyes(VillagerEntityMCA villager) {
        return supportsNativeEyeDyes((Object) villager);
    }

    public static EyeSnapshot capture(VillagerEntityMCA villager) {
        return capture((Object) villager);
    }

    public static boolean applyVampireEyes(VillagerEntityMCA villager) {
        return applyVampireEyes((Object) villager);
    }

    public static boolean restoreOriginalEyes(VillagerEntityMCA villager, int eyeDye, int eyeLeftDye) {
        return restoreOriginalEyes((Object) villager, eyeDye, eyeLeftDye);
    }

    static boolean supportsNativeEyeDyes(Object villager) {
        return villager != null && methodsFor(villager).supported();
    }

    static EyeSnapshot capture(Object villager) {
        EyeMethods methods = methodsFor(villager);
        if (!methods.supported()) {
            return EyeSnapshot.unsupported();
        }
        Integer eyeDye = invokeInt(methods.getEyeDye(), villager);
        Integer eyeLeftDye = invokeInt(methods.getEyeLeftDye(), villager);
        if (eyeDye == null || eyeLeftDye == null) {
            return EyeSnapshot.unsupported();
        }
        return new EyeSnapshot(true, eyeDye, eyeLeftDye);
    }

    static boolean applyVampireEyes(Object villager) {
        EyeMethods methods = methodsFor(villager);
        return methods.supported()
                && setOrClear(methods.setEyeDye(), methods.clearEyeDye(), villager, VAMPIRE_EYE_DYE)
                && setOrClear(methods.setEyeLeftDye(), methods.clearEyeLeftDye(), villager, VAMPIRE_EYE_DYE);
    }

    static boolean restoreOriginalEyes(Object villager, int eyeDye, int eyeLeftDye) {
        EyeMethods methods = methodsFor(villager);
        return methods.supported()
                && setOrClear(methods.setEyeDye(), methods.clearEyeDye(), villager, eyeDye)
                && setOrClear(methods.setEyeLeftDye(), methods.clearEyeLeftDye(), villager, eyeLeftDye);
    }

    private static EyeMethods methodsFor(Object villager) {
        if (villager == null) {
            return UNSUPPORTED;
        }
        return METHODS.computeIfAbsent(villager.getClass(), McaNativeEyeAppearanceBridge::resolveMethods);
    }

    private static EyeMethods resolveMethods(Class<?> type) {
        try {
            return new EyeMethods(
                    type.getMethod("getEyeDye"),
                    type.getMethod("setEyeDye", int.class),
                    type.getMethod("clearEyeDye"),
                    type.getMethod("getEyeLeftDye"),
                    type.getMethod("setEyeLeftDye", int.class),
                    type.getMethod("clearEyeLeftDye")
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

    private static boolean setOrClear(Method setter, Method clearer, Object target, int dye) {
        try {
            // MCA uses -1 as its absent-dye sentinel. ARGB colours with an
            // opaque alpha channel are also negative signed ints, including
            // the vampire red 0xFFFF0000, so they must go through the setter.
            if (dye == -1 && clearer != null) {
                clearer.invoke(target);
            } else {
                setter.invoke(target, dye);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    public record EyeSnapshot(boolean nativeSupported, int eyeDye, int eyeLeftDye) {
        static EyeSnapshot unsupported() {
            return new EyeSnapshot(false, -1, -1);
        }
    }

    private record EyeMethods(Method getEyeDye, Method setEyeDye, Method clearEyeDye,
                              Method getEyeLeftDye, Method setEyeLeftDye, Method clearEyeLeftDye) {
        boolean supported() {
            return getEyeDye != null && setEyeDye != null && getEyeLeftDye != null && setEyeLeftDye != null;
        }
    }
}
