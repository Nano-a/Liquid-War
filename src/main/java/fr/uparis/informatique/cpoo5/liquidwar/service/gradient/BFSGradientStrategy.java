package fr.uparis.informatique.cpoo5.liquidwar.service.gradient;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.util.DirectionTables;

/**
 * Implémentation BFS (Breadth-First Search) du calcul de gradient.
 * 
 * Algorithme simple et rapide basé sur le code C original.
 * Propage le gradient de manière uniforme depuis le curseur.
 * 
 * Complexité : O(n) où n = nombre de cellules
 * Mémoire : O(1) (propagation in-place)
 * 
 * @author Liquid War Team
 */
public class BFSGradientStrategy implements GradientStrategy {
    
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final int AREA_START_GRADIENT = GameConfig.AREA_START_GRADIENT;
    
    @Override
    public void calculateGradient(int[][] map, int[] gradient, Cursor cursor, 
                                  int cursorVal, int globalClock) {
        if (map == null || map.length == 0 || map[0] == null) {
            return;
        }
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        
        // Appliquer le curseur au gradient
        if (cursor != null && cursor.active != 0) {
            int x = cursor.x;
            int y = cursor.y;
            
            if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight && map[y][x] != -1) {
                int idx = y * mapWidth + x;
                if (idx >= 0 && idx < gradient.length) {
                    gradient[idx] = cursorVal;
                }
            }
        }
    }
    
    @Override
    public void spreadGradient(int[][] map, int[] gradient, int globalClock) {
        if (map == null || map.length == 0 || map[0] == null) {
            return;
        }
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        
        // Propagation dans UNE SEULE direction (rotation lente)
        // Crée l'effet de vagues naturelles
        int dir = (globalClock * 7) % 12;
        
        // Calculer les offsets pour cette direction
        int dx = DirectionTables.DIR_MOVE_X[0][dir];
        int dy = DirectionTables.DIR_MOVE_Y[0][dir];
        
        // Déterminer l'ordre de parcours selon la direction
        boolean reverse = (dir >= 6);
        
        if (reverse) {
            // Parcourir de la fin vers le début
            for (int y = mapHeight - 1; y >= 0; y--) {
                for (int x = mapWidth - 1; x >= 0; x--) {
                    propagateCell(x, y, dx, dy, map, gradient, mapWidth, mapHeight);
                }
            }
        } else {
            // Parcourir du début vers la fin
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    propagateCell(x, y, dx, dy, map, gradient, mapWidth, mapHeight);
                }
            }
        }
    }
    
    /**
     * Propage le gradient d'une cellule à ses voisins.
     */
    private void propagateCell(int x, int y, int dx, int dy, int[][] map, int[] gradient, int mapWidth, int mapHeight) {
        if (map[y][x] == -1) {
            return; // Obstacle
        }
        
        int idx = y * mapWidth + x;
        int nx = x + dx;
        int ny = y + dy;
        
        // Vérifier si le voisin existe et est valide
        if (nx >= 0 && nx < mapWidth && ny >= 0 && ny < mapHeight && map[ny][nx] != -1) {
            int neighborIdx = ny * mapWidth + nx;
            if (idx >= 0 && idx < gradient.length && neighborIdx >= 0 && neighborIdx < gradient.length) {
                int currentGrad = gradient[idx];
                int neighborGrad = gradient[neighborIdx];
                
                // Si le voisin a un gradient valide, propager
                if (neighborGrad < AREA_START_GRADIENT) {
                    int newGrad = neighborGrad + 1;
                    if (newGrad < currentGrad) {
                        gradient[idx] = newGrad;
                    }
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "BFS (Breadth-First Search)";
    }
    
    @Override
    public String getDescription() {
        return "Propagation uniforme depuis le curseur. Rapide et simple, basé sur le code C original.";
    }
}

