package com.mickdev.necromency.registry.BrainMaker.Gui;


import com.mickdev.necromency.registry.BrainMaker.BrainMakerLogic;
import com.mickdev.necromency.registry.BrainMaker.BrainTypes;
import com.mickdev.necromency.registry.init.ModItems;
import com.mickdev.necromency.registry.init.NecromencyModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BrainMakerGuiMenu extends AbstractContainerMenu implements NecromencyModMenus.MenuAccessor {

    public final Level world;
    public final Player entity;

    public final BlockPos pos;
    public final int x, y, z;

    private final ContainerLevelAccess access;
    private final IItemHandler internal;

    private final Map<Integer, Slot> customSlots = new HashMap<>();

    public final Map<String, Object> menuState = new HashMap<>() {
        @Override
        public Object put(String key, Object value) {
            if (!this.containsKey(key) && this.size() >= 7) return null;
            return super.put(key, value);
        }
    };

    public BrainMakerGuiMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos());
    }

    public BrainMakerGuiMenu(int id, Inventory inv, BlockPos pos) {
        super(NecromencyModMenus.BRAIN_MAKER_GUI.get(), id);

        this.entity = inv.player;
        this.world = inv.player.level();

        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();

        this.access = ContainerLevelAccess.create(world, pos);

        // 4 slots : cerveau 1 (haut droite), cerveau 2 (haut gauche), sang (bas droite), sortie (bas gauche)
        this.internal = new ItemStackHandler(4);

        // Haut droite — cerveau typé ou brain_core existant
        this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 167, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BrainMakerLogic.isValidInput(stack);
            }
        }));

        // Haut gauche — 2e cerveau typé (fusion copper + allay, etc.)
        this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 54, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BrainTypes.isTypedBrain(stack);
            }
        }));

        this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 167, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.Jar_of_Blood.get());
            }
        }));

        this.customSlots.put(3, this.addSlot(new SlotItemHandler(internal, 3, 54, 70) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        }));

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 35 + 8 + sj * 18, 18 + 84 + si * 18));

        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 35 + 8 + si * 18, 18 + 142));
    }

    @Override
    public Map<Integer, Slot> getSlots() {
        return Collections.unmodifiableMap(customSlots);
    }

    @Override
    public Map<String, Object> getMenuState() {
        return menuState;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, world.getBlockState(pos).getBlock());
    }

    /** Fusionne 1–2 cerveaux (ou brain_core + cerveau) + sang → brain_core cumulatif. */
    public void handleAdd(ServerPlayer player) {
        ItemStack input1 = internal.getStackInSlot(0);
        ItemStack input2 = internal.getStackInSlot(1);
        ItemStack blood = internal.getStackInSlot(2);
        ItemStack out   = internal.getStackInSlot(3);

        int primarySlot = 0;
        if (!BrainMakerLogic.isValidInput(input1) && BrainMakerLogic.isValidInput(input2)) {
            input1 = input2;
            input2 = ItemStack.EMPTY;
            primarySlot = 1;
        }
        if (!BrainMakerLogic.isValidInput(input1)) return;
        if (!input2.isEmpty() && !BrainTypes.isTypedBrain(input2)) return;
        if (blood.isEmpty() || !blood.is(ModItems.Jar_of_Blood.get())) return;
        if (!out.isEmpty()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Sortie pleine : retire le brain core avant d'ajouter."),
                    true);
            return;
        }

        internal.extractItem(primarySlot, 1, false);
        if (!input2.isEmpty()) {
            internal.extractItem(1, 1, false);
        }
        internal.extractItem(2, 1, false);

        ItemStack result = BrainMakerLogic.makeBrainCore(input1, input2);
        internal.insertItem(3, result, false);

        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        itemstack = stackInSlot.copy();

        int machineSlots = 4;
        int invStart = machineSlots;
        int invEnd = this.slots.size();

        if (index < machineSlots) {
            if (!this.moveItemStackTo(stackInSlot, invStart, invEnd, true)) return ItemStack.EMPTY;
        } else {
            if (BrainMakerLogic.isValidInput(stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)
                        && BrainTypes.isTypedBrain(stackInSlot)
                        && !this.moveItemStackTo(stackInSlot, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.is(ModItems.BRAIN_CORE.get())) {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
            } else if (stackInSlot.is(ModItems.Jar_of_Blood.get())) {
                if (!this.moveItemStackTo(stackInSlot, 2, 3, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }
}
