package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.NecromencyConfig;
import com.mickdev.necromency.entity.EntityIsaacNormal;
import com.mickdev.necromency.entity.NightCrawlerEntity;
import com.mickdev.necromency.registry.NecromencyEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class NecromencyMobEvents {

    private static final String SPAWN_CHECKED = "necromency.spawn_checked";

    private NecromencyMobEvents() {}

    /** Organes et cerveaux typés : voir {@link BrainCutterEvents} (brain cutter requis). */

    /** Port {@code ForgeEventHandler#onPotentialSpawns} (Isaac / Nightcrawler). */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (mob.getPersistentData().getBoolean(SPAWN_CHECKED).orElse(false)) {
            return;
        }
        mob.getPersistentData().putBoolean(SPAWN_CHECKED, true);

        EntitySpawnReason reason = mob.getSpawnType();
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return;
        }

        if (!(mob instanceof Zombie) && !(mob instanceof Skeleton)) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        var random = level.getRandom();
        double x = mob.getX();
        double y = mob.getY();
        double z = mob.getZ();
        float rot = mob.getYRot();

        if (mob instanceof Zombie && random.nextInt(NecromencyConfig.RARITY_NIGHTCRAWLERS.get()) == 0) {
            mob.discard();
            NightCrawlerEntity nc = new NightCrawlerEntity(NecromencyEntities.NIGHTCRAWLER.get(), level);
            nc.setPos(x, y, z);
            nc.setYRot(rot);
            level.addFreshEntity(nc);
            return;
        }

        if (mob instanceof Skeleton && random.nextInt(NecromencyConfig.RARITY_ISAACS.get()) == 0) {
            mob.discard();
            EntityIsaacNormal isaac = new EntityIsaacNormal(NecromencyEntities.ISAAC_NORMAL.get(), level);
            isaac.setPos(x, y, z);
            isaac.setYRot(rot);
            level.addFreshEntity(isaac);
        }
    }
}
