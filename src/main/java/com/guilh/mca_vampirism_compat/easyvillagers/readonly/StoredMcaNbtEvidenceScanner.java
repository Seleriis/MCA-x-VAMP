package com.guilh.mca_vampirism_compat.easyvillagers.readonly;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Bounded, read-only NBT traversal shared by the stored-villager evidence rules.
 * It owns traversal only: callers retain faction-specific matching and diagnostics.
 */
final class StoredMcaNbtEvidenceScanner {
    private StoredMcaNbtEvidenceScanner() {
    }

    static boolean contains(
            Tag tag,
            String path,
            int depth,
            int maxDepth,
            List<String> candidates,
            BiConsumer<String, Tag> candidateConsumer,
            EvidenceRule rule
    ) {
        if (tag == null || depth > maxDepth) {
            return false;
        }
        ScanResult result = rule.inspect(tag, path);
        if (result.diagnostic()) {
            candidateConsumer.accept(path, tag);
        }
        if (result.matched()) {
            return true;
        }
        if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
                if (contains(
                        compoundTag.get(key),
                        path + "." + key,
                        depth + 1,
                        maxDepth,
                        candidates,
                        candidateConsumer,
                        rule
                )) {
                    return true;
                }
            }
        } else if (tag instanceof ListTag listTag) {
            for (int i = 0; i < listTag.size(); i++) {
                if (contains(
                        listTag.get(i),
                        path + "[" + i + "]",
                        depth + 1,
                        maxDepth,
                        candidates,
                        candidateConsumer,
                        rule
                )) {
                    return true;
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    interface EvidenceRule {
        ScanResult inspect(Tag tag, String path);
    }

    record ScanResult(boolean matched, boolean diagnostic) {
        static ScanResult none() {
            return new ScanResult(false, false);
        }

        static ScanResult match() {
            return new ScanResult(true, true);
        }

        static ScanResult diagnosticOnly() {
            return new ScanResult(false, true);
        }
    }
}
