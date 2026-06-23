package com.mickdev.necromency.Client.Util;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class MinionRenderState extends HumanoidRenderState {

    public ResourceLocation headType;
    public ResourceLocation bodyType;
    public ResourceLocation armLType;
    public ResourceLocation armRType;
    public ResourceLocation legsType;

    @Nullable
    public ResourceLocation partPoseAnchor;

    /** Membres poulet en mesh atlas 128×64 : pas de PartProfiles (géométrie déjà poulet). */
    public boolean skipChickenLimbProfiles;

    public boolean saddled;
    @Nullable
    public ResourceLocation saddleTexture;

    public void setDefaultZombieIfNull() {
        if (headType == null) headType = MobTextureResolver.ZOMBIE_ID;
        if (bodyType == null) bodyType = MobTextureResolver.ZOMBIE_ID;
        if (armLType == null) armLType = MobTextureResolver.ZOMBIE_ID;
        if (armRType == null) armRType = MobTextureResolver.ZOMBIE_ID;
        if (legsType == null) legsType = MobTextureResolver.ZOMBIE_ID;
    }
}
