package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, Necromency.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTCRAWLER_HOWL =
            register("nightcrawler.howl");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTCRAWLER_SCREAM =
            register("nightcrawler.scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPAWN =
            register("spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> TEAR =
            register("tear");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Necromency.MODID, path);
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
