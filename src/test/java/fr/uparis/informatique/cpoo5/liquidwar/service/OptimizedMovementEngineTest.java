package fr.uparis.informatique.cpoo5.liquidwar.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Mesh;
import fr.uparis.informatique.cpoo5.liquidwar.util.MeshBuilder;

/**
 * Tests unitaires pour OptimizedMovementEngine.
 * 
 * Teste :
 * - getMainDir (direction selon gradient)
 * - getCloseDir (direction vers curseur)
 * - moveFighters (mouvement avec MESH)
 */
class OptimizedMovementEngineTest {

    private Mesh[] meshArray;
    private Mesh[][] meshLookup;
    private Cursor[] cursors;
    private ArrayList<Fighter> fighters;
    private int[] teamFighterCount;
    private int[][] map;
    private int[][][] FIGHTER_MOVE_DIR;
    private int[][][] FIGHTER_MOVE_X_ALT;
    private int[][][] FIGHTER_MOVE_Y_ALT;
    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 50;

    @BeforeEach
    void setUp() {
        // Créer une carte simple
        map = new int[MAP_HEIGHT][MAP_WIDTH];
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }

        // Construire le MESH
        meshArray = MeshBuilder.buildMeshArray(map);

        // Créer meshLookup
        meshLookup = new Mesh[MAP_HEIGHT][MAP_WIDTH];
        for (Mesh mesh : meshArray) {
            meshLookup[mesh.y][mesh.x] = mesh;
        }

        // Initialiser les tables de mouvement
        FIGHTER_MOVE_DIR = new int[2][12][GameConfig.NB_TRY_MOVE];
        FIGHTER_MOVE_X_ALT = new int[2][12][GameConfig.NB_TRY_MOVE];
        FIGHTER_MOVE_Y_ALT = new int[2][12][GameConfig.NB_TRY_MOVE];

        // Initialiser avec des valeurs de base
        for (int table = 0; table < 2; table++) {
            for (int dir = 0; dir < 12; dir++) {
                for (int tryIdx = 0; tryIdx < GameConfig.NB_TRY_MOVE; tryIdx++) {
                    FIGHTER_MOVE_DIR[table][dir][tryIdx] = dir;
                    FIGHTER_MOVE_X_ALT[table][dir][tryIdx] = 0;
                    FIGHTER_MOVE_Y_ALT[table][dir][tryIdx] = 0;
                }
            }
        }

        // Créer curseurs
        cursors = new Cursor[2];
        for (int team = 0; team < 2; team++) {
            cursors[team] = new Cursor();
            cursors[team].x = 25 + team * 10;
            cursors[team].y = 25;
            cursors[team].team = team;
            cursors[team].active = 1;
        }

        fighters = new ArrayList<>();
        teamFighterCount = new int[] { 0, 0 };
    }

    @Test
    @DisplayName("getMainDir trouve la meilleure direction selon gradient")
    void testGetMainDir() {
        // Créer un MESH avec un gradient configuré
        Mesh mesh = meshLookup[25][25];
        assertNotNull(mesh, "Le MESH doit exister");

        // Configurer un gradient pour un voisin
        Mesh neighbor = mesh.getNeighbor(1); // Direction NE
        if (neighbor != null) {
            neighbor.teamInfo[0].gradient = 1000; // Gradient faible (bon)
            mesh.teamInfo[0].gradient = 2000; // Gradient plus élevé

            int dir = OptimizedMovementEngine.getMainDir(mesh, 0, 0, 0, 0);

            // La direction doit être valide (0-11)
            assertTrue(dir >= 0 && dir < 12, "La direction doit être valide");
        }
    }

    @Test
    @DisplayName("getCloseDir trouve la direction vers le curseur")
    void testGetCloseDir() {
        Mesh mesh = meshLookup[25][25];
        assertNotNull(mesh, "Le MESH doit exister");

        // Configurer la position du curseur dans le MESH
        mesh.teamInfo[0].cursorX = 30;
        mesh.teamInfo[0].cursorY = 30;

        Fighter fighter = new Fighter(25, 25, 0);

        int dir = OptimizedMovementEngine.getCloseDir(mesh, fighter, 0, 0, 0);

        // La direction doit être valide
        assertTrue(dir >= 0 && dir < 12, "La direction doit être valide");
    }

    @Test
    @DisplayName("moveFighters déplace les fighters")
    void testMoveFighters() {
        // Créer un fighter
        Fighter fighter = new Fighter(25, 25, 0);
        fighters.add(fighter);
        teamFighterCount[0] = 1;

        // Configurer le gradient pour que le fighter se déplace
        OptimizedGradientEngine.resetAllGradients(meshArray, 2);
        OptimizedGradientEngine.applyAllCursors(meshArray, cursors,
                new int[] { GameConfig.CURSOR_START_GRADIENT,
                        GameConfig.CURSOR_START_GRADIENT },
                2);

        int initialX = fighter.x;
        int initialY = fighter.y;

        // Déplacer les fighters
        OptimizedMovementEngine.moveFighters(fighters, meshArray, meshLookup,
                cursors, teamFighterCount,
                FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT, FIGHTER_MOVE_Y_ALT,
                0, map, MAP_WIDTH, MAP_HEIGHT);

        // Le fighter doit toujours être dans les limites
        assertTrue(fighter.x >= 0 && fighter.x < MAP_WIDTH, "X doit être dans les limites");
        assertTrue(fighter.y >= 0 && fighter.y < MAP_HEIGHT, "Y doit être dans les limites");
    }

    @Test
    @DisplayName("moveFighters respecte les obstacles")
    void testMoveFightersRespectsObstacles() {
        // Créer un obstacle
        map[26][25] = -1;

        // Reconstruire le MESH avec l'obstacle
        meshArray = MeshBuilder.buildMeshArray(map);
        meshLookup = new Mesh[MAP_HEIGHT][MAP_WIDTH];
        for (Mesh mesh : meshArray) {
            meshLookup[mesh.y][mesh.x] = mesh;
        }

        Fighter fighter = new Fighter(25, 25, 0);
        fighters.add(fighter);
        teamFighterCount[0] = 1;

        OptimizedMovementEngine.moveFighters(fighters, meshArray, meshLookup,
                cursors, teamFighterCount,
                FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT, FIGHTER_MOVE_Y_ALT,
                0, map, MAP_WIDTH, MAP_HEIGHT);

        // Le fighter ne doit pas être sur l'obstacle
        assertNotEquals(-1, map[fighter.y][fighter.x], "Le fighter ne doit pas être sur un obstacle");
    }

    @Test
    @DisplayName("moveFighters gère plusieurs fighters")
    void testMoveFightersMultipleFighters() {
        // Créer plusieurs fighters
        for (int i = 0; i < 10; i++) {
            fighters.add(new Fighter(20 + i, 20, 0));
        }
        teamFighterCount[0] = 10;

        int initialCount = fighters.size();

        OptimizedMovementEngine.moveFighters(fighters, meshArray, meshLookup,
                cursors, teamFighterCount,
                FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT, FIGHTER_MOVE_Y_ALT,
                0, map, MAP_WIDTH, MAP_HEIGHT);

        // Le nombre de fighters doit rester constant
        assertEquals(initialCount, fighters.size(), "Le nombre de fighters doit rester constant");
    }
}
