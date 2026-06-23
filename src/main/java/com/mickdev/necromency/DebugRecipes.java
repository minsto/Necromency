package com.mickdev.necromency;
import com.mickdev.necromency.Necromency;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

@EventBusSubscriber(modid = Necromency.MODID)
public class DebugRecipes {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String GREEN = "\u001B[32m";
    private static final String RED   = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private static final ResourceLocation SWING_ID = ResourceLocation.parse("necromency:swing_shaped");

    private DebugRecipes() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent e) {

        MinecraftServer server = e.getServer();
        var resId = ResourceLocation.fromNamespaceAndPath("necromency", "recipe/swing_test.json");
        boolean exists = server.getResourceManager().getResource(resId).isPresent();
        System.out.println("[NECRO DEBUG] resource exists = " + exists + " -> " + resId);
        var types = BuiltInRegistries.RECIPE_TYPE;
        var serializers = BuiltInRegistries.RECIPE_SERIALIZER;

        boolean hasType = types.containsKey(SWING_ID);
        boolean hasSer  = serializers.containsKey(SWING_ID);

        int count = 0;
        List<String> ids = List.of();

        // ✅ méthode robuste: on compare la KEY du RecipeType (ResourceLocation)
        var rm = server.getRecipeManager();
        ids = rm.getRecipes().stream()
                .filter(h -> SWING_ID.equals(BuiltInRegistries.RECIPE_TYPE.getKey(h.value().getType())))
                .map(h -> h.id().toString())
                .sorted()
                .toList();

        // Si ton type n’existe pas, on met count à 0 même si ids trouve rien (normal)
        if (hasType) count = ids.size();
        else { ids = List.of(); count = 0; }

        // ✅ Console colorée + LOGGER
        logColored("RecipeType necromency:swing_shaped exists", hasType);
        logColored("RecipeSerializer necromency:swing_shaped exists", hasSer);

        String msg = "[NECRO DEBUG] swing_shaped recipes loaded = " + count;
        LOGGER.info(msg);
        System.out.println((count > 0 ? GREEN : RED) + msg + RESET);

        if (!ids.isEmpty()) {
            ids.forEach(id -> System.out.println(GREEN + "[NECRO DEBUG] - " + id + RESET));
        }

        // ✅ Fichier report
        writeReport(server, hasType, hasSer, count, ids);
    }

    private static void logColored(String label, boolean ok) {
        String msg = "[NECRO DEBUG] " + label + " = " + ok;
        LOGGER.info(msg);
        System.out.println((ok ? GREEN : RED) + msg + RESET);
    }

    private static void writeReport(MinecraftServer server, boolean hasType, boolean hasSer, int count, List<String> ids) {
        try {
            Path logsDir = server.getServerDirectory().resolve("logs");
            Files.createDirectories(logsDir);

            Path out = logsDir.resolve("necromency-swing-debug.txt");

            StringBuilder sb = new StringBuilder();
            sb.append("=== Necromency Swing Recipe Debug ===\n");
            sb.append("Time: ").append(LocalDateTime.now()).append("\n\n");

            sb.append("Registry check:\n");
            sb.append("- RecipeType necromency:swing_shaped: ").append(hasType).append("\n");
            sb.append("- RecipeSerializer necromency:swing_shaped: ").append(hasSer).append("\n\n");

            sb.append("Loaded recipes count: ").append(count).append("\n");
            if (!ids.isEmpty()) {
                sb.append("Loaded recipe IDs:\n");
                for (String id : ids) sb.append(" - ").append(id).append("\n");
            }
            sb.append("\n");

            sb.append("If FALSE / 0 recipes:\n");
            sb.append("1) JSON path: resources/data/necromency/recipes/<name>.json\n");
            sb.append("2) JSON key must be Ingredient objects:\n");
            sb.append("   \"a\": {\"item\":\"minecraft:iron_ingot\"} (PAS \"a\": \"minecraft:iron_ingot\")\n");
            sb.append("3) DeferredRegister must be registered on MOD event bus:\n");
            sb.append("   SwingRecipeSerializer.SERIALIZERS.register(modEventBus);\n");
            sb.append("   SwingRecipeType.TYPES.register(modEventBus);\n");
            sb.append("4) Check logs/latest.log for \"Failed to parse recipe\"\n");

            Files.writeString(out, sb.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            LOGGER.info("[NECRO DEBUG] Wrote report: {}", out.toAbsolutePath());
        } catch (Exception ex) {
            LOGGER.error("[NECRO DEBUG] Failed to write report", ex);
        }
    }
}