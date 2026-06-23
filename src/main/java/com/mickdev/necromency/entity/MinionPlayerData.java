package com.mickdev.necromency.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Données persistantes joueur (équivalent 1.12 NBT {@code aggressive}, relations ami/ennemi, compteur minions).
 */
public final class MinionPlayerData {

    private static final String ROOT = "necromency_minion";
    private static final String AGGRESSIVE = "aggressive";
    private static final String MINION_COUNT = "minions";

    public enum Relation {
        NONE(""),
        FRIEND("friend"),
        ENEMY("enemy");

        public final String id;

        Relation(String id) {
            this.id = id;
        }

        static Relation fromId(String id) {
            if ("friend".equals(id)) return FRIEND;
            if ("enemy".equals(id)) return ENEMY;
            return NONE;
        }
    }

    private MinionPlayerData() {}

    private static CompoundTag root(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ROOT)) {
            data.put(ROOT, new CompoundTag());
        }
        return data.getCompound(ROOT).orElse(new CompoundTag());
    }

    private static void saveRoot(Player player, CompoundTag tag) {
        player.getPersistentData().put(ROOT, tag);
    }

    public static boolean isAggressive(Player player) {
        return root(player).getBoolean(AGGRESSIVE).orElse(false);
    }

    public static void setAggressive(Player player, boolean aggressive) {
        CompoundTag tag = root(player);
        tag.putBoolean(AGGRESSIVE, aggressive);
        saveRoot(player, tag);
    }

    public static int getMinionCount(Player player) {
        return root(player).getInt(MINION_COUNT).orElse(0);
    }

    public static void setMinionCount(Player player, int count) {
        CompoundTag tag = root(player);
        tag.putInt(MINION_COUNT, Math.max(0, count));
        saveRoot(player, tag);
    }

    public static void incrementMinionCount(Player player) {
        setMinionCount(player, getMinionCount(player) + 1);
    }

    public static void decrementMinionCount(Player player) {
        setMinionCount(player, Math.max(0, getMinionCount(player) - 1));
    }

    public static void setRelation(Player owner, String otherPlayerName, Relation relation) {
        CompoundTag tag = root(owner);
        if (relation == Relation.NONE) {
            tag.remove(otherPlayerName);
        } else {
            tag.putString(otherPlayerName, relation.id);
        }
        saveRoot(owner, tag);
    }

    public static Relation getRelation(Player owner, String otherPlayerName) {
        return Relation.fromId(root(owner).getString(otherPlayerName).orElse(""));
    }

    /** Comme 1.12 : ennemi explicite, ou neutre + mode agressif. */
    public static boolean shouldMinionAttackPlayer(Player owner, Player candidate) {
        if (owner == null || candidate == null) return false;
        if (owner.getUUID().equals(candidate.getUUID())) return false;

        Relation rel = getRelation(owner, candidate.getName().getString());
        if (rel == Relation.FRIEND) return false;
        if (rel == Relation.ENEMY) return true;
        return isAggressive(owner);
    }
}
