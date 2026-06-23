package com.mickdev.necromency.registry.Altar.Menu;

import com.mickdev.necromency.registry.Altar.Block.ALTARBlockEntity;
import com.mickdev.necromency.registry.init.ModBlocks;
import com.mickdev.necromency.registry.init.ModItems;
import com.mickdev.necromency.registry.init.NecromencyModMenus;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AltarguiMenu extends AbstractContainerMenu {

    public static final int SLOT_COUNT = 8; // 0..8

    private final ALTARBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    // ===== Client constructor =====
    public AltarguiMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, getBlockEntity(playerInv, buf), ContainerLevelAccess.NULL);
    }

    // ===== Server constructor =====
    public AltarguiMenu(int id, Inventory playerInv, ALTARBlockEntity be, ContainerLevelAccess access) {
        super(NecromencyModMenus.ALTARGUI.get(), id);
        this.blockEntity = be;
        this.access = access;

        /*
         Slot mapping:
         0 = Blood Jar
         1 = LEGS
         2 = BODY
         3 = RIGHT ARM
         4 = LEFT ARM
         5 = HEAD
         6 = BRAIN CORE
         7 = Soul Jar

        */

        // ===== ALTAR slots =====

        // 0 Blood
        this.addSlot(new Slot(be, 0, 24, 24) {
            @Override public boolean mayPlace(ItemStack s) {
                return s.is(ModItems.Jar_of_Blood.get());
            }
        });

        // 1 LEGS
        this.addSlot(new Slot(be, 1, 91, 74) {
            @Override public boolean mayPlace(ItemStack s) {
                return isBodyPart(s, BodyPartItem.PartType.LEGS) ;
            }
        });

        // 2 BODY
        this.addSlot(new Slot(be, 2, 91, 55) {
            @Override public boolean mayPlace(ItemStack s) {
                return isBodyPart(s, BodyPartItem.PartType.BODY) ;
            }
        });

        // 3 RIGHT ARM
        this.addSlot(new Slot(be, 3, 73, 55) {
            @Override public boolean mayPlace(ItemStack s) {
                return isBodyPart(s, BodyPartItem.PartType.ARM_RIGHT) ;
            }
        });

        // 4 LEFT ARM
        this.addSlot(new Slot(be, 4, 110, 55) {
            @Override public boolean mayPlace(ItemStack s) {
                return isBodyPart(s, BodyPartItem.PartType.ARM_LEFT) ;
            }
        });

        // 5 HEAD
        this.addSlot(new Slot(be, 5, 91, 37) {
            @Override public boolean mayPlace(ItemStack s) {
                return isBodyPart(s, BodyPartItem.PartType.HEAD) ;
            }
        });

        // 6 BRAIN CORE
        this.addSlot(new Slot(be, 6, 175, 65) {
            @Override public boolean mayPlace(ItemStack s) {
                return s.is(ModItems.BRAIN_CORE.get());
            }
        });



        // 8 SOUL
        this.addSlot(new Slot(be, 7, 174, 21) {
            @Override public boolean mayPlace(ItemStack s) {
                return s.is(ModItems.JAR_OF_SOUL.get());
            }
        });

        // ===== Player inventory =====
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    // ===== Utils =====
    private static ALTARBlockEntity getBlockEntity(Inventory playerInv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = playerInv.player.level();
        if (level.getBlockEntity(pos) instanceof ALTARBlockEntity be) return be;
        throw new IllegalStateException("ALTARBlockEntity not found at " + pos);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ALTAR.get());
    }

    // ===== BODY PART HELPERS =====
    private boolean isBodyPart(ItemStack stack, BodyPartItem.PartType type) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BodyPartItem)) return false;
        return BodyPartItem.isPart(stack, type) && BodyPartItem.hasValidMob(stack);
    }

    /** Force toutes les parts à venir du même mob */
    private boolean sameMobAsOthers(ItemStack incoming) {
        String mob = BodyPartItem.getMobId(incoming);
        if (mob == null) return false;

        for (int s : new int[]{1,2,3,4,5}) { // slots parts
            ItemStack existing = blockEntity.getItem(s);
            if (existing.isEmpty()) continue;

            String m2 = BodyPartItem.getMobId(existing);
            if (m2 == null) continue;

            if (!mob.equals(m2)) return false;
        }
        return true;
    }

    // ===== SHIFT CLICK SAFE =====
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return empty;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        int altarStart = 0;
        int altarEnd = SLOT_COUNT; // 0..8
        int playerStart = altarEnd;
        int playerEnd = playerStart + 36;

        // ALTAR -> player
        if (index < altarEnd) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
        }
        // player -> ALTAR (smart routing)
        else {
            boolean moved = false;

            if (stack.is(ModItems.Jar_of_Blood.get()))
                moved = moveItemStackTo(stack, 0, 1, false);

            else if (stack.is(ModItems.JAR_OF_SOUL.get()))
                moved = moveItemStackTo(stack, 7, 8, false);

            else if (stack.is(ModItems.BRAIN_CORE.get()))
                moved = moveItemStackTo(stack, 6, 7, false);

            else if (stack.getItem() instanceof BodyPartItem bp) {
                BodyPartItem.PartType part = BodyPartItem.getPart(stack);
                if (part != null) {
                    switch (part) {
                        case LEGS -> moved = moveItemStackTo(stack, 1, 2, false);
                        case BODY -> moved = moveItemStackTo(stack, 2, 3, false);
                        case ARM_RIGHT -> moved = moveItemStackTo(stack, 3, 4, false);
                        case ARM_LEFT -> moved = moveItemStackTo(stack, 4, 5, false);
                        case HEAD -> moved = moveItemStackTo(stack, 5, 6, false);
                    }
                }
            }

            if (!moved)
                moved = moveItemStackTo(stack, altarStart, altarEnd, false);

            if (!moved) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }

    // ===== Player inventory layout =====
    private void addPlayerInventory(Inventory inv) {
        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 26 + 8 + sj * 18, 26 + 84 + si * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 26 + 8 + si * 18, 26 + 142));
    }

    public ALTARBlockEntity getBlockEntity() {
        return blockEntity;
    }
}