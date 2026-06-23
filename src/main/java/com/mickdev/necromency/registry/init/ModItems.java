package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.BrainMaker.Item.BrainCoreItem;
import com.mickdev.necromency.registry.Necromocon.NecronomiconItem;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.init.Fluid.Item.BloodItem;
import com.mickdev.necromency.registry.init.Fluid.Item.ScytheItem;
import com.mickdev.necromency.registry.item.BoneNeedleItem;
import com.mickdev.necromency.registry.item.BrainOnAStickItem;
import com.mickdev.necromency.registry.item.IsaacsHeadItem;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mickdev.necromency.registry.item.NecroSpawnerItem;
import com.mickdev.necromency.registry.item.NecromancerSpawnItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    // ✅ IMPORTANT: createItems, pas create(Registries.ITEM,...)
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Necromency.MODID);
public static final DeferredHolder<Item,Item> Jar_of_Blood=
        ITEMS.registerItem("jar_of_blood", props -> new Item(props.stacksTo(16)));
    public static final DeferredHolder<Item,Item> BONE_NEEDLE=
            ITEMS.registerItem("bone_needle", props -> new BoneNeedleItem(props.stacksTo(1)));
    public static final DeferredHolder<Item,Item> Brain_Cutter=
            ITEMS.registerItem("brain_cutter", props -> new BoneNeedleItem(props.stacksTo(1)));
    public static final DeferredHolder<Item, Item> BRAIN_ON_A_STICK =
            ITEMS.registerItem("brain_on_a_stick", props -> new BrainOnAStickItem(props.stacksTo(1)));

    public static final DeferredHolder<Item, BrainCoreItem> BRAIN_COPPER_GOLEM =
            ITEMS.registerItem("brain_copper_golem", props -> new BrainCoreItem(props.stacksTo(1)));
    public static final DeferredHolder<Item, BrainCoreItem> BRAIN_ALLAY =
            ITEMS.registerItem("brain_allay", props -> new BrainCoreItem(props.stacksTo(1)));
    public static final DeferredHolder<Item, BrainCoreItem> BRAIN_ENDERMAN =
            ITEMS.registerItem("brain_enderman", props -> new BrainCoreItem(props.stacksTo(1)));
    public static final DeferredHolder<Item, BrainCoreItem> BRAIN_IRON_GOLEM =
            ITEMS.registerItem("brain_iron_golem", props -> new BrainCoreItem(props.stacksTo(1)));
    public static final DeferredItem<Item> Necronomicon =
            ITEMS.registerItem("necronomicon", props -> new NecronomiconItem(props.stacksTo(1)));
    public static final DeferredHolder<Item, BrainCoreItem> BRAIN_CORE =
            ITEMS.registerItem("brain_core", props -> new BrainCoreItem(props.stacksTo(1)));
    public static final DeferredHolder<Item, IsaacsHeadItem> ISAACS_HEAD =
            ITEMS.registerItem("isaacs_head", props -> new IsaacsHeadItem(props.stacksTo(1)));

    public static final DeferredHolder<Item, Item> TEAR_ITEM =
            ITEMS.registerItem("tear", props -> new Item(props));
    public static final DeferredHolder<Item, Item> BloodTEAR_ITEM =
            ITEMS.registerItem("bloodtear", props -> new Item(props));
    public static final DeferredHolder<Item, BlockItem> BRAIN_MAKER_ITEM =
            ITEMS.registerItem("brain_maker", props ->
                    new BlockItem(ModBlocks.BRAIN_MAKER.get(), props)
            );
    public static final DeferredHolder<Item, BlockItem> ALTAR_Item =
            ITEMS.registerItem("altar", props ->
                    new BlockItem(ModBlocks.ALTAR.get(), props)
            );
    public static final DeferredHolder<Item, BlockItem> swing_Item =
            ITEMS.registerItem("swing", props ->
                    new BlockItem(ModBlocks.SWING.get(), props)
            );
    public static final DeferredHolder<Item, BlockItem> SKULL_WALL_ITEM =
            ITEMS.registerItem("skull_wall", props -> new BlockItem(ModBlocks.SKULL_WALL.get(), props) {
                @Override
                protected boolean updateCustomBlockEntityTag(
                        net.minecraft.core.BlockPos pos,
                        net.minecraft.world.level.Level level,
                        net.minecraft.world.entity.player.Player player,
                        ItemStack stack,
                        net.minecraft.world.level.block.state.BlockState state) {
                    boolean ok = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
                    if (level.getBlockEntity(pos) instanceof com.mickdev.necromency.registry.block.SkullWallBlockEntity be) {
                        if (stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) == null) {
                            com.mickdev.necromency.registry.block.SkullWallData.writeDefaults(stack);
                        }
                        com.mickdev.necromency.registry.block.SkullWallData.applyToBlockEntity(stack, be);
                    }
                    return ok;
                }
            });
    public static final DeferredHolder<Item, BlockItem> ALTAR_BUILDING_ITEM =
            ITEMS.registerItem("altar_block", props -> new BlockItem(ModBlocks.ALTAR_BUILDING.get(), props));

    public static final DeferredHolder<Item, BlockItem> ALTAR_OBSIDIAN_ITEM =
            ITEMS.registerItem("altarobsidian", props -> new BlockItem(ModBlocks.ALTAR_OBSIDIAN.get(), props));
    public static final DeferredHolder<Item, BlockItem> FLAME_TOWER_ITEM =
            ITEMS.registerItem("flametower", props -> new BlockItem(ModBlocks.FLAME_TOWER.get(), props));
    public static final DeferredHolder<Item, BlockItem> NECRO_SOUL_FLAME_ITEM =
            ITEMS.registerItem("necrosoulflame", props -> new BlockItem(ModBlocks.NECRO_SOUL_FLAME.get(), props));

    public static final DeferredHolder<Item, BlockItem> JOBS_STAND_ITEM =
            ITEMS.registerItem("necromency_jobs_stand", props -> new BlockItem(ModBlocks.JOBS_STAND.get(), props));


    public static final DeferredHolder<Item, Item> JAR_OF_SOUL =
            ITEMS.registerItem("jar_of_soul", props -> new Item(props.stacksTo(16)));

    public static final DeferredHolder<Item, Item> SCYTHE =
            ITEMS.registerItem("scypthe", props -> new ScytheItem(props.stacksTo(1)));

    public static final DeferredHolder<Item, Item> BONE_SCYTHE =
            ITEMS.registerItem("bone_scypth", props -> new ScytheItem(props.stacksTo(1)));
    public static final DeferredHolder<Item, Item> Blood_BUCKET =
            ITEMS.registerItem("blood_bucket",  props -> new BloodItem(props.stacksTo(1)));

    // ORGANS (exemples) -> remplace par tes vrais holders
    public static final DeferredHolder<Item, Item> BRAINS =
            ITEMS.registerItem("brain", props -> new Item(props));
    public static final DeferredHolder<Item, Item> HEART = ITEMS.registerItem("heart",props -> new Item(props) );
    public static final DeferredHolder<Item, Item> LUNGS = ITEMS.registerItem("lungs", props -> new Item(props));
    public static final DeferredHolder<Item, Item> MUSCLE = ITEMS.registerItem("muscle", props -> new Item(props));
    public static final DeferredHolder<Item, Item> SOULHEART = ITEMS.registerItem("soulheart", props -> new Item(props));
    public static final DeferredHolder<Item, Item> SKIN =
            ITEMS.registerItem("skin", props -> new Item(props));
    public static final DeferredHolder<Item, BodyPartItem> BODY_PART =
            ITEMS.registerItem("body_part", props -> new BodyPartItem(BodyPartItem.PartType.HEAD, null, props.stacksTo(16)));
    public static final DeferredHolder<Item, Item> NECRO_SPAWNER =
            ITEMS.registerItem("necro_spawner", props -> new NecroSpawnerItem(props.stacksTo(16)));

    public static final DeferredHolder<Item, Item> NECROMANCER_SPAWN_EGG =
            ITEMS.registerItem("necromancer_spawn_egg", props -> new NecromancerSpawnItem(props.stacksTo(16)));

    public static final DeferredHolder<Item, SpawnEggItem> TEDDY_SPAWN_EGG =
            ITEMS.registerItem("teddy_spawn_egg", props -> new SpawnEggItem(
                    props.spawnEgg(NecromencyEntities.TEDDY.get())
            ));

    //bodyparts
    public static final DeferredHolder<Item, BodyPartItem> CHICKEN_LEG_PART =
            ITEMS.registerItem("chiken_leg", props -> {
                var tag = new CompoundTag();
                tag.putString(BodyPartItem.TAG_PART, BodyPartItem.PartType.LEGS.id);
                tag.putString(BodyPartItem.TAG_MOB, "minecraft:chicken");

                return new BodyPartItem(
                        BodyPartItem.PartType.LEGS,
                        "minecraft:chicken",
                        props.stacksTo(16)
                                .component(DataComponents.CUSTOM_DATA, CustomData.of(tag))
                );
            });
    public static final DeferredHolder<Item, BodyPartItem> Chiken_Left_Arms_PART =
            ITEMS.registerItem("chiken_left_arm",
                    props -> new BodyPartItem(
                            BodyPartItem.PartType.ARM_LEFT,
                            "minecraft:chicken",
                            props.stacksTo(16)
                    )
            );
    public static final DeferredHolder<Item, BodyPartItem> Chiken_Right_ARMS_PART =
            ITEMS.registerItem("chiken_right_arm", props -> new BodyPartItem(BodyPartItem.PartType.ARM_RIGHT, "minecraft:chicken",props.stacksTo(16)));
    public static final DeferredHolder<Item, BodyPartItem> Chiken_BODY_PART =
            ITEMS.registerItem("chiken_body", props -> new BodyPartItem(BodyPartItem.PartType.BODY, "minecraft:chicken",props.stacksTo(16)));
    public static final DeferredHolder<Item, BodyPartItem> Chiken_HEAD_PART =
            ITEMS.registerItem("chiken_head", props -> new BodyPartItem(BodyPartItem.PartType.HEAD, "minecraft:chicken",props.stacksTo(16)));
    //Zombie part
    public static final DeferredHolder<Item, BodyPartItem> ZOMBIE_LEG_PART =
            ITEMS.registerItem("zombie_leg", props -> {
                var tag = new CompoundTag();
                tag.putString(BodyPartItem.TAG_PART, BodyPartItem.PartType.LEGS.id);
                tag.putString(BodyPartItem.TAG_MOB, "minecraft:zombie");

                return new BodyPartItem(
                        BodyPartItem.PartType.LEGS,
                        "minecraft:zombie",
                        props.stacksTo(16)
                                .component(DataComponents.CUSTOM_DATA, CustomData.of(tag))
                );
            });
    public static final DeferredHolder<Item, BodyPartItem> ZOMBIE_Left_Arms_PART =
            ITEMS.registerItem("zombie_left_arm",
                    props -> new BodyPartItem(
                            BodyPartItem.PartType.ARM_LEFT,
                            "minecraft:zombie",
                            props.stacksTo(16)
                    )
            );
    public static final DeferredHolder<Item, BodyPartItem> ZOMBIE_Right_ARMS_PART =
            ITEMS.registerItem("zombie_right_arm", props -> new BodyPartItem(BodyPartItem.PartType.ARM_RIGHT, "minecraft:zombie",props.stacksTo(16)));
    public static final DeferredHolder<Item, BodyPartItem> ZOMBIE_BODY_PART =
            ITEMS.registerItem("zombie_body", props -> new BodyPartItem(BodyPartItem.PartType.BODY, "minecraft:zombie",props.stacksTo(16)));
    public static final DeferredHolder<Item, BodyPartItem> ZOMBIE_HEAD_PART =
            ITEMS.registerItem("zombie_head", props -> new BodyPartItem(BodyPartItem.PartType.HEAD, "minecraft:zombie",props.stacksTo(16)));
    //villager
    public static final DeferredHolder<Item, BodyPartItem> VILLAGER_HEAD_PART =
            ITEMS.registerItem("villager_head", props -> new BodyPartItem(BodyPartItem.PartType.HEAD, "minecraft:villager",props.stacksTo(16)));

}