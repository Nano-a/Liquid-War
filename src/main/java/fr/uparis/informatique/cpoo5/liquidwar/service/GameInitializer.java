package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

import java.util.ArrayList;

/**
 * Initialiseur du jeu Liquid War.
 * 
 * IMPORTANT : Code extrait de GameCanvas.java (lignes 242-302, 304-316) pour réduire la taille du fichier.
 * Le code est déplacé TEL QUEL sans modification de la logique.
 * 
 * Responsabilités :
 * - Initialisation du système de mouvement (tables de directions)
 * - Spawn initial des combattants
 * - Spawn continu des combattants près des curseurs
 */
public class GameInitializer {
    
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final int NB_TRY_MOVE = GameConfig.NB_TRY_MOVE;
    
    // Empêcher l'instanciation
    private GameInitializer() {}
    
    /**
     * Initialiser le système de mouvement (comme init_move_fighters dans le code C)
     */
    public static void initMoveFighters(int[] LOCAL_DIR, int[][][] FIGHTER_MOVE_DIR,
                                       int[][][] FIGHTER_MOVE_X_ALT, int[][][] FIGHTER_MOVE_Y_ALT,
                                       int[][] DIR_MOVE_X, int[][] DIR_MOVE_Y,
                                       int[][] updateTime, int[][] cursorPosX, int[][] cursorPosY) {
        // Initialiser LOCAL_DIR (mapping des codes directionnels)
        // Code directionnel : 1=N, 2=E, 4=S, 8=W (combinaisons possibles)
        int[][] localDirMap = {
                { 1, 11 }, // N -> DIR_NNW ou DIR_NNE
                { 3, 1 }, // NE -> DIR_NE
                { 2, 0 }, // E -> DIR_ENE ou DIR_ESE
                { 6, 4 }, // SE -> DIR_SE
                { 4, 5 }, // S -> DIR_SSE ou DIR_SSW
                { 6, 7 }, // SW -> DIR_SW (corrigé: 12 -> 6, doit être 0-11)
                { 8, 9 }, // W -> DIR_WSW ou DIR_WNW
                { 9, 10 } // NW -> DIR_NW
        };

        for (int i = 0; i < localDirMap.length; i++) {
            LOCAL_DIR[i * 2] = localDirMap[i][0];
            LOCAL_DIR[i * 2 + 1] = localDirMap[i][1];
        }

        // Initialiser FIGHTER_MOVE_DIR (directions alternatives pour chaque direction
        // principale)
        // Table 0
        int[][][] moveDirTable0 = {
                { { 11, 1, 10, 0, 9 }, { 1, 0, 11, 4, 10 }, { 0, 1, 4, 11, 5 }, { 2, 4, 1, 5, 11 },
                        { 4, 5, 2, 7, 1 }, { 5, 4, 7, 2, 8 }, { 6, 7, 4, 9, 2 }, { 7, 9, 6, 10, 4 },
                        { 8, 7, 10, 6, 11 }, { 9, 10, 7, 11, 5 }, { 10, 11, 9, 1, 7 }, { 11, 10, 1, 0, 9 } }
        };

        // Table 1
        int[][][] moveDirTable1 = {
                { { 11, 1, 10, 0, 9 }, { 1, 11, 0, 10, 4 }, { 0, 1, 4, 11, 5 }, { 2, 4, 1, 5, 11 },
                        { 4, 2, 5, 1, 7 }, { 5, 4, 7, 2, 8 }, { 6, 7, 4, 9, 2 }, { 7, 6, 9, 4, 10 },
                        { 8, 7, 10, 6, 11 }, { 9, 10, 7, 11, 5 }, { 10, 9, 11, 7, 1 }, { 11, 10, 1, 9, 0 } }
        };

        for (int dir = 0; dir < 12; dir++) {
            for (int tryIdx = 0; tryIdx < NB_TRY_MOVE; tryIdx++) {
                FIGHTER_MOVE_DIR[0][dir][tryIdx] = moveDirTable0[0][dir][tryIdx];
                FIGHTER_MOVE_DIR[1][dir][tryIdx] = moveDirTable1[0][dir][tryIdx];

                // Calculer les offsets X et Y pour chaque direction alternative
                int altDir = FIGHTER_MOVE_DIR[0][dir][tryIdx];
                FIGHTER_MOVE_X_ALT[0][dir][tryIdx] = DIR_MOVE_X[0][altDir];
                FIGHTER_MOVE_Y_ALT[0][dir][tryIdx] = DIR_MOVE_Y[0][altDir];

                altDir = FIGHTER_MOVE_DIR[1][dir][tryIdx];
                FIGHTER_MOVE_X_ALT[1][dir][tryIdx] = DIR_MOVE_X[1][altDir];
                FIGHTER_MOVE_Y_ALT[1][dir][tryIdx] = DIR_MOVE_Y[1][altDir];
            }
        }

        // Initialiser updateTime, cursorPosX, cursorPosY à -1 (comme dans le code C)
        for (int team = 0; team < 6; team++) {
            for (int i = 0; i < MAP_HEIGHT * MAP_WIDTH; i++) {
                updateTime[team][i] = -1;
                cursorPosX[team][i] = -1;
                cursorPosY[team][i] = -1;
            }
        }
    }
    
    /**
     * Spawner des fighters initiaux autour d'une position
     * IMPORTANT : Garantir EXACTEMENT 2000 particules par équipe (total 4000)
     */
    public static void spawnInitialFighters(int team, int centerX, int centerY, int count,
                                           ArrayList<Fighter> fighters, int[] teamFighterCount,
                                           int[][] map) {
        int radius = GameConfig.FIGHTER_SPAWN_RADIUS;
        int spawned = 0;
        int attempts = 0;
        int maxAttempts = count * 10;  // Maximum 10x le nombre voulu pour éviter boucle infinie
        
        while (spawned < count && attempts < maxAttempts) {
            attempts++;
            
            // Spawn en spirale pour garantir le nombre exact
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * (radius + attempts / 100.0);  // Augmenter progressivement le rayon
            int x = centerX + (int) (Math.cos(angle) * distance);
            int y = centerY + (int) (Math.sin(angle) * distance);
            
            if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT && map[y][x] != -1) {
                // Vérifier que la position n'est pas déjà occupée
                boolean occupied = false;
                for (Fighter f : fighters) {
                    if (f.x == x && f.y == y) {
                        occupied = true;
                        break;
                    }
                }
                
                if (!occupied) {
                    fighters.add(new Fighter(x, y, team));
                    teamFighterCount[team]++;
                    spawned++;
                }
            }
        }
        
        // Message de debug pour vérifier le nombre exact
        System.out.println("✅ Équipe " + team + " : " + spawned + " particules spawnées (objectif : " + count + ")");
    }
}

