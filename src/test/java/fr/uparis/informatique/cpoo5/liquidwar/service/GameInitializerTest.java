package fr.uparis.informatique.cpoo5.liquidwar.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;

/**
 * Tests unitaires pour GameInitializer.
 * 
 * Teste :
 * - Initialisation des tables de mouvement
 * - Spawn initial des fighters
 * - Validation des positions de spawn
 */
class GameInitializerTest {

    private int[] LOCAL_DIR;
    private int[][][] FIGHTER_MOVE_DIR;
    private int[][][] FIGHTER_MOVE_X_ALT;
    private int[][][] FIGHTER_MOVE_Y_ALT;
    private int[][] DIR_MOVE_X;
    private int[][] DIR_MOVE_Y;
    private int[][] updateTime;
    private int[][] cursorPosX;
    private int[][] cursorPosY;
    private ArrayList<Fighter> fighters;
    private int[] teamFighterCount;
    private int[][] map;
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;

    @BeforeEach
    void setUp() {
        LOCAL_DIR = new int[GameConfig.NB_LOCAL_DIRS * 2];
        FIGHTER_MOVE_DIR = new int[2][12][GameConfig.NB_TRY_MOVE];
        FIGHTER_MOVE_X_ALT = new int[2][12][GameConfig.NB_TRY_MOVE];
        FIGHTER_MOVE_Y_ALT = new int[2][12][GameConfig.NB_TRY_MOVE];
        DIR_MOVE_X = new int[2][12];
        DIR_MOVE_Y = new int[2][12];
        updateTime = new int[6][MAP_HEIGHT * MAP_WIDTH];
        cursorPosX = new int[6][MAP_HEIGHT * MAP_WIDTH];
        cursorPosY = new int[6][MAP_HEIGHT * MAP_WIDTH];

        fighters = new ArrayList<>();
        teamFighterCount = new int[] { 0, 0 };

        map = new int[MAP_HEIGHT][MAP_WIDTH];
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }
    }

    @Test
    @DisplayName("initMoveFighters initialise les tables")
    void testInitMoveFighters() {
        GameInitializer.initMoveFighters(LOCAL_DIR, FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT,
                FIGHTER_MOVE_Y_ALT, DIR_MOVE_X, DIR_MOVE_Y,
                updateTime, cursorPosX, cursorPosY);

        // Vérifier que les tables sont initialisées
        assertNotNull(LOCAL_DIR, "LOCAL_DIR doit être initialisé");
        assertNotNull(FIGHTER_MOVE_DIR, "FIGHTER_MOVE_DIR doit être initialisé");
        assertNotNull(FIGHTER_MOVE_X_ALT, "FIGHTER_MOVE_X_ALT doit être initialisé");
        assertNotNull(FIGHTER_MOVE_Y_ALT, "FIGHTER_MOVE_Y_ALT doit être initialisé");

        // Vérifier que updateTime est initialisé à -1
        assertEquals(-1, updateTime[0][0], "updateTime doit être initialisé à -1");
        assertEquals(-1, cursorPosX[0][0], "cursorPosX doit être initialisé à -1");
        assertEquals(-1, cursorPosY[0][0], "cursorPosY doit être initialisé à -1");
    }

    @Test
    @DisplayName("spawnInitialFighters crée des fighters")
    void testSpawnInitialFighters() {
        int count = 100;
        int centerX = MAP_WIDTH / 2;
        int centerY = MAP_HEIGHT / 2;
        int team = 0;

        GameInitializer.spawnInitialFighters(team, centerX, centerY, count,
                fighters, teamFighterCount, map);

        // Vérifier que des fighters ont été créés
        assertTrue(fighters.size() > 0, "Des fighters doivent être créés");
        assertTrue(teamFighterCount[team] > 0, "Le compteur d'équipe doit être incrémenté");

        // Vérifier que tous les fighters sont de la bonne équipe
        for (Fighter f : fighters) {
            assertEquals(team, f.team, "Tous les fighters doivent être de la bonne équipe");
            assertTrue(f.x >= 0 && f.x < MAP_WIDTH, "X doit être dans les limites");
            assertTrue(f.y >= 0 && f.y < MAP_HEIGHT, "Y doit être dans les limites");
        }
    }

    @Test
    @DisplayName("spawnInitialFighters respecte les obstacles")
    void testSpawnRespectsObstacles() {
        // Créer un obstacle au centre
        int centerX = MAP_WIDTH / 2;
        int centerY = MAP_HEIGHT / 2;
        map[centerY][centerX] = -1;

        int count = 50;
        int team = 0;

        GameInitializer.spawnInitialFighters(team, centerX, centerY, count,
                fighters, teamFighterCount, map);

        // Aucun fighter ne doit être sur l'obstacle
        for (Fighter f : fighters) {
            assertNotEquals(-1, map[f.y][f.x], "Aucun fighter ne doit être sur un obstacle");
        }
    }

    @Test
    @DisplayName("spawnInitialFighters évite les superpositions")
    void testSpawnAvoidsOverlaps() {
        int count = 50;
        int centerX = MAP_WIDTH / 2;
        int centerY = MAP_HEIGHT / 2;
        int team = 0;

        GameInitializer.spawnInitialFighters(team, centerX, centerY, count,
                fighters, teamFighterCount, map);

        // Vérifier qu'il n'y a pas de superpositions
        for (int i = 0; i < fighters.size(); i++) {
            for (int j = i + 1; j < fighters.size(); j++) {
                Fighter f1 = fighters.get(i);
                Fighter f2 = fighters.get(j);
                assertFalse(f1.x == f2.x && f1.y == f2.y,
                        "Il ne doit pas y avoir de superpositions");
            }
        }
    }

    @Test
    @DisplayName("spawnInitialFighters limite le nombre de tentatives")
    void testSpawnLimitsAttempts() {
        // Créer une carte presque entièrement remplie d'obstacles
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                if (Math.random() > 0.1) { // 90% d'obstacles
                    map[y][x] = -1;
                }
            }
        }

        int count = 1000; // Nombre élevé
        int centerX = MAP_WIDTH / 2;
        int centerY = MAP_HEIGHT / 2;
        int team = 0;

        GameInitializer.spawnInitialFighters(team, centerX, centerY, count,
                fighters, teamFighterCount, map);

        // Le nombre spawné peut être inférieur au nombre demandé
        assertTrue(fighters.size() <= count, "Le nombre spawné ne doit pas dépasser le nombre demandé");
    }
}
