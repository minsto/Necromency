package com.mickdev.necromency.registry.Swing.Block;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Swing.Menu.SwingGuiMenu;
import com.mickdev.necromency.registry.Swing.Recipes.SwingInput;
import com.mickdev.necromency.registry.Swing.Recipes.SwingRecipeType;
import com.mickdev.necromency.registry.init.NecromencyModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;

public class SwingBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer, RecipeInput {

    public static final int INPUT_COUNT = 16;  // 4x4
    public static final int OUT_SLOT = 17;
    public static final int SIZE = 18;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private static final int[] SLOTS_INPUT = new int[]{
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15
    };
    private static final int[] SLOTS_OUTPUT = new int[]{OUT_SLOT};

    public SwingBlockEntity(BlockPos pos, BlockState state) {
        super(NecromencyModBlockEntities.SWING.get(), pos, state);
    }

    // ===== MenuProvider =====
    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.necromency.swing");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new SwingGuiMenu(id, inv, this);
    }

    // ===== Save / Load =====
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
    }

    // ===== Container =====
    @Override public int getContainerSize() { return SIZE; }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    //@Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public int size() {
        return INPUT_COUNT; // 16
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack ret = ContainerHelper.removeItem(items, slot, amount);
        if (!ret.isEmpty()) setChanged();
        return ret;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack ret = ContainerHelper.takeItem(items, slot);
        if (!ret.isEmpty()) setChanged();
        return ret;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
        ) <= 64.0;
    }
    @Override
    public ItemStack getItem(int index) {
        if (index < 0 || index >= SIZE) return ItemStack.EMPTY; // SIZE = 18
        return items.get(index);
    }
    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    // ===== WorldlyContainer (sided) =====
    @Override
    public int[] getSlotsForFace(Direction side) {
        return (side == Direction.DOWN) ? SLOTS_OUTPUT : SLOTS_INPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot != OUT_SLOT; // pas dans output
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == OUT_SLOT; // on sort seulement output
    }
    public void serverTick() {
        updateRecipeResult();
    }


    private void updateRecipeResult() {


        if (level == null || level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;

        var list = new java.util.ArrayList<ItemStack>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) list.add(items.get(i));

        SwingInput input = new SwingInput(list);

        var opt = serverLevel.getServer()
                .getRecipeManager()
                .getRecipeFor(SwingRecipeType.SWING_SHAPED.get(), input, serverLevel);

        ItemStack out = opt
                .map(holder -> holder.value().assemble(input, serverLevel.registryAccess()))
                .orElse(ItemStack.EMPTY);

        setItem(OUT_SLOT, out); // ✅ au lieu de items.set(...)
    }


}