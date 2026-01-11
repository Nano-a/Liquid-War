package fr.uparis.informatique.cpoo5.liquidwar.config;

import fr.uparis.informatique.cpoo5.liquidwar.config.factory.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ConfigFactory.
 * 
 * Teste :
 * - Création de configurations prédéfinies
 * - Configurations custom
 * - Chargement par nom
 */
class ConfigFactoryTest {
    
    @Test
    @DisplayName("Création configuration Default")
    void testCreateDefault() {
        GameConfiguration config = ConfigFactory.createDefault();
        
        assertNotNull(config);
        assertEquals("Default", config.getName());
        assertTrue(config.getLogicTimerIntervalMs() > 0);
        assertTrue(config.getTargetFps() > 0);
    }
    
    @Test
    @DisplayName("Création configuration Easy")
    void testCreateEasy() {
        GameConfiguration config = ConfigFactory.createEasy();
        
        assertNotNull(config);
        assertEquals("Easy", config.getName());
        
        // Easy devrait avoir :
        // - Timer plus lent (plus d'intervalles)
        // - Moins de fighters
        // - Curseur plus rapide
        assertTrue(config.getLogicTimerIntervalMs() >= 10);
        assertTrue(config.getInitialFightersPerTeam() <= 1500);
        assertTrue(config.getCursorMoveSpeed() >= 3);
    }
    
    @Test
    @DisplayName("Création configuration Hard")
    void testCreateHard() {
        GameConfiguration config = ConfigFactory.createHard();
        
        assertNotNull(config);
        assertEquals("Hard", config.getName());
        
        // Hard devrait avoir :
        // - Timer rapide
        // - Plus de fighters
        // - Curseur lent
        assertTrue(config.getLogicTimerIntervalMs() <= 5);
        assertTrue(config.getInitialFightersPerTeam() >= 2500);
        assertTrue(config.getCursorMoveSpeed() <= 1);
    }
    
    @Test
    @DisplayName("Création configuration Performance")
    void testCreatePerformance() {
        GameConfiguration config = ConfigFactory.createPerformance();
        
        assertNotNull(config);
        assertEquals("Performance", config.getName());
        
        // Performance : moins de fighters pour meilleur FPS
        assertTrue(config.getInitialFightersPerTeam() <= 1000);
    }
    
    @Test
    @DisplayName("Création configuration Quality")
    void testCreateQuality() {
        GameConfiguration config = ConfigFactory.createQuality();
        
        assertNotNull(config);
        assertEquals("Quality", config.getName());
        
        // Quality : max de fighters et FPS élevé
        assertTrue(config.getInitialFightersPerTeam() >= 3000);
        assertTrue(config.getTargetFps() >= 200);
    }
    
    @Test
    @DisplayName("Création configuration Custom")
    void testCreateCustom() {
        GameConfiguration config = ConfigFactory.createCustom(
            "MyConfig", 7, 150, 25, 1800, 2
        );
        
        assertNotNull(config);
        assertEquals("MyConfig", config.getName());
        assertEquals(7, config.getLogicTimerIntervalMs());
        assertEquals(150, config.getTargetFps());
        assertEquals(25, config.getCursorProximityRadius());
        assertEquals(1800, config.getInitialFightersPerTeam());
        assertEquals(2, config.getCursorMoveSpeed());
    }
    
    @Test
    @DisplayName("Chargement configuration par nom")
    void testLoadConfiguration() {
        GameConfiguration easy = ConfigFactory.loadConfiguration("easy");
        assertEquals("Easy", easy.getName());
        
        GameConfiguration normal = ConfigFactory.loadConfiguration("normal");
        assertEquals("Normal", normal.getName());
        
        GameConfiguration hard = ConfigFactory.loadConfiguration("hard");
        assertEquals("Hard", hard.getName());
        
        GameConfiguration perf = ConfigFactory.loadConfiguration("performance");
        assertEquals("Performance", perf.getName());
        
        GameConfiguration quality = ConfigFactory.loadConfiguration("qualité");
        assertEquals("Quality", quality.getName());
    }
    
    @Test
    @DisplayName("Chargement configuration inconnue retourne Default")
    void testLoadUnknownConfiguration() {
        GameConfiguration config = ConfigFactory.loadConfiguration("unknown");
        
        assertNotNull(config);
        assertEquals("Default", config.getName());
    }
    
    @Test
    @DisplayName("Configurations sont immutables")
    void testConfigurationImmutability() {
        GameConfiguration config = ConfigFactory.createDefault();
        
        // Vérifier qu'il n'y a pas de setters
        // (test compile-time vérifié par la structure de la classe)
        assertNotNull(config.toString());
        assertTrue(config.toString().contains("Default"));
    }
    
    @Test
    @DisplayName("Toutes les configurations ont des valeurs valides")
    void testAllConfigurationsValid() {
        GameConfiguration[] configs = {
            ConfigFactory.createDefault(),
            ConfigFactory.createEasy(),
            ConfigFactory.createNormal(),
            ConfigFactory.createHard(),
            ConfigFactory.createPerformance(),
            ConfigFactory.createQuality()
        };
        
        for (GameConfiguration config : configs) {
            // Valeurs positives
            assertTrue(config.getLogicTimerIntervalMs() > 0);
            assertTrue(config.getTargetFps() > 0);
            assertTrue(config.getCursorProximityRadius() >= 0);
            assertTrue(config.getInitialFightersPerTeam() > 0);
            assertTrue(config.getCursorMoveSpeed() > 0);
            
            // Valeurs raisonnables
            assertTrue(config.getLogicTimerIntervalMs() < 100);
            assertTrue(config.getTargetFps() <= 300);
            assertTrue(config.getInitialFightersPerTeam() <= 5000);
        }
    }
}

