package com.mickdev.necromency.registry.Swing.Recipes;

import com.mickdev.necromency.Necromency;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * Depuis Minecraft 1.21.2, le serveur n'envoie plus toutes les recettes au client.
 * NeoForge ne synchronise que les types explicitement demandés via
 * {@link OnDatapackSyncEvent#sendRecipes}. On demande donc l'envoi des recettes
 * de la machine à coudre pour qu'elles soient disponibles côté client (JEI).
 *
 * <p>Fonctionne aussi bien en solo (serveur intégré) que sur serveur dédié.</p>
 */
@EventBusSubscriber(modid = Necromency.MODID)
public final class SwingRecipeServerSync {

    private SwingRecipeServerSync() {}

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(SwingRecipeType.SWING_SHAPED.get());
    }
}
