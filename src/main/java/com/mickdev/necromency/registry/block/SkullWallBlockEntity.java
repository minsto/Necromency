package com.mickdev.necromency.registry.block;



import com.mickdev.necromency.registry.init.NecromencyModBlockEntities;

import net.minecraft.core.BlockPos;

import net.minecraft.core.HolderLookup;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.world.level.storage.ValueOutput;



public class SkullWallBlockEntity extends BlockEntity {



    private String baseBlock = "minecraft:obsidian";

    private String skullType = "skeleton";

    private String skull2 = "zombie";

    private String skull3 = "creeper";



    public SkullWallBlockEntity(BlockPos pos, BlockState state) {

        super(NecromencyModBlockEntities.SKULL_WALL.get(), pos, state);

    }



    public String getBaseBlock() { return baseBlock; }

    public String getSkullType() { return skullType; }

    public String getSkull2() { return skull2; }

    public String getSkull3() { return skull3; }



    public void setBaseBlock(String baseBlock) {

        this.baseBlock = baseBlock;

        setChanged();

    }



    public void setSkullType(String skullType) {

        this.skullType = skullType;

        setChanged();

    }



    public void setSkull2(String skull2) {

        this.skull2 = skull2;

        setChanged();

    }



    public void setSkull3(String skull3) {

        this.skull3 = skull3;

        setChanged();

    }



    @Override

    protected void saveAdditional(ValueOutput output) {

        super.saveAdditional(output);

        output.putString("Base", baseBlock);

        output.putString("SkullType", skullType);

        output.putString("Skull2", skull2);

        output.putString("Skull3", skull3);

    }



    @Override

    protected void loadAdditional(ValueInput input) {

        super.loadAdditional(input);

        baseBlock = input.getStringOr("Base", "minecraft:obsidian");

        skullType = input.getStringOr("SkullType", "skeleton");

        skull2 = input.getStringOr("Skull2", "zombie");

        skull3 = input.getStringOr("Skull3", "creeper");

    }



    @Override

    public ClientboundBlockEntityDataPacket getUpdatePacket() {

        return ClientboundBlockEntityDataPacket.create(this);

    }



    @Override

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {

        return saveWithoutMetadata(registries);

    }

}

