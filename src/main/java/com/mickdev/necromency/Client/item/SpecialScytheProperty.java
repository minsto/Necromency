package com.mickdev.necromency.Client.item;

import com.mickdev.necromency.NecromencyConfig;
import com.mickdev.necromency.registry.SpecialFolk;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Propriété client pour {@code range_dispatch} — faux spéciale selon le joueur. */
public record SpecialScytheProperty() implements RangeSelectItemModelProperty {

    public static final MapCodec<SpecialScytheProperty> MAP_CODEC = MapCodec.unit(new SpecialScytheProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (!NecromencyConfig.RENDER_SPECIAL_SCYTHE.get()) return 0f;
        if (owner == null) return 0f;
        var living = owner.asLivingEntity();
        if (living == null) return 0f;
        return SpecialFolk.isSpecial(living.getName().getString()) ? 1f : 0f;
    }

    @Override
    public MapCodec<SpecialScytheProperty> type() {
        return MAP_CODEC;
    }
}
