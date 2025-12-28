package fr.uparis.informatique.cpoo5.liquidwar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import main.java.fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;

/**
 * Moteur de combat pour Liquid War.
 * 
 * IMPORTANT : Code extrait de GameCanvas.java (lignes 686-721) pour réduire la
 * taille du fichier.
 * Le code est déplacé TEL QUEL sans modification de la logique.
 * 
 * Responsabilités :
 * - Vérification des combats entre combattants de différentes équipes
 * - Application des dégâts
 * - Suppression des combattants morts
 */
public class CombatEngine {

    private static final int ATTACK_DAMAGE = GameConfig.ATTACK_DAMAGE;

    // Empêcher l'instanciation
    private CombatEngine() {
    }

    /**
     * Vérifie et résout tous les combats - COMME DANS LE CODE C ORIGINAL
     * 
     * IMPORTANT : Dans Liquid War, les particules NE MEURENT PAS !
     * Elles CHANGENT DE CAMP quand leur santé tombe à 0.
     * C'est pour ça que le nombre total reste toujours constant (4000).
     * 
     * @param fighters         Liste des combattants
     * @param teamFighterCount Compteurs de combattants par équipe
     * @return Liste vide (pas de suppression, juste changement de camp)
     */
    public static List<Fighter> checkCombat(ArrayList<Fighter> fighters, int[] teamFighterCount) {
        // Créer une grille pour détecter les combats
        HashMap<String, Fighter> grid = new HashMap<>();

        int combatsDetected = 0; // Pour debug

        for (Fighter f : fighters) {
            String key = f.x + "," + f.y;
            Fighter existing = grid.get(key);

            if (existing != null && existing.team != f.team) {
                // COMBAT ! Deux particules de camps différents au même endroit
                combatsDetected++;

                // ⚔️ COMBAT MUTUEL : Les DEUX particules s'attaquent !
                existing.health -= ATTACK_DAMAGE;
                f.health -= ATTACK_DAMAGE;

                // Vérifier si la particule "existing" change de camp
                if (existing.health < 0) {
                    // Décrémenter l'ancien camp
                    teamFighterCount[existing.team]--;

                    // CHANGEMENT DE CAMP (comme dans le code C)
                    existing.team = f.team;

                    // Restaurer la santé avec celle du nouveau camp
                    while (existing.health < 0) {
                        existing.health += GameConfig.FIGHTER_INITIAL_HEALTH;
                    }

                    // Incrémenter le nouveau camp
                    teamFighterCount[existing.team]++;
                }

                // Vérifier si la particule actuelle (f) change de camp
                if (f.health < 0) {
                    // Décrémenter l'ancien camp
                    teamFighterCount[f.team]--;

                    // CHANGEMENT DE CAMP vers le camp de "existing"
                    f.team = existing.team;

                    // Restaurer la santé
                    while (f.health < 0) {
                        f.health += GameConfig.FIGHTER_INITIAL_HEALTH;
                    }

                    // Incrémenter le nouveau camp
                    teamFighterCount[f.team]++;
                }

                // Toujours ajouter 'f' à la grille car il est maintenant à cette position
                grid.put(key, f);
            } else {
                grid.put(key, f);
            }
        }

        // Log debug (toutes les 200 frames = ~1 seconde)
        if (combatsDetected > 0 && fighters.size() > 0) {
            Fighter f0 = fighters.get(0);
            if (f0 != null && f0.team >= 0) {
                // Log seulement occasionnellement
                int randomCheck = (int) (Math.random() * 200);
                if (randomCheck == 0) {
                    System.out.println("⚔️ [Combat] " + combatsDetected + " combats détectés");
                }
            }
        }

        // Retourner une liste vide car les particules ne meurent JAMAIS
        // Elles changent juste de camp !
        return new ArrayList<>();
    }
}
