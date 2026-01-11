package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Mesh;

/**
 * Moteur de propagation du gradient OPTIMISÉ - Reproduction fidèle du code C.
 * 
 * Cette classe reproduit EXACTEMENT l'algorithme de grad.c
 * (spread_single_gradient)
 * en utilisant la structure MESH pour obtenir la même fluidité que le C.
 * 
 * DIFFÉRENCES CLÉS avec PathfindingEngine.java :
 * 1. Utilise structure MESH au lieu de tableaux 2D
 * 2. Parcours séquentiel du tableau MESH (excellent cache)
 * 3. Accès O(1) aux voisins via mesh.neighbors[]
 * 4. Code identique à grad.c (lignes 135-237)
 * 
 * RÉSULTAT : Vagues concentriques naturelles comme dans le C ! 🌊
 */
public class OptimizedGradientEngine {

    private static final int NB_DIRS = 12;
    private static final int AREA_START_GRADIENT = GameConfig.AREA_START_GRADIENT;
    private static final int CURSOR_START_GRADIENT = GameConfig.CURSOR_START_GRADIENT;

    // Directions (correspondant au code C)
    private static final int DIR_NNE = 0;
    private static final int DIR_NE = 1;
    private static final int DIR_ENE = 2;
    private static final int DIR_ESE = 3;
    private static final int DIR_SE = 4;
    private static final int DIR_SSE = 5;
    private static final int DIR_SSW = 6;
    private static final int DIR_SW = 7;
    private static final int DIR_WSW = 8;
    private static final int DIR_WNW = 9;
    private static final int DIR_NW = 10;
    private static final int DIR_NNW = 11;

    /**
     * Réinitialise tous les gradients à AREA_START_GRADIENT.
     * 
     * Appelé au début du jeu ou lors d'un reset.
     * 
     * @param meshArray   Tableau de MESH
     * @param activeTeams Nombre d'équipes actives
     */
    public static void resetAllGradients(Mesh[] meshArray, int activeTeams) {
        for (Mesh mesh : meshArray) {
            for (int team = 0; team < activeTeams; team++) {
                mesh.teamInfo[team].resetGradient();
            }
        }
    }

    /**
     * Applique les curseurs au gradient (comme apply_all_cursor dans cursor.c).
     * 
     * Met à jour SEULEMENT la position des curseurs dans le gradient.
     * 
     * @param meshArray   Tableau de MESH
     * @param cursors     Curseurs de chaque équipe
     * @param cursorVal   Valeur du gradient au curseur pour chaque équipe
     * @param activeTeams Nombre d'équipes actives
     */
    public static void applyAllCursors(Mesh[] meshArray, Cursor[] cursors,
            int[] cursorVal, int activeTeams) {
        for (int team = 0; team < activeTeams; team++) {
            if (cursors[team] == null || cursors[team].active == 0) {
                continue;
            }

            int cx = cursors[team].x;
            int cy = cursors[team].y;

            // Trouver le MESH à la position du curseur
            for (Mesh mesh : meshArray) {
                if (mesh.x == cx && mesh.y == cy) {
                    // Appliquer la valeur du curseur
                    mesh.teamInfo[team].gradient = cursorVal[team];
                    break;
                }
            }
        }
    }

    /**
     * ⭐ FONCTION CLÉ : Propagation du gradient en vagues (spread_single_gradient).
     * 
     * REPRODUCTION EXACTE de grad.c (lignes 135-237).
     * 
     * PRINCIPE :
     * - Propage dans UNE SEULE direction à la fois
     * - La direction tourne progressivement : dir = (globalClock * 7) % 12
     * - Crée des vagues concentriques naturelles ! 🌊
     * 
     * @param meshArray   Tableau de MESH (parcours séquentiel = cache-friendly)
     * @param activeTeams Nombre d'équipes actives
     * @param globalClock Horloge globale
     */
    public static void spreadSingleGradient(Mesh[] meshArray, int activeTeams, int globalClock) {

        long startTime = System.nanoTime();

        // ⭐ FORMULE MAGIQUE : (GLOBAL_CLOCK * 7) % 12
        // Comme dans grad.c ligne 141
        int dir = (globalClock * 7) % NB_DIRS;

        // Déterminer l'ordre de parcours selon la direction
        // (comme dans le switch du code C)
        boolean reverse = isReverseDirection(dir);

        // LOG DEBUG : Vérifier que le gradient se propage bien
        if (globalClock % 240 == 0) {
            System.out.println("🌊 [OptimizedGradient] Clock=" + globalClock +
                    " dir=" + dir + " (" + getDirName(dir) + ") " +
                    (reverse ? "⬆ reverse" : "⬇ forward"));

            // Vérifier la couverture du gradient
            int minGrad = Integer.MAX_VALUE;
            int maxGrad = Integer.MIN_VALUE;
            int countValid = 0;

            for (Mesh mesh : meshArray) {
                for (int team = 0; team < activeTeams; team++) {
                    int grad = mesh.teamInfo[team].gradient;
                    if (grad < AREA_START_GRADIENT) {
                        minGrad = Math.min(minGrad, grad);
                        maxGrad = Math.max(maxGrad, grad);
                        countValid++;
                    }
                }
            }

            System.out.println("   📊 Gradient: min=" + minGrad + " max=" + maxGrad +
                    " cellules_valides=" + countValid + "/" + (meshArray.length * activeTeams));
        }

        if (reverse) {
            // Parcourir de la fin vers le début (directions 6-11)
            // Comme dans le code C : pos = CURRENT_MESH + SIZE - 1; pos--
            for (int i = meshArray.length - 1; i >= 0; i--) {
                propagateFromMesh(meshArray[i], dir, activeTeams);
            }
        } else {
            // Parcourir du début vers la fin (directions 0-5)
            // Comme dans le code C : pos = CURRENT_MESH; pos++
            for (int i = 0; i < meshArray.length; i++) {
                propagateFromMesh(meshArray[i], dir, activeTeams);
            }
        }

        // NOTE : Log désactivé car il ralentit le jeu (12-15ms juste pour afficher !)
        // long endTime = System.nanoTime();
        // long durationMs = (endTime - startTime) / 1_000_000;
        // if (durationMs > 5) {
        // System.out.println("⚠️ [GRADIENT] Propagation a pris " + durationMs + "ms");
        // }
    }

