package com.mickdev.necromency.registry.Swing.Menu;

import com.mickdev.necromency.registry.Swing.Block.SwingBlockEntity;
import com.mickdev.necromency.registry.init.NecromencyModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SwingGuiMenu extends AbstractContainerMenu {

    public final Level world;
    public final Player entity;
    public final int x, y, z;

    private final SwingBlockEntity be;
    private final ContainerLevelAccess access;

    // ✅ CONSTRUCTEUR CLIENT (appelé via IMenuTypeExtension.create)
    public SwingGuiMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, getBE(inv, extraData));
    }

    // ✅ CONSTRUCTEUR SERVEUR (appelé quand tu openMenu)
    public SwingGuiMenu(int id, Inventory inv, SwingBlockEntity be) {
        super(NecromencyModMenus.SWING_GUI.get(), id);

        this.be = be;
        this.world = inv.player.level();
        this.entity = inv.player;

        BlockPos pos = be.getBlockPos();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.access = ContainerLevelAccess.create(world, pos);

        // ---- 4x4 INPUT (0..15)
        int startX = 31;
        int startY = 16;

        int slot = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int sx = startX + col * 18;
                int sy = startY + row * 18;
                this.addSlot(new Slot(be, slot++, sx, sy));
            }
        }

        // ---- OUTPUT slot 16
        this.addSlot(new Slot(be, SwingBlockEntity.OUT_SLOT, 140, 43) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void onTake(Player player, ItemStack taken) {
                super.onTake(player, taken);

                if (player.level().isClientSide()) return;

                // Consomme 1 item dans chaque slot d’input (0..15) si non vide
                for (int i = 0; i < SwingBlockEntity.INPUT_COUNT; i++) {
                    ItemStack in = be.getItem(i);
                    if (!in.isEmpty()) {
                        in.shrink(1);
                        be.setItem(i, in);
                    }
                }

                be.setChanged(); // marque BE modifiée
                // ton serverTick va recalculer l’output juste après
            }
        });

        // ---- Player inventory
        int invStartY = 110;
        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 6 + 8 + sj * 18, 18 + 84 + si * 18));



        // ---- Hotbar
        for (int c = 0; c < 9; ++c)
            this.addSlot(new Slot(inv, c, 14 + c * 18, invStartY + 50));
    }

    private static SwingBlockEntity getBE(Inventory inv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        var be = inv.player.level().getBlockEntity(pos);
        if (!(be instanceof SwingBlockEntity swing)) {
            throw new IllegalStateException("Swing BE not found at " + pos);
        }
        return swing;
    }

    @Override
    public boolean stillValid(Player player) {
        return be.stillValid(player);
    }

    private void consumeIngredientsOnce() {
        // Rebuild l'input 0..15 depuis le BE
        var list = new java.util.ArrayList<ItemStack>(SwingBlockEntity.INPUT_COUNT);
        for (int i = 0; i < SwingBlockEntity.INPUT_COUNT; i++) {
            list.add(be.getItem(i));
        }

        var level = (net.minecraft.server.level.ServerLevel) world;
        var input = new com.mickdev.necromency.registry.Swing.Recipes.SwingInput(list);

        var opt = level.getServer()
                .getRecipeManager()
                .getRecipeFor(com.mickdev.necromency.registry.Swing.Recipes.SwingRecipeType.SWING_SHAPED.get(), input, level);

        if (opt.isEmpty()) return;

        var recipe = opt.get().value(); // SwingShapedRecipe

        // Consomme 1 item par slot requis (Ingredient non-vide)
        for (int i = 0; i < SwingBlockEntity.INPUT_COUNT; i++) {
            var ing = recipe.getSlots4x4().get(i);
            if (ing.isEmpty()) continue;

            ItemStack in = be.getItem(i);
            if (!in.isEmpty()) {
                in.shrink(1);
                be.setItem(i, in);
            }
        }

        be.setChanged();
        this.broadcastChanges(); // refresh immédiat côté menu
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return empty;

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();

        // Indices menu
        final int INPUT_START = 0;
        final int INPUT_END = 16;              // 0..15
        final int OUTPUT_INDEX = 16;           // slot menu output
        final int PLAYER_INV_START = 17;       // après output
        final int PLAYER_INV_END = 44;         // 17..43
        final int HOTBAR_START = 44;           // 44..52
        final int HOTBAR_END = 53;             // exclusive

        // 1) SHIFT-CLICK sur OUTPUT
        if (index == OUTPUT_INDEX) {
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, true))
                return empty;

            // Important : déclenche onTake => consomme ingrédients
            slot.onTake(player, stackInSlot);
        }
        // 2) SHIFT-CLICK depuis la machine (inputs)
        else if (index >= INPUT_START && index < INPUT_END) {
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, false))
                return empty;
        }
        // 3) SHIFT-CLICK depuis inventaire joueur -> tenter de mettre dans inputs
        else if (index >= PLAYER_INV_START && index < HOTBAR_END) {
            if (!this.moveItemStackTo(stackInSlot, INPUT_START, INPUT_END, false)) {
                // sinon inv <-> hotbar
                if (index < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(stackInSlot, HOTBAR_START, HOTBAR_END, false))
                        return empty;
                } else {
                    if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_INV_END, false))
                        return empty;
                }
            }
        }

        if (stackInSlot.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }

}