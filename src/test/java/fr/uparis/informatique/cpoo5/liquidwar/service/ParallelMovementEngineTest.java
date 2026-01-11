package fr.uparis.informatique.cpoo5.liquidwar.service;

import static fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;

/**
 * Tests unitaires pour ParallelMovementEngine.
 * 
 * Teste :
 * - Parallélisation du mouvement
 * - Conservation du nombre de fighters
 * - Gestion des collisions
 */
class ParallelMovementEngineTest {

    private ArrayList<Fighter> fighters;
    private Cursor[] cursors;
    private int[] teamFighterCount;
    private int[][] map;
    private int[][] gradient;
    private int[][] updateTime;
    private int[][] cursorPosX;
    private int[][] cursorPosY;
    private int[] LOCAL_DIR;
    private int[][][] FIGHTER_MOVE_DIR;
    private int[][][] FIGHTER_MOVE_X_ALT;
    private int[][][] FIGHTER_MOVE_Y_ALT;
    private int[][] DIR_MOVE_X;
    private int[][] DIR_MOVE_Y;

    @BeforeEach
    void setUp() {
        fighters = new ArrayList<>();
        cursors = new Cursor[2];
        teamFighterCount = new int[] { 0, 0 };

        map = new int[MAP_HEIGHT][MAP_WIDTH];
        gradient = new int[2][MAP_WIDTH * MAP_HEIGHT];
        updateTime = new int[6][GameConfig.MAP_WIDTH * GameConfig.MAP_HEIGHT];
        cursorPosX = new int[6][GameConfig.MAP_WIDTH * GameConfig.MAP_HEIGHT];
        cursorPosY = new int[6][GameConfig.MAP_WIDTH * GameConfig.MAP_HEIGHT];

        // Initialiser la carte
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }

        // Initialiser les gradients
        for (int team = 0; team < 2; team++) {
            for (int i = 0; i < gradient[team].length; i++) {
                gradient[team][i] = GameConfig.AREA_START_GRADIENT;
            }
        }

        // Initialiser les curseurs
        for (int team = 0; team < 2; team++) {
            cursors[team] = new Cursor();
            cursors[team].x = 25 + team * 10;
            cursors[team].y = 25;
            cursors[team].team = team;
            cursors[team].active = 1;
        }

        // Initialiser les tables de mouvement
        LOCAL_DIR = new int[GameConfig.NB_LOCAL_DIRS * 2];
        FIGHTER_MOVE_DIR = new int[2][12][GameConfig.NB_TRY_MOVE];
        FIGHTER_MOVE_X_ALT = new int[2][12][GameConfig.NB_TRY_MOVE];
        FIGHTER_MOVE_Y_ALT = new int[2][12][GameConfig.NB_TRY_MOVE];
        DIR_MOVE_X = new int[2][12];
        DIR_MOVE_Y = new int[2][12];

        // Initialiser avec des valeurs de base
        GameInitializer.initMoveFighters(LOCAL_DIR, FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT,
                FIGHTER_MOVE_Y_ALT, DIR_MOVE_X, DIR_MOVE_Y,
                updateTime, cursorPosX, cursorPosY);
    }

    @Test
    @DisplayName("moveFightersParallel ne crash pas avec liste vide")
    void testEmptyList() {
        // Liste vide
        ParallelMovementEngine.moveFightersParallel(fighters, cursors, teamFighterCount,
                map, gradient, updateTime, cursorPosX, cursorPosY,
                LOCAL_DIR, FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT,
                FIGHTER_MOVE_Y_ALT, DIR_MOVE_X, DIR_MOVE_Y, 0);

        // Doit se terminer sans erreur
        assertEquals(0, fighters.size());
    }

    @Test
    @DisplayName("moveFightersParallel conserve le nombre de fighters")
    void testConservesFighterCount() {
        // Créer plusieurs fighters
        for (int i = 0; i < 20; i++) {
            fighters.add(new Fighter(20 + i % 10, 20 + i / 10, i % 2));
            teamFighterCount[i % 2]++;
        }

        int initialCount = fighters.size();
        int initialTeam0 = teamFighterCount[0];
        int initialTeam1 = teamFighterCount[1];

        ParallelMovementEngine.moveFightersParallel(fighters, cursors, teamFighterCount,
                map, gradient, updateTime, cursorPosX, cursorPosY,
                LOCAL_DIR, FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT,
                FIGHTER_MOVE_Y_ALT, DIR_MOVE_X, DIR_MOVE_Y, 0);

        // Le nombre total doit rester constant
        assertEquals(initialCount, fighters.size(), "Le nombre total doit rester constant");
        assertEquals(initialTeam0 + initialTeam1, teamFighterCount[0] + teamFighterCount[1],
                "Le nombre par équipe doit rester constant");
    }

    @Test
    @DisplayName("moveFightersParallel respecte les limites de la carte")
    void testRespectsMapBounds() {
        // Créer un fighter
        Fighter fighter = new Fighter(25, 25, 0);
        fighters.add(fighter);
        teamFighterCount[0] = 1;

        ParallelMovementEngine.moveFightersParallel(fighters, cursors, teamFighterCount,
                map, gradient, updateTime, cursorPosX, cursorPosY,
                LOCAL_DIR, FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT,
                FIGHTER_MOVE_Y_ALT, DIR_MOVE_X, DIR_MOVE_Y, 0);

        // Le fighter doit être dans les limites
        assertTrue(fighter.x >= 0 && fighter.x < MAP_WIDTH, "X doit être dans les limites");
        assertTrue(fighter.y >= 0 && fighter.y < MAP_HEIGHT, "Y doit être dans les limites");
    }

    @Test
    @DisplayName("moveFightersParallel gère plusieurs fighters en parallèle")
    void testMultipleFightersParallel() {
        // Créer plusieurs fighters
        for (int i = 0; i < 50; i++) {
            fighters.add(new Fighter(20 + i % 10, 20 + i / 10, i % 2));
            teamFighterCount[i % 2]++;
        }

        int initialCount = fighters.size();

        // Exécuter plusieurs fois
        for (int i = 0; i < 10; i++) {
            ParallelMovementEngine.moveFightersParallel(fighters, cursors, teamFighterCount,
                    map, gradient, updateTime, cursorPosX, cursorPosY,
                    LOCAL_DIR, FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT,
                    FIGHTER_MOVE_Y_ALT, DIR_MOVE_X, DIR_MOVE_Y, i);
        }

        // Le nombre doit rester constant
        assertEquals(initialCount, fighters.size(), "Le nombre doit rester constant après plusieurs mouvements");
    }
}
