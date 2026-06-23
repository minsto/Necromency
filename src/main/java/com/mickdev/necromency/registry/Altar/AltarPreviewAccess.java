package com.mickdev.necromency.registry.Altar;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Pont commun → client pour l'aperçu minion sur l'autel (sans {@code @OnlyIn} ni classe client sur le serveur).
 */
public final class AltarPreviewAccess {

    @Nullable
    private static Consumer<BlockPos> onPreviewRemoved;

    private AltarPreviewAccess() {}

    /** Appelé depuis {@link com.mickdev.necromency.NecromencyClient} au démarrage client. */
    public static void bindPreviewRemoved(Consumer<BlockPos> handler) {
        onPreviewRemoved = handler;
    }

    public static void clearPreview(BlockPos pos) {
        if (onPreviewRemoved != null) {
            onPreviewRemoved.accept(pos);
        }
    }
}
