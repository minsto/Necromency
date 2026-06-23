package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.Client.Util.MobTextureResolver;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Texture 128×64 : zombie (0,0) + poulet vanilla (64,0), pour un seul bind avec des UV décalées sur le mesh atlas.
 */
public final class MinionCompositeAtlas {

    public static final ResourceLocation LOCATION =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entity/minion_zombie_chicken_atlas");

    private static final Logger LOGGER = LoggerFactory.getLogger("necromency/MinionCompositeAtlas");

    private static final ResourceLocation ZOMBIE_TEX =
            ResourceLocation.parse("minecraft:textures/entity/zombie/zombie.png");

    private static DynamicTexture boundTexture;

    private MinionCompositeAtlas() {}

    public static boolean isReady() {
        return boundTexture != null;
    }

    public static void ensureBuilt() {
        if (boundTexture != null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) {
            return;
        }
        rebuild(mc.getResourceManager());
    }

    public static void rebuild(ResourceManager resources) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) {
            return;
        }
        if (boundTexture != null) {
            boundTexture.close();
            boundTexture = null;
        }
        try (NativeImage zombie = loadImage(resources, ZOMBIE_TEX);
                NativeImage chicken = loadChickenImage(resources)) {
            NativeImage atlas = new NativeImage(NativeImage.Format.RGBA, 128, 64, true);
            atlas.fillRect(0, 0, 128, 64, 0);
            // copyRect lit depuis this et écrit dans le 1er argument (API 1.21.10)
            int zw = Math.min(64, zombie.getWidth());
            int zh = Math.min(64, zombie.getHeight());
            int cw = Math.min(64, chicken.getWidth());
            int ch = Math.min(32, chicken.getHeight());
            zombie.copyRect(atlas, 0, 0, 0, 0, zw, zh, false, false);
            chicken.copyRect(atlas, 0, 0, 64, 0, cw, ch, false, false);
            boundTexture = new DynamicTexture(() -> Necromency.MODID + "/minion_zombie_chicken_atlas", atlas);
            boundTexture.upload();
            mc.getTextureManager().register(LOCATION, boundTexture);
        } catch (Exception e) {
            LOGGER.warn("Impossible de construire l'atlas minion zombie+poulet", e);
        }
    }

    private static NativeImage loadImage(ResourceManager resources, ResourceLocation path) throws IOException {
        var opt = resources.getResource(path);
        if (opt.isEmpty()) {
            throw new IOException("Missing resource: " + path);
        }
        try (InputStream in = opt.get().open()) {
            return NativeImage.read(in);
        }
    }

    private static NativeImage loadChickenImage(ResourceManager resources) throws IOException {
        ResourceLocation loc = MobTextureResolver.firstExistingChickenEntityTexture(resources);
        if (loc == null) {
            throw new IOException("No vanilla chicken entity texture in known paths (temperate/warm/cold/legacy)");
        }
        return loadImage(resources, loc);
    }
}
