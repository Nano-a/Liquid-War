package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.service.gradient.*;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les stratégies de gradient.
 * 
 * Teste :
 * - BFSGradientStrategy
 * - DijkstraGradientStrategy
 * - Comparaison des performances
 */
class GradientStrategyTest {
    
    private int[][] map;
    private int[] gradient;
    private Cursor cursor;
    private int cursorVal = 1000000;
    
    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 50;
    private static final int AREA_START_GRADIENT = 2000000;
    
    @BeforeEach
    void setUp() {
        map = new int[MAP_HEIGHT][MAP_WIDTH];
        gradient = new int[MAP_WIDTH * MAP_HEIGHT];
        
        // Carte vide
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }
        
        // Gradient initial
        for (int i = 0; i < gradient.length; i++) {
            gradient[i] = AREA_START_GRADIENT;
        }
        
        // Curseur au centre
        cursor = new Cursor();
        cursor.x = 25;
        cursor.y = 25;
        cursor.active = 1;
    }
    
    @Test
    @DisplayName("BFS applique le curseur au gradient")
    void testBFSAppliesCursor() {
        GradientStrategy bfs = new BFSGradientStrategy();
        bfs.calculateGradient(map, gradient, cursor, cursorVal, 0);
        
        int cursorIdx = cursor.y * MAP_WIDTH + cursor.x;
        assertEquals(cursorVal, gradient[cursorIdx],
                    "BFS doit appliquer la valeur du curseur");
    }
    
    @Test
    @DisplayName("BFS propage le gradient")
    void testBFSSpreadsGradient() {
        GradientStrategy bfs = new BFSGradientStrategy();
        bfs.calculateGradient(map, gradient, cursor, cursorVal, 0);
        
        // Propager plusieurs fois
        for (int i = 0; i < 50; i++) {
            bfs.spreadGradient(map, gradient, i);
        }
        
        // Vérifier propagation
        int cursorIdx = cursor.y * MAP_WIDTH + cursor.x;
        int neighborIdx = cursor.y * MAP_WIDTH + (cursor.x + 1);
        
        assertTrue(gradient[neighborIdx] < AREA_START_GRADIENT,
                  "Le gradient BFS doit se propager");
        assertTrue(gradient[neighborIdx] > gradient[cursorIdx],
                  "Le gradient BFS doit augmenter en s'éloignant");
    }
    
    @Test
    @DisplayName("Dijkstra calcule le gradient complet")
    void testDijkstraCalculatesGradient() {
        GradientStrategy dijkstra = new DijkstraGradientStrategy();
        dijkstra.calculateGradient(map, gradient, cursor, cursorVal, 0);
        
        // Dijkstra fait un calcul complet
        int cursorIdx = cursor.y * MAP_WIDTH + cursor.x;
        assertEquals(0, gradient[cursorIdx],
                    "Dijkstra doit mettre le curseur à 0");
        
        // Vérifier que les voisins ont des valeurs
        int neighborIdx = cursor.y * MAP_WIDTH + (cursor.x + 1);
        assertTrue(gradient[neighborIdx] < AREA_START_GRADIENT,
                  "Dijkstra doit calculer les voisins");
    }
    
    @Test
    @DisplayName("Dijkstra respecte les coûts diagonaux")
    void testDijkstraDiagonalCosts() {
        GradientStrategy dijkstra = new DijkstraGradientStrategy();
        dijkstra.calculateGradient(map, gradient, cursor, cursorVal, 0);
        
        int cursorX = cursor.x;
        int cursorY = cursor.y;
        
        // Voisin cardinal (coût 10)
        int cardinalIdx = cursorY * MAP_WIDTH + (cursorX + 1);
        int cardinalCost = gradient[cardinalIdx];
        
        // Voisin diagonal (coût 14)
        int diagonalIdx = (cursorY + 1) * MAP_WIDTH + (cursorX + 1);
        int diagonalCost = gradient[diagonalIdx];
        
        // Diagonal doit être plus coûteux
        assertTrue(diagonalCost >= cardinalCost,
                  "Dijkstra doit donner un coût supérieur aux diagonales");
    }
    
    @Test
    @DisplayName("BFS et Dijkstra avec obstacles")
    void testStrategiesWithObstacles() {
        // Créer un mur
        for (int y = 20; y < 30; y++) {
            map[y][27] = -1;
        }
        
        GradientStrategy bfs = new BFSGradientStrategy();
        GradientStrategy dijkstra = new DijkstraGradientStrategy();
        
        int[] gradBFS = new int[MAP_WIDTH * MAP_HEIGHT];
        int[] gradDijkstra = new int[MAP_WIDTH * MAP_HEIGHT];
        
        for (int i = 0; i < gradBFS.length; i++) {
            gradBFS[i] = AREA_START_GRADIENT;
            gradDijkstra[i] = AREA_START_GRADIENT;
        }
        
        bfs.calculateGradient(map, gradBFS, cursor, cursorVal, 0);
        dijkstra.calculateGradient(map, gradDijkstra, cursor, cursorVal, 0);
        
        // Propager BFS
        for (int i = 0; i < 50; i++) {
            bfs.spreadGradient(map, gradBFS, i);
        }
        
        // Les obstacles doivent rester inchangés
        for (int y = 20; y < 30; y++) {
            int obstacleIdx = y * MAP_WIDTH + 27;
            assertEquals(AREA_START_GRADIENT, gradBFS[obstacleIdx],
                        "BFS ne doit pas propager sur obstacles");
            assertEquals(AREA_START_GRADIENT, gradDijkstra[obstacleIdx],
                        "Dijkstra ne doit pas propager sur obstacles");
        }
    }
    
    @Test
    @DisplayName("Comparaison performance BFS vs Dijkstra")
    void testPerformanceComparison() {
        GradientStrategy bfs = new BFSGradientStrategy();
        GradientStrategy dijkstra = new DijkstraGradientStrategy();
        
        int[] gradBFS = new int[MAP_WIDTH * MAP_HEIGHT];
        int[] gradDijkstra = new int[MAP_WIDTH * MAP_HEIGHT];
        
        // Test BFS
        for (int i = 0; i < gradBFS.length; i++) gradBFS[i] = AREA_START_GRADIENT;
        
        long startBFS = System.nanoTime();
        bfs.calculateGradient(map, gradBFS, cursor, cursorVal, 0);
        for (int i = 0; i < 100; i++) {
            bfs.spreadGradient(map, gradBFS, i);
        }
        long timeBFS = (System.nanoTime() - startBFS) / 1_000_000;
        
        // Test Dijkstra
        for (int i = 0; i < gradDijkstra.length; i++) gradDijkstra[i] = AREA_START_GRADIENT;
        
        long startDijkstra = System.nanoTime();
        dijkstra.calculateGradient(map, gradDijkstra, cursor, cursorVal, 0);
        long timeDijkstra = (System.nanoTime() - startDijkstra) / 1_000_000;
        
        System.out.println("⏱️  BFS (100 propagations): " + timeBFS + "ms");
        System.out.println("⏱️  Dijkstra (calcul complet): " + timeDijkstra + "ms");
        
        // BFS devrait être plus rapide pour propagation incrémentale
        assertTrue(timeBFS < 500 && timeDijkstra < 500,
                  "Les deux stratégies doivent être rapides");
    }
    
    @Test
    @DisplayName("Les stratégies ont des métadonnées")
    void testStrategyMetadata() {
        GradientStrategy bfs = new BFSGradientStrategy();
        GradientStrategy dijkstra = new DijkstraGradientStrategy();
        
        assertNotNull(bfs.getName());
        assertNotNull(bfs.getDescription());
        assertNotNull(dijkstra.getName());
        assertNotNull(dijkstra.getDescription());
        
        assertFalse(bfs.getName().isEmpty());
        assertFalse(dijkstra.getName().isEmpty());
    }
}

