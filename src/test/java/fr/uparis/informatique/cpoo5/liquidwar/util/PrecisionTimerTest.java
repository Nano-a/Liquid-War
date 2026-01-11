package fr.uparis.informatique.cpoo5.liquidwar.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour PrecisionTimer.
 * 
 * Teste :
 * - Initialisation
 * - Timing de logique
 * - Timing d'affichage
 * - Statistiques
 */
class PrecisionTimerTest {

    private PrecisionTimer timer;

    @BeforeEach
    void setUp() {
        timer = new PrecisionTimer();
    }

    @Test
    @DisplayName("reset réinitialise le timer")
    void testReset() {
        timer.markLogicUpdated();
        timer.markDisplayUpdated();

        timer.reset();

        assertEquals(0, timer.getElapsedSeconds(), 0.1,
                "Le temps écoulé doit être proche de 0 après reset");
    }

    @Test
    @DisplayName("shouldUpdateLogic retourne true après l'intervalle")
    void testShouldUpdateLogic() {
        // Attendre un peu pour que l'intervalle passe
        try {
            Thread.sleep(10); // 10ms > 5ms (LOGIC_INTERVAL_NS)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(timer.shouldUpdateLogic(),
                "shouldUpdateLogic doit retourner true après l'intervalle");
    }

    @Test
    @DisplayName("shouldUpdateDisplay retourne true après l'intervalle")
    void testShouldUpdateDisplay() {
        // Attendre un peu pour que l'intervalle passe
        try {
            Thread.sleep(20); // 20ms > 16ms (DISPLAY_INTERVAL_NS)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(timer.shouldUpdateDisplay(),
                "shouldUpdateDisplay doit retourner true après l'intervalle");
    }

    @Test
    @DisplayName("markLogicUpdated met à jour les statistiques")
    void testMarkLogicUpdated() {
        timer.markLogicUpdated();

        assertTrue(timer.getLogicFPS() >= 0, "Le FPS de logique doit être >= 0");
        assertTrue(timer.getAverageLogicTime() >= 0,
                "Le temps moyen de logique doit être >= 0");
    }

    @Test
    @DisplayName("markDisplayUpdated met à jour les statistiques")
    void testMarkDisplayUpdated() {
        timer.markDisplayUpdated();

        assertTrue(timer.getDisplayFPS() >= 0, "Le FPS d'affichage doit être >= 0");
        assertTrue(timer.getAverageDisplayTime() >= 0,
                "Le temps moyen d'affichage doit être >= 0");
    }

    @Test
    @DisplayName("getElapsedSeconds retourne le temps écoulé")
    void testGetElapsedSeconds() {
        timer.reset();

        try {
            Thread.sleep(100); // 100ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        double elapsed = timer.getElapsedSeconds();
        assertTrue(elapsed >= 0.09 && elapsed <= 0.2,
                "Le temps écoulé doit être proche de 0.1 secondes");
    }

    @Test
    @DisplayName("nanoTime retourne un temps valide")
    void testNanoTime() {
        long time1 = PrecisionTimer.nanoTime();
        assertTrue(time1 > 0, "Le temps doit être positif");

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long time2 = PrecisionTimer.nanoTime();
        assertTrue(time2 > time1, "Le temps doit augmenter");
    }

    @Test
    @DisplayName("Les statistiques sont cohérentes après plusieurs updates")
    void testStatisticsConsistency() {
        for (int i = 0; i < 10; i++) {
            timer.markLogicUpdated();
            timer.markDisplayUpdated();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertTrue(timer.getLogicFPS() >= 0, "Le FPS de logique doit être valide");
        assertTrue(timer.getDisplayFPS() >= 0, "Le FPS d'affichage doit être valide");
        assertTrue(timer.getAverageLogicTime() >= 0,
                "Le temps moyen de logique doit être valide");
        assertTrue(timer.getAverageDisplayTime() >= 0,
                "Le temps moyen d'affichage doit être valide");
    }
}
