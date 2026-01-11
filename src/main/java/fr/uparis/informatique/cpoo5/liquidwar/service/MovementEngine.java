package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Moteur de mouvement pour Liquid War.
 * 
 * IMPORTANT : Code extrait de GameCanvas.java (lignes 534-684) pour réduire la taille du fichier.
 * Le code est déplacé TEL QUEL sans modification de la logique.
 * 
 * Responsabilités :
 * - Calcul de la direction de mouvement (get_close_dir, get_main_dir)
 * - Déplacement des combattants avec directions alternatives
 * - Gestion des collisions
 */
public class MovementEngine {
    
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final int NB_TRY_MOVE = GameConfig.NB_TRY_MOVE;
    private static final int NB_LOCAL_DIRS = GameConfig.NB_LOCAL_DIRS;
    private static final int AREA_START_GRADIENT = GameConfig.AREA_START_GRADIENT;
    
    // Empêcher l'instanciation
    private MovementEngine() {}
    
    /**
     * get_close_dir : Direction directe vers le curseur (comme dans fighter.c)
     */
    public static int getCloseDir(Fighter f, int team, int sens, int start,
                                  int[][] cursorPosX, int[][] cursorPosY, 
                                  Cursor[] cursors, int[] LOCAL_DIR) {
        int idx = f.y * MAP_WIDTH + f.x;
        int cursor_x = cursorPosX[team][idx];
        int cursor_y = cursorPosY[team][idx];
        int fighter_x = f.x;
        int fighter_y = f.y;

        // Si la position du curseur n'est pas initialisée, utiliser le curseur
        // directement
        if (cursor_x < 0 || cursor_y < 0) {
            if (cursors[team] != null && cursors[team].active != 0) {
                cursor_x = cursors[team].x;
                cursor_y = cursors[team].y;
            } else {
                return start; // Pas de curseur, utiliser direction par défaut
            }
        }

        int code_dir = 0;
        if (cursor_y < fighter_y)
            code_dir += 1; // N
        if (cursor_x > fighter_x)
            code_dir += 2; // E
        if (cursor_y > fighter_y)
            code_dir += 4; // S
        if (cursor_x < fighter_x)
            code_dir += 8; // W

        if (code_dir > 0 && code_dir <= NB_LOCAL_DIRS) {
            int localIdx = (code_dir - 1) * 2 + ((sens != 0) ? 1 : 0);
            if (localIdx >= 0 && localIdx < LOCAL_DIR.length) {
                return LOCAL_DIR[localIdx];
            }
        }
        return start;
    }
    
    /**
     * get_main_dir : Direction selon le gradient (comme dans fighter.c)
     */
    public static int getMainDir(int x, int y, int team, int sens, int start,
                                 int[][] map, int[][] gradient, int[][] DIR_MOVE_X, 
                                 int[][] DIR_MOVE_Y, int globalClock) {
        int idx = y * MAP_WIDTH + x;
        int bestDir = -1;
        int bestGrad = AREA_START_GRADIENT;

        int i = start;
        do {
            int nx = x + DIR_MOVE_X[0][i];
            int ny = y + DIR_MOVE_Y[0][i];

            if (nx >= 0 && nx < MAP_WIDTH && ny >= 0 && ny < MAP_HEIGHT && map[ny][nx] != -1) {
                int neighborIdx = ny * MAP_WIDTH + nx;
                int grad = gradient[team][neighborIdx];

                if (grad < bestGrad) {
                    bestGrad = grad;
                    bestDir = i;
                }
            }

            if (sens != 0) {
                i = (i < 11) ? i + 1 : 0;
            } else {
                i = (i > 0) ? i - 1 : 11;
            }
        } while (i != start);

        if (bestDir >= 0) {
            return bestDir;
        }
        return globalClock % 12;
    }
    
    /**
     * Déplacer les fighters selon le gradient (comme move_fighters dans le code C)
     */
    public static void moveFighters(ArrayList<Fighter> fighters, Cursor[] cursors, 
                                   int[] teamFighterCount, int[][] map, int[][] gradient,
                                   int[][] updateTime, int[][] cursorPosX, int[][] cursorPosY,
                                   int[] LOCAL_DIR, int[][][] FIGHTER_MOVE_DIR,
                                   int[][][] FIGHTER_MOVE_X_ALT, int[][][] FIGHTER_MOVE_Y_ALT,
                                   int[][] DIR_MOVE_X, int[][] DIR_MOVE_Y,
                                   int globalClock) {
        ArrayList<Fighter> fightersToUpdate = new ArrayList<>(fighters);
        // MOUVEMENT EN VAGUES : Légère rotation pour effet circulaire naturel
        // Pas complètement fixe = évite le carré, crée des vagues
        int table = 0; // Table fixe pour cohérence
        int startDir = (globalClock / 2) % 12; // Rotation LENTE pour vagues circulaires
        int sens = 0;

        // HashMap pour vérifier les collisions rapidement
        HashMap<String, Fighter> positionMap = new HashMap<>();
        for (Fighter f : fighters) {
            String key = f.x + "," + f.y;
            positionMap.put(key, f);
        }

        for (Fighter f : fightersToUpdate) {
            Cursor target = cursors[f.team];
            if (target == null || target.active == 0) {
                fighters.remove(f);
                teamFighterCount[f.team]--;
                continue;
            }

            int fx = f.x;
            int fy = f.y;
            int idx = fy * MAP_WIDTH + fx;
            int dir;

            // MOUVEMENT EN VAGUES : Proche = direct, Loin = gradient
            // Les vagues se propagent naturellement du curseur vers l'extérieur
            if (updateTime[f.team][idx] >= 0) {
                // Proche du curseur : mouvement direct (centre de la vague)
                dir = getCloseDir(f, f.team, sens, startDir, cursorPosX, cursorPosY, cursors, LOCAL_DIR);
            } else {
                // Loin du curseur : suit le gradient (propagation de la vague)
                dir = getMainDir(fx, fy, f.team, sens, startDir, map, gradient, DIR_MOVE_X, DIR_MOVE_Y, globalClock);
                updateTime[f.team][idx] = -globalClock;
            }

            // Valider que dir est dans les limites (0-11)
            if (dir < 0 || dir >= 12) {
                dir = startDir; // Direction par défaut
            }
            
            // Légère progression pour variété circulaire (évite le carré)
            startDir = (startDir + 1) % 12;

            // Essayer de se déplacer dans la direction principale ou alternatives
            // (NB_TRY_MOVE = 5)
            boolean moved = false;
            for (int tryIdx = 0; tryIdx < NB_TRY_MOVE && !moved; tryIdx++) {
                int altDir = FIGHTER_MOVE_DIR[table][dir][tryIdx];
                // Valider altDir aussi
                if (altDir < 0 || altDir >= 12) {
                    continue; // Passer à la prochaine tentative
                }
                int dx = FIGHTER_MOVE_X_ALT[table][dir][tryIdx];
                int dy = FIGHTER_MOVE_Y_ALT[table][dir][tryIdx];

                int newX = fx + dx;
                int newY = fy + dy;

                if (newX >= 0 && newX < MAP_WIDTH && newY >= 0 && newY < MAP_HEIGHT && map[newY][newX] != -1) {
                    String newKey = newX + "," + newY;
                    if (!positionMap.containsKey(newKey)) {
                        // Déplacer le fighter
                        String oldKey = fx + "," + fy;
                        positionMap.remove(oldKey);
                        positionMap.put(newKey, f);
                        f.x = newX;
                        f.y = newY;
                        moved = true;
                    }
                }
            }
        }
    }
}

