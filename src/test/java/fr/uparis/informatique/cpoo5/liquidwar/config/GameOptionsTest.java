package fr.uparis.informatique.cpoo5.liquidwar.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour GameOptions.
 * 
 * Teste :
 * - Singleton pattern
 * - Gestion du volume
 * - Qualité graphique
 * - Vitesse du jeu
 */
class GameOptionsTest {

    private GameOptions options;

    @BeforeEach
    void setUp() {
        options = GameOptions.getInstance();
    }

    @Test
    @DisplayName("getInstance retourne la même instance (Singleton)")
    void testSingleton() {
        GameOptions instance1 = GameOptions.getInstance();
        GameOptions instance2 = GameOptions.getInstance();

        assertSame(instance1, instance2, "getInstance doit retourner la même instance");
    }

    @Test
    @DisplayName("setVolumeLevel limite les valeurs entre 0 et 100")
    void testSetVolumeLevel() {
        options.setVolumeLevel(150);
        assertEquals(100, options.getVolumeLevel(), "Volume doit être limité à 100");

        options.setVolumeLevel(-10);
        assertEquals(0, options.getVolumeLevel(), "Volume doit être limité à 0");

        options.setVolumeLevel(50);
        assertEquals(50, options.getVolumeLevel(), "Volume doit être 50");
    }

    @Test
    @DisplayName("setGraphicsQuality change la qualité")
    void testSetGraphicsQuality() {
        options.setGraphicsQuality("Basse");
        assertEquals("Basse", options.getGraphicsQuality());

        options.setGraphicsQuality("Élevée");
        assertEquals("Élevée", options.getGraphicsQuality());
    }

    @Test
    @DisplayName("isAntialiasingEnabled retourne false pour Basse")
    void testAntialiasingEnabled() {
        options.setGraphicsQuality("Basse");
        assertFalse(options.isAntialiasingEnabled(), "Antialiasing doit être désactivé pour Basse");

        options.setGraphicsQuality("Moyenne");
        assertTrue(options.isAntialiasingEnabled(), "Antialiasing doit être activé pour Moyenne");

        options.setGraphicsQuality("Élevée");
        assertTrue(options.isAntialiasingEnabled(), "Antialiasing doit être activé pour Élevée");
    }

    @Test
    @DisplayName("isBilinearInterpolationEnabled retourne true uniquement pour Élevée")
    void testBilinearInterpolation() {
        options.setGraphicsQuality("Basse");
        assertFalse(options.isBilinearInterpolationEnabled());

        options.setGraphicsQuality("Moyenne");
        assertFalse(options.isBilinearInterpolationEnabled());

        options.setGraphicsQuality("Élevée");
        assertTrue(options.isBilinearInterpolationEnabled());
    }

    @Test
    @DisplayName("getRenderSkipFrames retourne les bonnes valeurs")
    void testRenderSkipFrames() {
        options.setGraphicsQuality("Basse");
        assertEquals(3, options.getRenderSkipFrames());

        options.setGraphicsQuality("Moyenne");
        assertEquals(2, options.getRenderSkipFrames());

        options.setGraphicsQuality("Élevée");
        assertEquals(1, options.getRenderSkipFrames());
    }

    @Test
    @DisplayName("setGameSpeed limite les valeurs entre 25 et 200")
    void testSetGameSpeed() {
        options.setGameSpeed(300);
        assertEquals(200, options.getGameSpeed(), "Vitesse doit être limitée à 200");

        options.setGameSpeed(10);
        assertEquals(25, options.getGameSpeed(), "Vitesse doit être limitée à 25");

        options.setGameSpeed(100);
        assertEquals(100, options.getGameSpeed(), "Vitesse doit être 100");
    }

    @Test
    @DisplayName("getLogicTimerDelay calcule correctement")
    void testGetLogicTimerDelay() {
        options.setGameSpeed(100);
        int delay = options.getLogicTimerDelay();
        assertTrue(delay >= 2 && delay <= 20, "Le délai doit être dans une plage raisonnable");

        options.setGameSpeed(200);
        int delayFast = options.getLogicTimerDelay();
        assertTrue(delayFast < delay, "Vitesse plus élevée = délai plus court");
    }

    @Test
    @DisplayName("getSpeedMultiplier calcule correctement")
    void testGetSpeedMultiplier() {
        options.setGameSpeed(100);
        assertEquals(1.0f, options.getSpeedMultiplier(), 0.01f, "Multiplicateur doit être 1.0 à 100%");

        options.setGameSpeed(50);
        assertEquals(0.5f, options.getSpeedMultiplier(), 0.01f, "Multiplicateur doit être 0.5 à 50%");

        options.setGameSpeed(200);
        assertEquals(2.0f, options.getSpeedMultiplier(), 0.01f, "Multiplicateur doit être 2.0 à 200%");
    }
}
