package com.mickdev.necromency.necroapi.NecroAPIRemake;

import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
public record MinionBuildPlan(String mobId, byte partMask) {
    @Nullable
    private static MinionBuildPlan findBuildPlan(Player p) {
        String mobId = null;
        byte mask = 0;

        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.isEmpty() || !(s.getItem() instanceof BodyPartItem)) continue;

            BodyPartItem.PartType part = BodyPartItem.getPart(s);
            String m = BodyPartItem.getMobId(s);
            if (part == null || m == null) continue;

            if (mobId == null) mobId = m;
            if (!mobId.equals(m)) continue;

            mask = switch (part) {
                case HEAD -> MinionParts.add(mask, MinionParts.HEAD);
                case BODY -> MinionParts.add(mask, MinionParts.BODY);
                case ARM_LEFT -> MinionParts.add(mask, MinionParts.ARM_L);
                case ARM_RIGHT -> MinionParts.add(mask, MinionParts.ARM_R);
                case LEGS -> MinionParts.add(mask, MinionParts.LEGS);
            };
        }

        if (mobId == null) return null;
        return new MinionBuildPlan(mobId, mask);
    }
}
