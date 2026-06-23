package com.mickdev.necromency.Client.Util;
public record PartProfile(
        float scale,
        float offX, float offY, float offZ,
        float rotX, float rotY, float rotZ
) {
    public static PartProfile identity() {
        return new PartProfile(1f, 0,0,0, 0,0,0);
    }
}