package com.mickdev.necromency.registry.Swing.Recipes;

import com.mickdev.necromency.Necromency;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache côté client des recettes de la machine à coudre reçues du serveur.
 *
 * <p>Les recettes sont poussées par {@link SwingRecipeServerSync} puis reçues ici via
 * {@link RecipesReceivedEvent}. Le plugin JEI lit ensuite {@link #get()}.</p>
 *
 * <p>La priorité {@code HIGHEST} garantit que la liste est remplie avant que JEI
 * (qui écoute le même évènement) ne déclenche son rechargement.</p>
 */
@EventBusSubscriber(modid = Necromency.MODID, value = Dist.CLIENT)
public final class SwingClientRecipes {

    private static volatile List<SwingShapedRecipe> recipes = List.of();

    private SwingClientRecipes() {}

    public static List<SwingShapedRecipe> get() {
        return recipes;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        var holders = event.getRecipeMap().byType(SwingRecipeType.SWING_SHAPED.get());
        List<SwingShapedRecipe> list = new ArrayList<>(holders.size());
        for (RecipeHolder<SwingShapedRecipe> holder : holders) {
            list.add(holder.value());
        }
        recipes = List.copyOf(list);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = List.of();
    }
}
