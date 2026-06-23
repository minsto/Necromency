package com.mickdev.necromency.Client.Util;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Sur un modèle vanilla (proxy), n’affiche que les morceaux correspondant au slot du minion
 * (ex. poulet : seulement les ailes pour les bras, pas le corps ni la tête).
 */
public final class ProxySlotVisibility {

    public enum Slot {
        HEAD,
        BODY,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG
    }

    private ProxySlotVisibility() {}

    public static void applyToModel(EntityModel<?> model, ResourceLocation mobId, Slot slot) {
        Collection<Field> fields = modelPartFields(model);
        for (Field f : fields) {
            ModelPart mp = readPart(model, f);
            if (mp != null) {
                mp.visible = false;
            }
        }
        Set<String> show = visibleFieldNames(mobId, slot);
        if (show.isEmpty()) {
            return;
        }
        for (Field f : fields) {
            if (!show.contains(normalizeName(f.getName()))) {
                continue;
            }
            ModelPart mp = readPart(model, f);
            if (mp != null) {
                mp.visible = true;
            }
        }
    }

    /** Morceaux visibles après {@link #applyToModel} (champs top-level du modèle). */
    public static void collectVisibleTopLevelParts(EntityModel<?> model, Collection<ModelPart> out) {
        for (Field f : modelPartFields(model)) {
            ModelPart mp = readPart(model, f);
            if (mp != null && mp.visible) {
                out.add(mp);
            }
        }
    }

    public static boolean hasAnyVisibleTopLevelPart(EntityModel<?> model) {
        for (Field f : modelPartFields(model)) {
            ModelPart mp = readPart(model, f);
            if (mp != null && mp.visible) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> visibleFieldNames(ResourceLocation mobId, Slot slot) {
        if (mobId == null || !"minecraft".equals(mobId.getNamespace())) {
            return Set.of();
        }
        String path = mobId.getPath();
        return switch (path) {
            case "chicken" -> chickenNames(slot);
            case "zombie" -> humanoidNames(slot);
            case "villager" -> villagerNames(slot);
            case "camel" -> camelNames(slot);
            default -> defaultSinglePartNames(slot);
        };
    }

    private static Set<String> chickenNames(Slot slot) {
        return switch (slot) {
            case HEAD -> names("head", "beak", "bill", "chin", "redthing", "wattle", "red_thing");
            case BODY -> names("body");
            case LEFT_ARM -> names("leftwing", "left_wing");
            case RIGHT_ARM -> names("rightwing", "right_wing");
            case LEFT_LEG -> names("leftleg", "left_leg");
            case RIGHT_LEG -> names("rightleg", "right_leg");
        };
    }

    private static Set<String> humanoidNames(Slot slot) {
        return switch (slot) {
            case HEAD -> names("head");
            case BODY -> names("body");
            case LEFT_ARM -> names("leftarm", "left_arm");
            case RIGHT_ARM -> names("rightarm", "right_arm");
            case LEFT_LEG -> names("leftleg", "left_leg");
            case RIGHT_LEG -> names("rightleg", "right_leg");
        };
    }

    /** Vanilla villager : un seul bloc {@code arms} ; les deux slots bras ciblent le même ModelPart (dédoublonné côté renderer). */
    private static Set<String> villagerNames(Slot slot) {
        return switch (slot) {
            case HEAD -> names("head", "nose", "hat", "hood");
            case BODY -> names("body", "jacket");
            case LEFT_ARM, RIGHT_ARM -> names("arms");
            case LEFT_LEG -> names("leftleg", "left_leg");
            case RIGHT_LEG -> names("rightleg", "right_leg");
        };
    }

    /** Dromadaire vanilla (Mojang : leftFrontLeg, rightHindLeg, etc.). */
    private static Set<String> camelNames(Slot slot) {
        return switch (slot) {
            case HEAD -> names("head", "leftear", "rightear", "left_ear", "right_ear");
            case BODY -> names("body", "hump");
            case LEFT_ARM -> names("leftfrontleg", "frontleftleg", "front_left_leg");
            case RIGHT_ARM -> names("rightfrontleg", "frontrightleg", "front_right_leg");
            case LEFT_LEG -> names("lefthindleg", "backleftleg", "back_left_leg");
            case RIGHT_LEG -> names("righthindleg", "backrightleg", "back_right_leg");
        };
    }

    /** Fallback : un seul nom canonique par slot (camelCase Mojang). */
    private static Set<String> defaultSinglePartNames(Slot slot) {
        return switch (slot) {
            case HEAD -> names("head");
            case BODY -> names("body");
            case LEFT_ARM -> names("leftarm", "left_arm", "frontleftleg", "front_left_leg");
            case RIGHT_ARM -> names("rightarm", "right_arm", "frontrightleg", "front_right_leg");
            case LEFT_LEG -> names("leftleg", "left_leg", "backleftleg", "back_left_leg");
            case RIGHT_LEG -> names("rightleg", "right_leg", "backrightleg", "back_right_leg");
        };
    }

    private static Set<String> names(String... n) {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        for (String x : n) {
            s.add(normalizeName(x));
        }
        return s;
    }

    private static String normalizeName(String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    private static Collection<Field> modelPartFields(EntityModel<?> model) {
        HashSet<Field> out = new HashSet<>();
        for (Class<?> c = model.getClass(); c != null && EntityModel.class.isAssignableFrom(c); c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!ModelPart.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                f.setAccessible(true);
                out.add(f);
            }
        }
        return out;
    }

    @Nullable
    private static ModelPart readPart(EntityModel<?> model, Field f) {
        try {
            Object v = f.get(model);
            return v instanceof ModelPart mp ? mp : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
