package com.mickdev.necromency.necroapi.NecroAPIRemake;

public final class MinionParts {
    private MinionParts() {}

    public static final byte HEAD = 1;
    public static final byte BODY = 2;
    public static final byte ARM_L = 4;
    public static final byte ARM_R = 8;
    public static final byte LEGS = 16;

    public static boolean has(byte mask, byte part) {
        return (mask & part) != 0;
    }

    public static byte add(byte mask, byte part) {
        return (byte) (mask | part);
    }
}
