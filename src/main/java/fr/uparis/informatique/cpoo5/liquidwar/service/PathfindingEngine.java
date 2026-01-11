package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.util.DirectionTables;

/**
 * Moteur de pathfinding pour Liquid War.
 * 
 * IMPORTANT : Code extrait de GameCanvas.java (lignes 401-532) pour réduire la taille du fichier.
 * Le code est déplacé TEL QUEL sans modification de la logique.
 * 
 * Responsabilités :
 * - Application des curseurs au gradient
 * - Mise à jour des positions des curseurs dans les cellules
 * - Propagation du gradient (algorithme de pathfinding)
 */
public class PathfindingEngine {
    
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final int AREA_START_GRADIENT = GameConfig.AREA_START_GRADIENT;
    private static final int CURSOR_START_GRADIENT = GameConfig.CURSOR_START_GRADIENT;
    
    // Empêcher l'instanciation
    private PathfindingEngine() {}
    
    /**
     * Appliquer les curseurs au gradient (comme apply_all_cursor dans cursor.c)
     * 
     * IMPORTANT : Cette méthode met à jour SEULEMENT la position des curseurs,
     * elle ne réinitialise PAS tout le gradient. Le gradient se propage
     * progressivement sur plusieurs ticks, comme dans le code C.
     * 
     * RESTAURÉ : Version identique à la v9 qui fonctionnait bien.
     */
    public static void applyAllCursors(Cursor[] cursors, int activeTeams, int[][] map, 
                                       int[][] gradient, int[] cursorVal) {
        if (map == null || map.length == 0 || map[0] == null) {
            return;
        }
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        
        for (int team = 0; team < activeTeams; team++) {
            if (cursors[team] == null || cursors[team].active == 0)
                continue;

            int x = cursors[team].x;
            int y = cursors[team].y;

            // Mettre la valeur du curseur directement dans le gradient à sa position
            if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight && map[y][x] != -1) {
                int idx = y * mapWidth + x;
                gradient[team][idx] = cursorVal[team];
            }
        }
    }
    
    /**
     * Appliquer un seul curseur au gradient (version simplifiée de applyAllCursors)
     * 
     * @deprecated Utiliser applyAllCursors() à la place pour la cohérence avec la v9
     */
    @Deprecated
    public static void applyCursor(int[] gradient, Cursor cursor, int cursorVal) {
        // Appliquer le curseur si actif (comme apply_all_cursor dans cursor.c ligne 179)
        if (cursor != null && cursor.active != 0) {
            int x = cursor.x;
            int y = cursor.y;
            
            // Calculer les dimensions à partir de la taille du gradient
            // Le gradient est un tableau 1D de taille MAP_WIDTH * MAP_HEIGHT
            int mapWidth = (int) Math.sqrt(gradient.length);
            int mapHeight = gradient.length / mapWidth;
            
            if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
                int idx = y * mapWidth + x;
                if (idx >= 0 && idx < gradient.length) {
                    gradient[idx] = cursorVal; // Mettre à jour seulement la position du curseur
                }
            }
        }
    }
    
    /**
     * Réinitialiser le gradient pour une équipe (utilisé seulement au démarrage)
     * 
     * @deprecated Utiliser applyAllCursors() à la place pour mettre à jour seulement les curseurs
     */
    @Deprecated
    public static void resetGradient(int[] gradient, Cursor cursor, 
                                     int[] cursorPosX, int[] cursorPosY, int cursorVal) {
        // Réinitialiser à AREA_START_GRADIENT
        for (int i = 0; i < gradient.length; i++) {
            gradient[i] = AREA_START_GRADIENT;
        }
        
        // Appliquer le curseur
        applyCursor(gradient, cursor, cursorVal);
    }
    
    /**
     * Mettre à jour la position du curseur dans chaque cellule (comme dans move.c)
     */
    public static void updateCursorPositions(Cursor[] cursors, int activeTeams, int[][] map,
                                            int[][] cursorPosX, int[][] cursorPosY, 
                                            int[][] updateTime, int globalClock) {
        if (map == null || map.length == 0 || map[0] == null) {
            return;
        }
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        
        for (int team = 0; team < activeTeams; team++) {
            if (cursors[team] == null || cursors[team].active == 0)
                continue;

            int cx = cursors[team].x;
            int cy = cursors[team].y;

            // Mettre à jour la position du curseur dans TOUTES les cellules (comme dans le
            // code C)
            // Le code C met à jour la position du curseur dans chaque cellule du mesh
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    if (map[y][x] != -1) {
                        int idx = y * mapWidth + x;
                        if (idx < cursorPosX[team].length && idx < cursorPosY[team].length && idx < updateTime[team].length) {
                            cursorPosX[team][idx] = cx;
                            cursorPosY[team][idx] = cy;

                            // Marquer comme proche du curseur si dans un rayon défini
                            int dx = x - cx;
                            int dy = y - cy;
                            int distSq = dx * dx + dy * dy;
                            int radiusSq = GameConfig.CURSOR_PROXIMITY_RADIUS * GameConfig.CURSOR_PROXIMITY_RADIUS;
                            if (distSq <= radiusSq) {
                                updateTime[team][idx] = globalClock; // Proche du curseur
                            } else if (updateTime[team][idx] >= 0) {
                                // Réinitialiser si trop loin
                                updateTime[team][idx] = -1;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Propager le gradient de manière incrémentale (comme spread_single_gradient dans grad.c)
     * 
     * CODE C ORIGINAL RESTAURÉ : La propagation se fait dans UNE SEULE direction à la fois,
     * créant des vagues naturelles comme un liquide. C'est CORRECT !
     * 
     * IMPORTANT : Utiliser (globalClock * 7) comme dans le code C pour un mouvement fluide.
     */
    public static void spreadSingleGradient(int[][] map, int[][] gradient, int activeTeams, int globalClock) {
        if (map == null || map.length == 0 || map[0] == null) {
            return;
        }
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        
        // IMPORTANT : Comme dans le code C, on propage dans UNE SEULE direction à la fois !
        // Cela crée des "vagues" de propagation qui donnent l'effet liquide
        // (globalClock * 7) % 12 = rotation lente → mouvement fluide et naturel
        int dir = (globalClock * 7) % 12;  // RESTAURÉ : comme dans le code C et la v9

        // Calculer les offsets pour cette direction
        int dx = DirectionTables.DIR_MOVE_X[0][dir];
        int dy = DirectionTables.DIR_MOVE_Y[0][dir];
        
        // LOG : Vérifier la propagation (toutes les 2 secondes)
        if (globalClock % 240 == 0) {
            // Compter combien de cellules ont un gradient valide (pas AREA_START_GRADIENT)
            int[] validGradCount = new int[activeTeams];
            for (int team = 0; team < activeTeams; team++) {
                for (int i = 0; i < gradient[team].length; i++) {
                    if (gradient[team][i] < AREA_START_GRADIENT) {
                        validGradCount[team]++;
                    }
                }
            }
            System.out.println("🌊 [spreadSingleGradient] Clock=" + globalClock + " dir=" + dir + 
                             " | Gradients valides: Team0=" + validGradCount[0] + " Team1=" + validGradCount[1]);
        }

        // Déterminer l'ordre de parcours selon la direction (comme dans le code C)
        // Directions 6-11 sont parcourues en reverse
        boolean reverse = (dir >= 6);

        // Parcourir toutes les cellules dans l'ordre approprié
        if (reverse) {
            // Parcourir de la fin vers le début
            for (int y = mapHeight - 1; y >= 0; y--) {
                for (int x = mapWidth - 1; x >= 0; x--) {
                    if (map[y][x] == -1)
                        continue; // Obstacle

                    int idx = y * mapWidth + x;
                    int nx = x + dx;
                    int ny = y + dy;

                    // Vérifier si la cellule voisine dans cette direction existe
                    if (nx >= 0 && nx < mapWidth && ny >= 0 && ny < mapHeight && map[ny][nx] != -1) {
                        int neighborIdx = ny * mapWidth + nx;

                        // Propager le gradient pour chaque équipe
                        for (int team = 0; team < activeTeams; team++) {
                            if (idx < gradient[team].length && neighborIdx < gradient[team].length) {
                                int currentGrad = gradient[team][idx];
                                int neighborGrad = gradient[team][neighborIdx];

                                // Si le voisin a un gradient valide, propager
                                // side.size = 1 pour simplifier (dans le code C, ça peut varier)
                                if (neighborGrad < AREA_START_GRADIENT) {
                                    int newGrad = neighborGrad + 1;
                                    if (newGrad < currentGrad) {
                                        gradient[team][idx] = newGrad;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Parcourir du début vers la fin
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    if (map[y][x] == -1)
                        continue; // Obstacle

                    int idx = y * mapWidth + x;
                    int nx = x + dx;
                    int ny = y + dy;

                    // Vérifier si la cellule voisine dans cette direction existe
                    if (nx >= 0 && nx < mapWidth && ny >= 0 && ny < mapHeight && map[ny][nx] != -1) {
                        int neighborIdx = ny * mapWidth + nx;

                        // Propager le gradient pour chaque équipe
                        for (int team = 0; team < activeTeams; team++) {
                            if (idx < gradient[team].length && neighborIdx < gradient[team].length) {
                                int currentGrad = gradient[team][idx];
                                int neighborGrad = gradient[team][neighborIdx];

                                // Si le voisin a un gradient valide, propager
                                if (neighborGrad < AREA_START_GRADIENT) {
                                    int newGrad = neighborGrad + 1;
                                    if (newGrad < currentGrad) {
                                        gradient[team][idx] = newGrad;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

