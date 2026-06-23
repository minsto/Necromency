package com.mickdev.necromency.registry.Altar.Block;

import com.mickdev.necromency.NecromencyServer;
import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.entity.MinionPlayerData;
import com.mickdev.necromency.registry.Altar.AltarPreviewAccess;
import com.mickdev.necromency.registry.Altar.Menu.AltarguiMenu;
import com.mickdev.necromency.registry.BrainMaker.Item.BrainCoreItem;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.init.ModItems;
import com.mickdev.necromency.registry.init.NecromencyModBlockEntities;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mickdev.necromency.registry.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class ALTARBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {

    // ✅ 0..7 uniquement
    public static final int SIZE = 8;
    private NonNullList<ItemStack> stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    // slots:
    // 0 blood
    // 1 legs
    // 2 body
    // 3 right arm
    // 4 left arm
    // 5 head
    // 6 brain (optionnel)
    // 7 soul

    private static final byte MASK_HEAD      = 0x01;
    private static final byte MASK_BODY      = 0x02;
    private static final byte MASK_ARM_LEFT  = 0x04;
    private static final byte MASK_ARM_RIGHT = 0x08;
    private static final byte MASK_LEGS      = 0x10;

    public ALTARBlockEntity(BlockPos pos, BlockState state) {
        super(NecromencyModBlockEntities.ALTAR.get(), pos, state);
    }

    @Override
    public void setRemoved() {
        if (level != null && level.isClientSide()) {
            AltarPreviewAccess.clearPreview(worldPosition);
        }
        super.setRemoved();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean isBrainCore(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof BrainCoreItem);
        // ou: return stack.is(ModItems.BRAIN_CORE.get());
    }

    public boolean tryBuildAbomination(ServerPlayer player) {
        if (level == null || level.isClientSide()) return false;

        ItemStack blood = getItem(0);
        ItemStack legs  = getItem(1);
        ItemStack body  = getItem(2);
        ItemStack armR  = getItem(3);
        ItemStack armL  = getItem(4);
        ItemStack head  = getItem(5);
        ItemStack brain = getItem(6); // optionnel
        ItemStack soul  = getItem(7);

        java.util.function.Consumer<String> msg = s ->
                player.displayClientMessage(Component.literal("ALTAR DEBUG: " + s), true);

        // requis
        if (blood.isEmpty()) { msg.accept("slot 0 blood vide"); return false; }
        if (soul.isEmpty())  { msg.accept("slot 7 soul vide"); return false; }
        if (legs.isEmpty())  { msg.accept("slot 1 legs vide"); return false; }
        if (body.isEmpty())  { msg.accept("slot 2 body vide"); return false; }
        if (armR.isEmpty())  { msg.accept("slot 3 armR vide"); return false; }
        if (armL.isEmpty())  { msg.accept("slot 4 armL vide"); return false; }
        if (head.isEmpty())  { msg.accept("slot 5 head vide"); return false; }

        if (!blood.is(ModItems.Jar_of_Blood.get())) { msg.accept("slot 0 pas Jar_of_Blood"); return false; }
        if (!soul.is(ModItems.JAR_OF_SOUL.get()))   { msg.accept("slot 7 pas Jar_of_Soul"); return false; }

        if (!isCorrectPart(legs, BodyPartItem.PartType.LEGS))      { msg.accept("slot 1 pas LEGS"); return false; }
        if (!isCorrectPart(body, BodyPartItem.PartType.BODY))      { msg.accept("slot 2 pas BODY"); return false; }
        if (!isCorrectPart(armR, BodyPartItem.PartType.ARM_RIGHT)) { msg.accept("slot 3 pas ARM_RIGHT"); return false; }
        if (!isCorrectPart(armL, BodyPartItem.PartType.ARM_LEFT))  { msg.accept("slot 4 pas ARM_LEFT"); return false; }
        if (!isCorrectPart(head, BodyPartItem.PartType.HEAD))      { msg.accept("slot 5 pas HEAD"); return false; }

        if (NecromencyServer.MAX_MINION_SPAWN >= 0
                && MinionPlayerData.getMinionCount(player) >= NecromencyServer.MAX_MINION_SPAWN) {
            msg.accept("limite minions atteinte (" + NecromencyServer.MAX_MINION_SPAWN + ")");
            return false;
        }

        boolean hasBrain = !brain.isEmpty();
        if (hasBrain && !isBrainCore(brain)) {
            msg.accept("slot 6 n'est pas un BrainCore");
            return false;
        }

        String headId = BodyPartItem.resolveMobId(head);
        String bodyId = BodyPartItem.resolveMobId(body);
        String legsId = BodyPartItem.resolveMobId(legs);
        String armLId = BodyPartItem.resolveMobId(armL);
        String armRId = BodyPartItem.resolveMobId(armR);

        String baseId = firstNonEmpty(headId, bodyId, legsId, armLId, armRId);
        if (baseId == null || baseId.isEmpty()) { msg.accept("baseId vide"); return false; }

        byte mask = (byte) (MASK_HEAD | MASK_BODY | MASK_ARM_LEFT | MASK_ARM_RIGHT | MASK_LEGS);

        ItemStack brainCoreStack = hasBrain ? brain.copy() : ItemStack.EMPTY;

        // consume requis
        removeItem(0, 1);
        removeItem(7, 1);
        removeItem(1, 1);
        removeItem(2, 1);
        removeItem(3, 1);
        removeItem(4, 1);
        removeItem(5, 1);
        if (hasBrain) removeItem(6, 1);

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        return spawnMinionForPlayer((ServerLevel) level, player, brainCoreStack, baseId, mask,
                headId, bodyId, legsId, armLId, armRId);
    }

    private static boolean isCorrectPart(ItemStack stack, BodyPartItem.PartType expected) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BodyPartItem)) return false;
        return BodyPartItem.getPart(stack) == expected;
    }

    private static String firstNonEmpty(String... ids) {
        for (String s : ids) if (s != null && !s.isEmpty()) return s;
        return null;
    }
    private static ResourceLocation rlOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return ResourceLocation.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
    private boolean spawnMinionForPlayer(ServerLevel level,
                                         ServerPlayer player,
                                         ItemStack brainCoreStack,
                                         String baseId,
                                         byte mask,
                                         String headId,
                                         String bodyId,
                                         String legsId,
                                         String armLId,
                                         String armRId) {

        if (level.isClientSide()) return false;
        if (baseId == null || baseId.isEmpty()) return false;

        BlockPos base = this.getBlockPos();
        BlockPos[] tries = new BlockPos[] { base.above(2), base.above(3), base.above(4) };

        var type = NecromencyEntities.MINION.get();
        if (type == null) return false;

        for (BlockPos spawnPos : tries) {
            if (!level.hasChunkAt(spawnPos)) continue;
            if (!level.getWorldBorder().isWithinBounds(spawnPos)) continue;

            MinionEntity minion = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (minion == null) return false;

            minion.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 0.05, spawnPos.getZ() + 0.5);
            minion.setYRot(player.getYRot());
            minion.setXRot(0);

           minion.setOwnerUUID(player.getUUID());

            // IDs mob (ex: "minecraft:zombie", "minecraft:chicken", ...)
            ResourceLocation head = rlOrNull(headId);
            ResourceLocation body = rlOrNull(bodyId);
            ResourceLocation legs = rlOrNull(legsId);
            ResourceLocation armL = rlOrNull(armLId);
            ResourceLocation armR = rlOrNull(armRId);

// fallback /summon-safe
            ResourceLocation ZOMBIE = ResourceLocation.parse("minecraft:zombie");
            if (head == null) head = ZOMBIE;
            if (body == null) body = ZOMBIE;
            if (legs == null) legs = ZOMBIE;
            if (armL == null) armL = ZOMBIE;
            if (armR == null) armR = ZOMBIE;

            minion.setHeadId(head);
            minion.setBodyId(body);
            minion.setLegsId(legs);
            minion.setArmLId(armL);
            minion.setArmRId(armR);
            minion.applyBrainCore(brainCoreStack);
            minion.calculateAttributes();

            if (!level.noCollision(minion)) continue;

            boolean ok = level.addFreshEntity(minion);
            if (ok) {
                MinionPlayerData.incrementMinionCount(player);
                com.mickdev.necromency.registry.NecromencyAdvancements.grant(player, com.mickdev.necromency.registry.NecromencyAdvancements.MINION);
                level.playSound(null, BlockPos.containing(minion.getX(), minion.getY(), minion.getZ()),
                        ModSounds.SPAWN.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.85F);
                player.displayClientMessage(Component.literal("<Minion> Your bidding?"), true);
                setChanged();
                minion.refreshDimensions();
                level.sendParticles(ParticleTypes.SMOKE, minion.getX(), minion.getY() + 0.4, minion.getZ(),
                        40, 0.35, 0.25, 0.35, 0.02);
                level.sendParticles(ParticleTypes.POOF, minion.getX(), minion.getY() + 0.4, minion.getZ(),
                        18, 0.25, 0.15, 0.25, 0.03);
                return true;
            }
        }
        return false;
    }

    // ================= Save/Load =================
    @Override
    public void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        if (!this.tryLoadLootTable(in)) {
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(in, this.stacks);
    }

    @Override
    public void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (!this.trySaveLootTable(out)) {
            ContainerHelper.saveAllItems(out, this.stacks);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
        return this.saveWithFullMetadata(lookupProvider);
    }

    // ================= Container / Menu =================
    @Override public int getContainerSize() { return stacks.size(); }
    @Override public boolean isEmpty() { for (ItemStack s : stacks) if (!s.isEmpty()) return false; return true; }
    @Override public Component getDefaultName() { return Component.literal("altar"); }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new AltarguiMenu(id, inv, this, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override public Component getDisplayName() { return Component.literal("ALTAR"); }
    @Override protected NonNullList<ItemStack> getItems() { return stacks; }
    @Override protected void setItems(NonNullList<ItemStack> stacks) { this.stacks = stacks; }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (stack.isEmpty()) return false;

        return switch (index) {
            case 0 -> stack.is(ModItems.Jar_of_Blood.get());
            case 7 -> stack.is(ModItems.JAR_OF_SOUL.get());

            case 1 -> isCorrectPart(stack, BodyPartItem.PartType.LEGS);
            case 2 -> isCorrectPart(stack, BodyPartItem.PartType.BODY);
            case 3 -> isCorrectPart(stack, BodyPartItem.PartType.ARM_RIGHT);
            case 4 -> isCorrectPart(stack, BodyPartItem.PartType.ARM_LEFT);
            case 5 -> isCorrectPart(stack, BodyPartItem.PartType.HEAD);

            case 6 -> isBrainCore(stack); // optionnel
            default -> false;
        };
    }

    public void debugSlots(ServerPlayer player) {
        // ✅ plus de "0..9", on respecte la taille
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack s = getItem(i);
            String info = s.isEmpty() ? "EMPTY" : s.getItem().toString() + " x" + s.getCount();
            player.displayClientMessage(Component.literal("ALTAR SLOT " + i + ": " + info), false);
        }

        ItemStack legs = getItem(1);
        if (!legs.isEmpty() && legs.getItem() instanceof BodyPartItem) {
            player.displayClientMessage(Component.literal("slot1 PartType=" + BodyPartItem.getPart(legs)), false);
            player.displayClientMessage(Component.literal("slot1 MobId=" + BodyPartItem.getMobId(legs)), false);
        }
    }

    @Override public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction dir) { return canPlaceItem(index, stack); }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction dir) { return true; }
}