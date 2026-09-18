package com.guilh.mca_vampirism_compat.easyvillagers;
import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaFactionResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.function.BiConsumer;
/** Optional API registration. Old EV releases keep their existing bridge. */
public final class McaEvTooltipIntegration {
    private McaEvTooltipIntegration() {}
    public static void register() {
        try {
            Class<?> api=Class.forName("com.guilh.mcaevcompat.api.StoredVillagerInfoApi");
            BiConsumer<ItemStack,List<Component>> contributor=McaEvTooltipIntegration::append;
            api.getMethod("register",String.class,BiConsumer.class).invoke(null,McaVampirismCompat.MOD_ID,contributor);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // EV is optional and its supported legacy builds predate this API.
        } catch (ReflectiveOperationException exception) {
            McaVampirismCompat.LOGGER.warn("Could not register optional EV tooltip information",exception);
        }
    }
    private static void append(ItemStack stack,List<Component> lines) {
        var info=StoredMcaFactionResolver.resolveFactionSnapshot(stack);
        if (!info.isMca()) return;
        String faction=info.isVampire()?"vampire":info.isWerewolf()?"werewolf":info.isHunter()?"hunter":"human";
        lines.add(Component.translatable("mca_vamp_compat.stored.faction."+faction));
        McaEvStatePayload.readState(stack).ifPresent(state -> {
            if (state.getBoolean("CuringVampire")) lines.add(Component.translatable("mca_vamp_compat.stored.curing_vampire"));
            if (state.getBoolean("CuringWerewolf")) lines.add(Component.translatable("mca_vamp_compat.stored.curing_werewolf"));
        });
    }
}
