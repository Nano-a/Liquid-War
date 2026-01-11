package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PathfindingEngine.
 * 
 * Teste :
 * - Application des curseurs au gradient
 * - Propagation du gradient
 * - Gestion des obstacles
 * - Mise à jour des positions curseurs
 */
class PathfindingEngineTest {
    
    private int[][] map;
    private int[][] gradient;
    private Cursor[] cursors;
    private int[] cursorVal;
    private int[][] cursorPosX;
    private int[][] cursorPosY;
    private int[][] updateTime;
    
    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 50;
    private static final int AREA_START_GRADIENT = 2000000;
    
    @BeforeEach
    void setUp() {
        // Initialiser une petite carte pour les tests
        map = new int[MAP_HEIGHT][MAP_WIDTH];
        gradient = new int[2][MAP_WIDTH * MAP_HEIGHT];
        cursors = new Cursor[2];
        cursorVal = new int[]{1000000, 1000000};
        cursorPosX = new int[2][MAP_WIDTH * MAP_HEIGHT];
        cursorPosY = new int[2][MAP_WIDTH * MAP_HEIGHT];
        updateTime = new int[2][MAP_WIDTH * MAP_HEIGHT];
        
        // Carte vide (pas d'obstacles)
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }
        
        // Initialiser les gradients
        for (int team = 0; team < 2; team++) {
            for (int i = 0; i < gradient[team].length; i++) {
                gradient[team][i] = AREA_START_GRADIENT;
                cursorPosX[team][i] = -1;
                cursorPosY[team][i] = -1;
                updateTime[team][i] = -1;
            }
        }
        
        // Créer des curseurs
        cursors[0] = new Cursor();
        cursors[0].x = 25;
        cursors[0].y = 25;
        cursors[0].team = 0;
        cursors[0].active = 1;
        
        cursors[1] = new Cursor();
        cursors[1].x = 10;
        cursors[1].y = 10;
        cursors[1].team = 1;
        cursors[1].active = 1;
    }
    
    @Test
    @DisplayName("Application des curseurs au gradient")
    void testApplyAllCursors() {
        // Appliquer les curseurs
        PathfindingEngine.applyAllCursors(cursors, 2, map, gradient, cursorVal);
        
        // Vérifier que la valeur du curseur est appliquée
        int idx0 = cursors[0].y * MAP_WIDTH + cursors[0].x;
        int idx1 = cursors[1].y * MAP_WIDTH + cursors[1].x;
        
        assertEquals(cursorVal[0], gradient[0][idx0], 
                    "La valeur du curseur doit être appliquée pour l'équipe 0");
        assertEquals(cursorVal[1], gradient[1][idx1], 
                    "La valeur du curseur doit être appliquée pour l'équipe 1");
    }
    
    @Test
    @DisplayName("Propagation du gradient vers les cellules voisines")
    void testSpreadSingleGradient() {
        // Appliquer les curseurs d'abord
        PathfindingEngine.applyAllCursors(cursors, 2, map, gradient, cursorVal);
        
        // Propager le gradient plusieurs fois
        for (int i = 0; i < 50; i++) {
            PathfindingEngine.spreadSingleGradient(map, gradient, 2, i);
        }
        
        // Vérifier que le gradient s'est propagé autour du curseur
        int cursorIdx = cursors[0].y * MAP_WIDTH + cursors[0].x;
        int neighborIdx = cursors[0].y * MAP_WIDTH + (cursors[0].x + 1);
        
        assertTrue(gradient[0][neighborIdx] < AREA_START_GRADIENT,
                  "Le gradient doit s'être propagé aux voisins");
        assertTrue(gradient[0][neighborIdx] > gradient[0][cursorIdx],
                  "Le gradient doit augmenter en s'éloignant du curseur");
    }
    
    @Test
    @DisplayName("Gestion des obstacles dans le gradient")
    void testGradientWithObstacles() {
        // Placer un obstacle au milieu
        map[25][26] = -1;
        
        // Appliquer et propager
        PathfindingEngine.applyAllCursors(cursors, 2, map, gradient, cursorVal);
        
        for (int i = 0; i < 50; i++) {
            PathfindingEngine.spreadSingleGradient(map, gradient, 2, i);
        }
        
        // L'obstacle doit rester à AREA_START_GRADIENT
        int obstacleIdx = 25 * MAP_WIDTH + 26;
        assertEquals(AREA_START_GRADIENT, gradient[0][obstacleIdx],
                    "L'obstacle ne doit pas avoir de gradient");
    }
    
    @Test
    @DisplayName("Mise à jour des positions curseurs")
    void testUpdateCursorPositions() {
        PathfindingEngine.updateCursorPositions(cursors, 2, map, 
                                               cursorPosX, cursorPosY, 
                                               updateTime, 0);
        
        // Vérifier que toutes les cellules connaissent la position du curseur
        int testIdx = 10 * MAP_WIDTH + 10;
        assertEquals(cursors[0].x, cursorPosX[0][testIdx],
                    "Toutes les cellules doivent connaître la position X du curseur");
        assertEquals(cursors[0].y, cursorPosY[0][testIdx],
                    "Toutes les cellules doivent connaître la position Y du curseur");
    }
    
    @Test
    @DisplayName("Test de performance - propagation gradient")
    void testGradientPropagationPerformance() {
        PathfindingEngine.applyAllCursors(cursors, 2, map, gradient, cursorVal);
        
        long startTime = System.nanoTime();
        
        // 100 propagations
        for (int i = 0; i < 100; i++) {
            PathfindingEngine.spreadSingleGradient(map, gradient, 2, i);
        }
        
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // en ms
        
        System.out.println("⏱️  100 propagations: " + duration + "ms");
        
        // Doit être rapide (< 500ms pour 100 propagations sur 50x50)
        assertTrue(duration < 500,
                  "La propagation doit être rapide (< 500ms pour 100 passes)");
    }
    
    @Test
    @DisplayName("Gradient décroît depuis le curseur")
    void testGradientDecreasesFromCursor() {
        PathfindingEngine.applyAllCursors(cursors, 2, map, gradient, cursorVal);
        
        // Propager suffisamment
        for (int i = 0; i < 100; i++) {
            PathfindingEngine.spreadSingleGradient(map, gradient, 2, i);
        }
        
        int cursorX = cursors[0].x;
        int cursorY = cursors[0].y;
        
        // Vérifier que le gradient augmente en s'éloignant
        int[] distances = {1, 2, 3, 4, 5};
        int previousGrad = gradient[0][cursorY * MAP_WIDTH + cursorX];
        
        for (int dist : distances) {
            int idx = cursorY * MAP_WIDTH + (cursorX + dist);
            int currentGrad = gradient[0][idx];
            
            assertTrue(currentGrad >= previousGrad,
                      "Le gradient doit augmenter ou rester constant en s'éloignant");
            previousGrad = currentGrad;
        }
    }
}

