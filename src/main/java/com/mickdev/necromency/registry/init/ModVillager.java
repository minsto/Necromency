package com.mickdev.necromency.registry.init;

import com.google.common.collect.ImmutableSet;
import com.mickdev.necromency.Necromency;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.stream.Collectors;

public final class ModVillager {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Necromency.MODID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, Necromency.MODID);

    private static Set<BlockState> jobStandStates() {
        return ModBlocks.JOBS_STAND.get().getStateDefinition().getPossibleStates().stream()
                .collect(Collectors.toSet());
    }

    public static final DeferredHolder<PoiType, PoiType> NECROMANCER_POI =
            POI_TYPES.register("necromancer", () -> new PoiType(
                    ImmutableSet.copyOf(jobStandStates()),
                    1,
                    1
            ));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> NECROMANCER =
            PROFESSIONS.register("necromancer", () -> new VillagerProfession(
                    Component.translatable("entity.minecraft.villager.necromency.necromancer"),
                    holder -> holder.is(NECROMANCER_POI),
                    holder -> holder.is(NECROMANCER_POI),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN
            ));

    private ModVillager() {}
}
