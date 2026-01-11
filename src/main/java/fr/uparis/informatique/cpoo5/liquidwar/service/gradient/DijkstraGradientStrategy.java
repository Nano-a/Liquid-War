package fr.uparis.informatique.cpoo5.liquidwar.service.gradient;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;

import java.util.PriorityQueue;

/**
 * Implémentation Dijkstra du calcul de gradient.
 * 
 * Algorithme plus précis que BFS, prend en compte les coûts variables.
 * Utile pour des cartes avec terrain difficile ou zones spéciales.
 * 
 * Complexité : O(n log n) où n = nombre de cellules
 * Mémoire : O(n) (priority queue)
 * 
 * @author Liquid War Team
 */
public class DijkstraGradientStrategy implements GradientStrategy {
    
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final int AREA_START_GRADIENT = GameConfig.AREA_START_GRADIENT;
    
    // Directions 8-connectées pour Dijkstra
    private static final int[] DX = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] DY = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] COST = {14, 10, 14, 10, 10, 14, 10, 14}; // Diagonal=14, Cardinal=10
    
    /**
     * Classe interne pour représenter une cellule dans la priority queue.
     */
    private static class Cell implements Comparable<Cell> {
        int x, y, cost;
        
        Cell(int x, int y, int cost) {
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
        
        @Override
        public int compareTo(Cell other) {
            return Integer.compare(this.cost, other.cost);
        }
    }
    
    @Override
    public void calculateGradient(int[][] map, int[] gradient, Cursor cursor, 
                                  int cursorVal, int globalClock) {
        if (map == null || map.length == 0 || map[0] == null) {
            return;
        }
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        
        if (cursor == null || cursor.active == 0) {
            return;
        }
        
        int startX = cursor.x;
        int startY = cursor.y;
        
        if (startX < 0 || startX >= mapWidth || startY < 0 || startY >= mapHeight) {
            return;
        }
        
        if (map[startY][startX] == -1) {
            return; // Curseur sur un obstacle
        }
        
        // Réinitialiser le gradient
        for (int i = 0; i < gradient.length; i++) {
            gradient[i] = AREA_START_GRADIENT;
        }
        
        // Dijkstra depuis le curseur
        PriorityQueue<Cell> pq = new PriorityQueue<>();
        boolean[] visited = new boolean[mapWidth * mapHeight];
        
        int startIdx = startY * mapWidth + startX;
        if (startIdx >= 0 && startIdx < gradient.length) {
            gradient[startIdx] = 0;
            pq.offer(new Cell(startX, startY, 0));
        }
        
        while (!pq.isEmpty()) {
            Cell current = pq.poll();
            int idx = current.y * mapWidth + current.x;
            
            if (idx < 0 || idx >= visited.length || visited[idx]) {
                continue;
            }
            visited[idx] = true;
            
            // Explorer les 8 voisins
            for (int i = 0; i < 8; i++) {
                int nx = current.x + DX[i];
                int ny = current.y + DY[i];
                
                if (nx < 0 || nx >= mapWidth || ny < 0 || ny >= mapHeight) {
                    continue;
                }
                
                if (map[ny][nx] == -1) {
                    continue; // Obstacle
                }
                
                int neighborIdx = ny * mapWidth + nx;
                if (neighborIdx < 0 || neighborIdx >= visited.length || visited[neighborIdx]) {
                    continue;
                }
                
                if (neighborIdx >= 0 && neighborIdx < gradient.length) {
                    int newCost = current.cost + COST[i];
                    
                    if (newCost < gradient[neighborIdx]) {
                        gradient[neighborIdx] = newCost;
                        pq.offer(new Cell(nx, ny, newCost));
                    }
                }
            }
        }
    }
    
    @Override
    public void spreadGradient(int[][] map, int[] gradient, int globalClock) {
        // Dijkstra fait un calcul complet, pas de propagation incrémentale
        // Cette méthode n'est pas utilisée pour Dijkstra
    }
    
    @Override
    public String getName() {
        return "Dijkstra";
    }
    
    @Override
    public String getDescription() {
        return "Calcul optimal du plus court chemin avec coûts variables. Plus précis mais plus lent que BFS.";
    }
}

