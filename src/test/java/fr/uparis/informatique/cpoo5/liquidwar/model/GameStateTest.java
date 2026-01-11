package fr.uparis.informatique.cpoo5.liquidwar.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;

/**
 * Tests unitaires pour GameState.
 * 
 * Teste :
 * - Validation des positions
 * - Détection des murs
 * - Accès thread-safe aux données
 * - Gestion des locks
 */
class GameStateTest {

    private GameState gameState;
    private int[][] map;
    private int[][] gradient;
    private ArrayList<Fighter> fighters;
    private Cursor[] cursors;
    private int[] teamFighterCount;
    private static final int ACTIVE_TEAMS = 2;
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;

    @BeforeEach
    void setUp() {
        map = new int[MAP_HEIGHT][MAP_WIDTH];
        gradient = new int[ACTIVE_TEAMS][MAP_WIDTH * MAP_HEIGHT];
        fighters = new ArrayList<>();
        cursors = new Cursor[ACTIVE_TEAMS];
        teamFighterCount = new int[ACTIVE_TEAMS];

        // Initialiser la carte
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }

        // Créer un obstacle
        map[25][25] = -1;

        // Initialiser les gradients
        for (int team = 0; team < ACTIVE_TEAMS; team++) {
            for (int i = 0; i < gradient[team].length; i++) {
                gradient[team][i] = GameConfig.AREA_START_GRADIENT;
            }
        }

        // Créer des curseurs
        for (int team = 0; team < ACTIVE_TEAMS; team++) {
            cursors[team] = new Cursor();
            cursors[team].x = 20 + team * 10;
            cursors[team].y = 20;
            cursors[team].team = team;
            cursors[team].active = 1;
        }

        gameState = new GameState(map, gradient, fighters, cursors, teamFighterCount, ACTIVE_TEAMS);
    }

    @Test
    @DisplayName("isValid vérifie les limites")
    void testIsValid() {
        assertTrue(gameState.isValid(0, 0), "Position (0,0) doit être valide");
        assertTrue(gameState.isValid(GameConfig.MAP_WIDTH - 1, GameConfig.MAP_HEIGHT - 1),
                "Position (max, max) doit être valide");
        assertFalse(gameState.isValid(-1, 0), "Position négative X doit être invalide");
        assertFalse(gameState.isValid(0, -1), "Position négative Y doit être invalide");
        assertFalse(gameState.isValid(GameConfig.MAP_WIDTH, 0), "Position hors limites X doit être invalide");
        assertFalse(gameState.isValid(0, GameConfig.MAP_HEIGHT), "Position hors limites Y doit être invalide");
    }

    @Test
    @DisplayName("isWall détecte les obstacles")
    void testIsWall() {
        assertTrue(gameState.isWall(25, 25), "Position avec obstacle doit être un mur");
        assertFalse(gameState.isWall(10, 10), "Position libre ne doit pas être un mur");
        assertTrue(gameState.isWall(-1, 0), "Position invalide doit être considérée comme mur");
    }

    @Test
    @DisplayName("getMapValue retourne la valeur correcte")
    void testGetMapValue() {
        assertEquals(0, gameState.getMapValue(10, 10), "Position libre doit retourner 0");
        assertEquals(-1, gameState.getMapValue(25, 25), "Position obstacle doit retourner -1");
        assertEquals(-1, gameState.getMapValue(-1, 0), "Position invalide doit retourner -1");
    }

    @Test
    @DisplayName("Locks gradient fonctionnent")
    void testGradientLocks() {
        // Test lock lecture
        gameState.lockGradientRead(0);
        try {
            // Utiliser des coordonnées valides
            int x = Math.min(10, GameConfig.MAP_WIDTH - 1);
            int y = Math.min(10, GameConfig.MAP_HEIGHT - 1);
            int value = gameState.getGradient(0, x, y);
            assertTrue(value >= 0, "Le gradient doit être accessible");
        } finally {
            gameState.unlockGradientRead(0);
        }

        // Test lock écriture
        gameState.lockGradientWrite(0);
        try {
            // Utiliser des coordonnées valides
            int x = Math.min(10, GameConfig.MAP_WIDTH - 1);
            int y = Math.min(10, GameConfig.MAP_HEIGHT - 1);
            gameState.setGradient(0, x, y, 1000);
            assertEquals(1000, gameState.getGradient(0, x, y),
                    "Le gradient doit être modifiable");
        } finally {
            gameState.unlockGradientWrite(0);
        }
    }

    @Test
    @DisplayName("Locks fighters fonctionnent")
    void testFightersLocks() {
        Fighter fighter = new Fighter(10, 10, 0);

        gameState.lockFightersWrite();
        try {
            gameState.getFighters().add(fighter);
            assertEquals(1, gameState.getFighters().size(),
                    "Le fighter doit être ajouté");
        } finally {
            gameState.unlockFightersWrite();
        }

        gameState.lockFightersRead();
        try {
            assertTrue(gameState.getFighters().contains(fighter),
                    "Le fighter doit être accessible en lecture");
        } finally {
            gameState.unlockFightersRead();
        }
    }

    @Test
    @DisplayName("getCursorPosition retourne la position correcte")
    void testGetCursorPosition() {
        gameState.lockCursorsRead();
        try {
            int x = gameState.getCursorPosition(0).x;
            int y = gameState.getCursorPosition(0).y;
            assertEquals(cursors[0].x, x, "X du curseur doit être correct");
            assertEquals(cursors[0].y, y, "Y du curseur doit être correct");
        } finally {
            gameState.unlockCursorsRead();
        }
    }

    @Test
    @DisplayName("getFighterCount retourne le compteur correct")
    void testGetFighterCount() {
        teamFighterCount[0] = 100;
        teamFighterCount[1] = 200;

        // Utiliser la méthode thread-safe
        assertEquals(100, gameState.getFighterCount(0),
                "Le compteur équipe 0 doit être correct");
        assertEquals(200, gameState.getFighterCount(1),
                "Le compteur équipe 1 doit être correct");
    }

    @Test
    @DisplayName("Map est immuable après construction")
    void testMapImmutability() {
        // Modifier la map originale
        map[10][10] = -1;

        // La map dans GameState ne doit pas être modifiée
        assertEquals(0, gameState.getMapValue(10, 10),
                "La map dans GameState ne doit pas être affectée");
    }
}
