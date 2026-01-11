package fr.uparis.informatique.cpoo5.liquidwar.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour RenderConfig.
 * 
 * Teste :
 * - Constantes de zoom
 * - Couleurs des équipes
 * - Couleurs de la carte
 */
class RenderConfigTest {

    @Test
    @DisplayName("SCALE et dimensions sont valides")
    void testScaleAndDimensions() {
        assertTrue(RenderConfig.SCALE > 0, "SCALE doit être positif");
        assertTrue(RenderConfig.GAME_WIDTH > 0, "GAME_WIDTH doit être positif");
        assertTrue(RenderConfig.GAME_HEIGHT > 0, "GAME_HEIGHT doit être positif");
        assertEquals(281 * RenderConfig.SCALE, RenderConfig.GAME_WIDTH,
                "GAME_WIDTH doit être MAP_WIDTH * SCALE");
        assertEquals(240 * RenderConfig.SCALE, RenderConfig.GAME_HEIGHT,
                "GAME_HEIGHT doit être MAP_HEIGHT * SCALE");
    }

    @Test
    @DisplayName("TEAM_COLORS contient 6 couleurs")
    void testTeamColors() {
        assertNotNull(RenderConfig.TEAM_COLORS, "TEAM_COLORS ne doit pas être null");
        assertEquals(6, RenderConfig.TEAM_COLORS.length, "Il doit y avoir 6 couleurs");

        for (int i = 0; i < RenderConfig.TEAM_COLORS.length; i++) {
            assertNotNull(RenderConfig.TEAM_COLORS[i], "Couleur " + i + " ne doit pas être null");
        }
    }

    @Test
    @DisplayName("Couleurs de fond sont valides")
    void testBackgroundColors() {
        assertNotNull(RenderConfig.BACKGROUND_COLOR_1, "BACKGROUND_COLOR_1 ne doit pas être null");
        assertNotNull(RenderConfig.BACKGROUND_COLOR_2, "BACKGROUND_COLOR_2 ne doit pas être null");
        assertNotNull(RenderConfig.OBSTACLE_COLOR, "OBSTACLE_COLOR ne doit pas être null");
        assertNotNull(RenderConfig.GAME_AREA_BACKGROUND, "GAME_AREA_BACKGROUND ne doit pas être null");
    }

    @Test
    @DisplayName("Constantes de curseur sont valides")
    void testCursorConstants() {
        assertTrue(RenderConfig.CURSOR_SIZE > 0, "CURSOR_SIZE doit être positif");
        assertTrue(RenderConfig.CURSOR_CROSS_SIZE > 0, "CURSOR_CROSS_SIZE doit être positif");
    }

    @Test
    @DisplayName("Constantes de police sont valides")
    void testFontConstants() {
        assertNotNull(RenderConfig.HUD_FONT_NAME, "HUD_FONT_NAME ne doit pas être null");
        assertTrue(RenderConfig.HUD_FONT_SIZE_LARGE > 0, "HUD_FONT_SIZE_LARGE doit être positif");
        assertTrue(RenderConfig.HUD_FONT_SIZE_SMALL > 0, "HUD_FONT_SIZE_SMALL doit être positif");
    }
}
