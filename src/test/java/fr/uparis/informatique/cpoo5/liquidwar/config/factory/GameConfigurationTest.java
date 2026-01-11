package fr.uparis.informatique.cpoo5.liquidwar.config.factory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour GameConfiguration.
 * 
 * Teste :
 * - Création de configuration
 * - Getters
 * - Immutabilité
 */
class GameConfigurationTest {

    @Test
    @DisplayName("Création de configuration avec tous les paramètres")
    void testCreateConfiguration() {
        GameConfiguration config = new GameConfiguration(
                "Test", 5, 60, 40, 2000, 2);

        assertEquals("Test", config.getName());
        assertEquals(5, config.getLogicTimerIntervalMs());
        assertEquals(60, config.getTargetFps());
        assertEquals(40, config.getCursorProximityRadius());
        assertEquals(2000, config.getInitialFightersPerTeam());
        assertEquals(2, config.getCursorMoveSpeed());
    }

    @Test
    @DisplayName("toString retourne une représentation valide")
    void testToString() {
        GameConfiguration config = new GameConfiguration(
                "Test", 5, 60, 40, 2000, 2);

        String str = config.toString();
        assertNotNull(str, "toString ne doit pas retourner null");
        assertTrue(str.contains("Test"), "toString doit contenir le nom");
        assertFalse(str.isEmpty(), "toString ne doit pas être vide");
    }

    @Test
    @DisplayName("Configuration est immuable")
    void testImmutability() {
        GameConfiguration config = new GameConfiguration(
                "Test", 5, 60, 40, 2000, 2);

        // Vérifier que les valeurs ne changent pas
        String name1 = config.getName();
        int fps1 = config.getTargetFps();

        // Appeler plusieurs fois
        String name2 = config.getName();
        int fps2 = config.getTargetFps();

        assertEquals(name1, name2, "Le nom doit rester constant");
        assertEquals(fps1, fps2, "Le FPS doit rester constant");
    }
}
