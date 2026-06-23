package com.mickdev.necromency;

import com.mickdev.necromency.registry.data.NecroMobCatalog;
import com.mickdev.necromency.registry.init.ModBlocks;
import com.mickdev.necromency.registry.init.ModItems;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mickdev.necromency.registry.item.MobPart.BodyPartStacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NecromencyTabs {

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Necromency.MODID);

    private static final ResourceLocation NECRO_TAB_BG =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/gui/tab_necro_gui.png");

    private static final ResourceLocation NECRO_TABS_IMAGE =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/gui/tab_necro_gui.png");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_NECROMANCY =
            TABS.register("necromancy", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.necromency"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> Items.BOOK.getDefaultInstance())
                    .backgroundTexture(NECRO_TAB_BG)          // ✅ fond custom
                    .withTabsImage(NECRO_TABS_IMAGE)          // ✅ boutons d’onglets custom
                    .displayItems((params, out) -> {
                        // out.accept(RegistryNecromencyItems.NECRONOMICON.get());
                        out.accept(ModItems.ISAACS_HEAD.get());
                        out.accept(ModBlocks.BRAIN_MAKER.get());
                        out.accept(ModItems.Necronomicon.get());
                        out.accept(ModBlocks.SWING.get());
                        out.accept(ModItems.BRAIN_ON_A_STICK.get());
                        out.accept(ModBlocks.JOBS_STAND.get());
                        out.accept(ModItems.SCYTHE.get());
                        out.accept(ModItems.BONE_SCYTHE.get());
                        out.accept(ModItems.BONE_NEEDLE.get());
                        out.accept(ModItems.Jar_of_Blood.get());
                        out.accept(ModItems.JAR_OF_SOUL.get());
                        out.accept(ModItems.Blood_BUCKET.get());
                        out.accept(ModItems.Brain_Cutter.get());
                        out.accept(ModItems.BRAIN_CORE.get());
                        out.accept(ModItems.BRAIN_COPPER_GOLEM.get());
                        out.accept(ModItems.BRAIN_ALLAY.get());
                        out.accept(ModItems.BRAIN_ENDERMAN.get());
                        out.accept(ModItems.BRAIN_IRON_GOLEM.get());
                        out.accept(ModItems.BRAINS.get());
                        out.accept(ModItems.HEART.get());
                        out.accept(ModItems.MUSCLE.get());
                        out.accept(ModItems.LUNGS.get());
                        out.accept(ModItems.SOULHEART.get());
                        out.accept(ModItems.SKIN.get());
                        out.accept(ModItems.NECRO_SPAWNER.get());
                        out.accept(ModItems.NECROMANCER_SPAWN_EGG.get());
                        out.accept(ModItems.TEDDY_SPAWN_EGG.get());
                        out.accept(ModBlocks.SKULL_WALL.get());
                        out.accept(ModBlocks.ALTAR_OBSIDIAN.get());
                        out.accept(ModBlocks.FLAME_TOWER.get());

                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_BODYPARTS =
            TABS.register("bodyparts", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.bodyparts"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> new ItemStack(Items.SKELETON_SKULL))
                    .backgroundTexture(NECRO_TAB_BG)
                    .withTabsImage(NECRO_TABS_IMAGE)
                    .displayItems((params, out) -> {
                        for (var mob : NecroMobCatalog.all()) {
                            if (mob.hasHead()) {
                                out.accept(BodyPartStacks.create(BodyPartItem.PartType.HEAD, mob.mobId()));
                            }
                            if (mob.hasTorso()) {
                                out.accept(BodyPartStacks.create(BodyPartItem.PartType.BODY, mob.mobId()));
                            }
                            if (mob.hasArms()) {
                                out.accept(BodyPartStacks.create(BodyPartItem.PartType.ARM_LEFT, mob.mobId()));
                                out.accept(BodyPartStacks.create(BodyPartItem.PartType.ARM_RIGHT, mob.mobId()));
                            }
                            if (mob.hasLegs()) {
                                out.accept(BodyPartStacks.create(BodyPartItem.PartType.LEGS, mob.mobId()));
                            }
                        }
                    })
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private NecromencyTabs() {}
}