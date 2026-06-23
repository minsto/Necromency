package com.mickdev.necromency.registry;

import com.mickdev.necromency.NecromencyConfig;

import java.util.List;

/**
 * Liste des joueurs ayant droit à la faux spéciale (port de {@code Necromancy.specialFolk}).
 *
 * <p>L'ancienne version téléchargeait une liste distante depuis Dropbox au démarrage.
 * C'est retiré volontairement :
 * <ul>
 *   <li>l'URL Dropbox {@code /u/} n'existe plus depuis ~2016 (appel toujours en échec) ;</li>
 *   <li>un appel réseau bloquant pendant le setup peut figer le boot d'un serveur dédié ;</li>
 *   <li>une connexion sortante non divulguée peut être refusée par CurseForge/Modrinth.</li>
 * </ul>
 * La liste est désormais entièrement locale : noms intégrés + liste de config.
 */
public final class SpecialFolk {

    /** Noms intégrés par défaut (hommage à l'auteur d'origine de Necromancy). */
    private static final List<String> BUILT_IN = List.of("AtomicStryker");

    private SpecialFolk() {}

    public static boolean isSpecial(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;

        for (String name : BUILT_IN) {
            if (name.equalsIgnoreCase(playerName)) return true;
        }
        for (String name : NecromencyConfig.SPECIAL_SCYTHE_PLAYERS.get()) {
            if (name.equalsIgnoreCase(playerName)) return true;
        }
        return false;
    }
}
