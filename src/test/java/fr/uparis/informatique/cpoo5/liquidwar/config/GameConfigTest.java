package fr.uparis.informatique.cpoo5.liquidwar.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour GameConfig.
 * 
 * Teste :
 * - Valeurs des constantes
 * - Validité des configurations
 */
class GameConfigTest {

    @Test
    @DisplayName("Constantes de carte sont valides")
    void testMapConstants() {
        assertTrue(GameConfig.MAP_WIDTH > 0, "MAP_WIDTH doit être positif");
        assertTrue(GameConfig.MAP_HEIGHT > 0, "MAP_HEIGHT doit être positif");
        assertEquals(281, GameConfig.MAP_WIDTH, "MAP_WIDTH doit être 281");
        assertEquals(240, GameConfig.MAP_HEIGHT, "MAP_HEIGHT doit être 240");
    }

    @Test
    @DisplayName("Constantes de combattants sont valides")
    void testFighterConstants() {
        assertTrue(GameConfig.MAX_FIGHTERS_PER_TEAM > 0, "MAX_FIGHTERS_PER_TEAM doit être positif");
        assertTrue(GameConfig.FIGHTER_INITIAL_HEALTH > 0, "FIGHTER_INITIAL_HEALTH doit être positif");
        assertTrue(GameConfig.ATTACK_DAMAGE > 0, "ATTACK_DAMAGE doit être positif");
        assertTrue(GameConfig.TOTAL_FIGHTERS > 0, "TOTAL_FIGHTERS doit être positif");
    }

    @Test
    @DisplayName("Constantes de gradient sont valides")
    void testGradientConstants() {
        assertTrue(GameConfig.AREA_START_GRADIENT > 0, "AREA_START_GRADIENT doit être positif");
        assertTrue(GameConfig.CURSOR_START_GRADIENT > 0, "CURSOR_START_GRADIENT doit être positif");
        assertTrue(GameConfig.CURSOR_START_GRADIENT < GameConfig.AREA_START_GRADIENT,
                "CURSOR_START_GRADIENT doit être inférieur à AREA_START_GRADIENT");
    }

    @Test
    @DisplayName("Constantes de mouvement sont valides")
    void testMovementConstants() {
        assertTrue(GameConfig.NB_TRY_MOVE > 0, "NB_TRY_MOVE doit être positif");
        assertTrue(GameConfig.NB_LOCAL_DIRS > 0, "NB_LOCAL_DIRS doit être positif");
        assertTrue(GameConfig.NB_DIRECTIONS > 0, "NB_DIRECTIONS doit être positif");
        assertEquals(12, GameConfig.NB_DIRECTIONS, "NB_DIRECTIONS doit être 12");
    }

    @Test
    @DisplayName("Constantes de performance sont valides")
    void testPerformanceConstants() {
        assertTrue(GameConfig.LOGIC_TIMER_INTERVAL_MS > 0, "LOGIC_TIMER_INTERVAL_MS doit être positif");
        assertTrue(GameConfig.DISPLAY_TIMER_INTERVAL_MS > 0, "DISPLAY_TIMER_INTERVAL_MS doit être positif");
        assertTrue(GameConfig.TARGET_FPS > 0, "TARGET_FPS doit être positif");
    }

    @Test
    @DisplayName("Constantes d'équipes sont valides")
    void testTeamConstants() {
        assertTrue(GameConfig.MAX_TEAMS > 0, "MAX_TEAMS doit être positif");
        assertTrue(GameConfig.DEFAULT_TEAM_COUNT > 0, "DEFAULT_TEAM_COUNT doit être positif");
        assertTrue(GameConfig.DEFAULT_TEAM_COUNT <= GameConfig.MAX_TEAMS,
                "DEFAULT_TEAM_COUNT ne doit pas dépasser MAX_TEAMS");
    }
}
