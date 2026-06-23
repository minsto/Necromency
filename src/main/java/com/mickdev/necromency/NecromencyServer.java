package com.mickdev.necromency;

import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

@Mod(value = Necromency.MODID, dist = Dist.DEDICATED_SERVER)
public final class NecromencyServer {

    public static int MAX_MINION_SPAWN = -1;

    @SubscribeEvent
    static void onServerStarting(ServerStartingEvent event) {
        Necromency.LOGGER.info("Necromency server starting");

        // Commande /minion enregistrée via {@link com.mickdev.necromency.registry.MinionCommand}
    }

    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        int fromProps = readIntFromServerProperties(server, "max_minion_spawn", Integer.MIN_VALUE);
        if (fromProps != Integer.MIN_VALUE) {
            MAX_MINION_SPAWN = fromProps;
        } else {
            MAX_MINION_SPAWN = NecromencyConfig.MAX_MINION_SPAWN.get();
        }
        Necromency.LOGGER.info("max_minion_spawn = {}", MAX_MINION_SPAWN);
    }

    private static int readIntFromServerProperties(MinecraftServer server, String key, int def) {
        try {
            // dossier racine serveur
            File propsFile = new File("server.properties");
            if (!propsFile.exists()) return def;

            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream(propsFile)) {
                p.load(in);
            }
            String v = p.getProperty(key);
            if (v == null) return def;
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            Necromency.LOGGER.warn("Failed reading server.properties key {}", key, e);
            return def;
        }
    }
}