    /**
     * Propage le gradient depuis un MESH vers son voisin dans une direction.
     * 
     * Correspond aux lignes 181-186 de grad.c :
     * if ((temp = (pos->link[dir])) &&
     * (temp->info[i].state.grad > (new_grad = pos->info[i].state.grad +
     * pos->side.size)))
     * temp->info[i].state.grad = new_grad;
     * 
     * @param mesh        MESH source
     * @param dir         Direction de propagation
     * @param activeTeams Nombre d'équipes
     */
    private static void propagateFromMesh(Mesh mesh, int dir, int activeTeams) {
        // Obtenir le voisin dans cette direction (O(1) avec MESH !)
        Mesh neighbor = mesh.getNeighbor(dir);

        if (neighbor == null) {
            return; // Pas de voisin (mur ou bord)
        }

        // Propager pour chaque équipe
        for (int team = 0; team < activeTeams; team++) {
            int currentGrad = mesh.teamInfo[team].gradient;
            int neighborGrad = neighbor.teamInfo[team].gradient;

            // Si le gradient actuel est valide (pas AREA_START_GRADIENT)
            if (currentGrad < AREA_START_GRADIENT) {
                // Calculer nouveau gradient : gradient actuel + taille cellule
                int newGrad = currentGrad + mesh.sideSize;

                // Si c'est mieux que le gradient actuel du voisin, mettre à jour
                if (newGrad < neighborGrad) {
                    neighbor.teamInfo[team].gradient = newGrad;
                }
            }
        }
    }

    /**
     * Détermine si la direction nécessite un parcours inverse.
     * 
     * Comme dans grad.c (switch case) :
     * - Directions 0-5 (NE, E, SE, S, SW, W) → parcours normal
     * - Directions 6-11 (NW, N, NE) → parcours inverse
     */
    private static boolean isReverseDirection(int dir) {
        switch (dir) {
            case DIR_ENE: // 2
            case DIR_ESE: // 3
            case DIR_SE: // 4
            case DIR_SSE: // 5
            case DIR_SSW: // 6
            case DIR_SW: // 7
                return false; // Parcours normal

            case DIR_WSW: // 8
            case DIR_WNW: // 9
            case DIR_NW: // 10
            case DIR_NNW: // 11
            case DIR_NNE: // 0
            case DIR_NE: // 1
                return true; // Parcours inverse

            default:
                return false;
        }
    }

    /**
     * Nom de la direction pour le debug.
     */
    private static String getDirName(int dir) {
        switch (dir) {
            case DIR_NNE:
                return "NNE";
            case DIR_NE:
                return "NE";
            case DIR_ENE:
                return "ENE";
            case DIR_ESE:
                return "ESE";
            case DIR_SE:
                return "SE";
            case DIR_SSE:
                return "SSE";
            case DIR_SSW:
                return "SSW";
            case DIR_SW:
                return "SW";
            case DIR_WSW:
                return "WSW";
            case DIR_WNW:
                return "WNW";
            case DIR_NW:
                return "NW";
            case DIR_NNW:
                return "NNW";
            default:
                return "???";
        }
    }

    /**
     * Met à jour la position du curseur dans chaque MESH (comme dans move.c).
     * 
     * Marque les cellules proches du curseur pour mouvement direct.
     * 
     * @param meshArray   Tableau de MESH
     * @param cursors     Curseurs
     * @param activeTeams Nombre d'équipes
     * @param globalClock Horloge globale
     */
    public static void updateCursorPositions(Mesh[] meshArray, Cursor[] cursors,
            int activeTeams, int globalClock) {
        for (int team = 0; team < activeTeams; team++) {
            if (cursors[team] == null || cursors[team].active == 0) {
                continue;
            }

            int cx = cursors[team].x;
            int cy = cursors[team].y;
            int radiusSq = GameConfig.CURSOR_PROXIMITY_RADIUS * GameConfig.CURSOR_PROXIMITY_RADIUS;

            // Mettre à jour pour toutes les cellules
            for (Mesh mesh : meshArray) {
                Mesh.MeshInfo info = mesh.teamInfo[team];

                // Mettre à jour position curseur
                info.updateCursorPosition(cx, cy);

                // Calculer distance au curseur
                int dx = mesh.x - cx;
                int dy = mesh.y - cy;
                int distSq = dx * dx + dy * dy;

                // Marquer comme proche ou loin
                if (distSq <= radiusSq) {
                    info.updateTime = globalClock; // Proche → mouvement direct
                } else if (info.updateTime >= 0) {
                    info.updateTime = -globalClock; // Loin → suivre gradient
                }
            }
        }
    }
}
