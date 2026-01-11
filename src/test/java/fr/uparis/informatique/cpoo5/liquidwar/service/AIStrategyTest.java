package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.service.ai.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

/**
 * Tests unitaires pour les stratégies d'IA.
 * 
 * Teste :
 * - RandomAI (mouvement aléatoire)
 * - DefensiveAI (défense du centre)
 * - AggressiveAI (attaque agressive)
 */
class AIStrategyTest {
    
    private static class MockGameState implements AIStrategy.GameState {
        private Point cursor0 = new Point(100, 100);
        private Point cursor1 = new Point(200, 200);
        private int[] fighterCounts = {1000, 1000};
        private int mapWidth = 281;
        private int mapHeight = 240;
        
        @Override
        public Point getCursorPosition(int team) {
            return team == 0 ? cursor0 : cursor1;
        }
        
        @Override
        public int getFighterCount(int team) {
            return fighterCounts[team];
        }
        
        @Override
        public boolean isValidPosition(int x, int y) {
            return x >= 10 && x < mapWidth - 10 && y >= 10 && y < mapHeight - 10;
        }
        
        @Override
        public boolean isWall(int x, int y) {
            return false; // Pas de murs pour les tests
        }
        
        @Override
        public int getGradient(int team, int x, int y) {
            // Gradient simple : distance au curseur
            Point cursor = getCursorPosition(team);
            return (int) cursor.distance(x, y);
        }
        
        @Override
        public int getMapWidth() {
            return mapWidth;
        }
        
        @Override
        public int getMapHeight() {
            return mapHeight;
        }
    }
    
    @Test
    @DisplayName("RandomAI génère des positions valides")
    void testRandomAI() {
        AIStrategy ai = new RandomAI();
        MockGameState state = new MockGameState();
        
        // Tester 100 fois pour s'assurer que c'est toujours valide
        for (int i = 0; i < 100; i++) {
            Point newPos = ai.calculateNextMove(state, 1);
            
            assertNotNull(newPos, "RandomAI doit retourner une position");
            assertTrue(state.isValidPosition(newPos.x, newPos.y),
                      "La position générée doit être valide");
        }
    }
    
    @Test
    @DisplayName("RandomAI reste proche de la position actuelle")
    void testRandomAIStaysClose() {
        AIStrategy ai = new RandomAI();
        MockGameState state = new MockGameState();
        
        Point initial = state.getCursorPosition(1);
        Point newPos = ai.calculateNextMove(state, 1);
        
        // Distance ne doit pas être trop grande (mouvement limité)
        double distance = initial.distance(newPos);
        assertTrue(distance <= 20, // RANDOM_MOVE_DISTANCE = 15
                  "RandomAI ne doit pas se téléporter loin");
    }
    
    @Test
    @DisplayName("DefensiveAI se dirige vers le centre")
    void testDefensiveAIMovesToCenter() {
        AIStrategy ai = new DefensiveAI();
        MockGameState state = new MockGameState();
        
        // Positionner loin du centre
        state.cursor1 = new Point(10, 10);
        
        Point newPos = ai.calculateNextMove(state, 1);
        
        // Doit se rapprocher du centre
        Point center = new Point(state.getMapWidth() / 2, state.getMapHeight() / 2);
        double initialDist = state.cursor1.distance(center);
        double newDist = newPos.distance(center);
        
        assertTrue(newDist <= initialDist,
                  "DefensiveAI doit se rapprocher du centre");
    }
    
    @Test
    @DisplayName("DefensiveAI patrouille autour du centre")
    void testDefensiveAIPatrols() {
        AIStrategy ai = new DefensiveAI();
        MockGameState state = new MockGameState();
        
        // Positionner au centre
        Point center = new Point(state.getMapWidth() / 2, state.getMapHeight() / 2);
        state.cursor1 = new Point(center.x, center.y);
        
        // Faire plusieurs mouvements
        Point[] positions = new Point[10];
        for (int i = 0; i < 10; i++) {
            positions[i] = ai.calculateNextMove(state, 1);
            state.cursor1 = positions[i];
        }
        
        // Les positions doivent rester proches du centre
        for (Point pos : positions) {
            double dist = pos.distance(center);
            assertTrue(dist < 50, // PATROL_RADIUS + marge
                      "DefensiveAI doit patrouiller près du centre");
        }
    }
    
    @Test
    @DisplayName("AggressiveAI se rapproche de l'ennemi")
    void testAggressiveAIMovesToEnemy() {
        AIStrategy ai = new AggressiveAI();
        MockGameState state = new MockGameState();
        
        // Équipe 1 loin de l'équipe 0
        state.cursor1 = new Point(200, 200);
        state.cursor0 = new Point(50, 50);
        
        Point newPos = ai.calculateNextMove(state, 1);
        
        // Doit se rapprocher de l'ennemi
        double initialDist = state.cursor1.distance(state.cursor0);
        double newDist = newPos.distance(state.cursor0);
        
        assertTrue(newDist <= initialDist,
                  "AggressiveAI doit se rapprocher de l'ennemi");
    }
    
    @Test
    @DisplayName("Toutes les IA ont des noms et descriptions")
    void testAIMetadata() {
        AIStrategy[] strategies = {
            new RandomAI(),
            new DefensiveAI(),
            new AggressiveAI()
        };
        
        for (AIStrategy ai : strategies) {
            assertNotNull(ai.getName(),
                         "Chaque IA doit avoir un nom");
            assertNotNull(ai.getDescription(),
                         "Chaque IA doit avoir une description");
            assertFalse(ai.getName().isEmpty(),
                       "Le nom ne doit pas être vide");
            assertFalse(ai.getDescription().isEmpty(),
                       "La description ne doit pas être vide");
        }
    }
    
    @Test
    @DisplayName("Les IA génèrent des positions différentes")
    void testAIDiversity() {
        AIStrategy random = new RandomAI();
        AIStrategy defensive = new DefensiveAI();
        AIStrategy aggressive = new AggressiveAI();
        
        MockGameState state = new MockGameState();
        state.cursor1 = new Point(150, 120);
        
        Point randomPos = random.calculateNextMove(state, 1);
        Point defensivePos = defensive.calculateNextMove(state, 1);
        Point aggressivePos = aggressive.calculateNextMove(state, 1);
        
        // Les stratégies doivent produire des résultats différents
        // (au moins 2 sur 3 doivent être différents)
        int uniquePositions = 1;
        if (!randomPos.equals(defensivePos)) uniquePositions++;
        if (!randomPos.equals(aggressivePos) && !defensivePos.equals(aggressivePos)) uniquePositions++;
        
        assertTrue(uniquePositions >= 2,
                  "Les différentes IA doivent produire des comportements variés");
    }
}

