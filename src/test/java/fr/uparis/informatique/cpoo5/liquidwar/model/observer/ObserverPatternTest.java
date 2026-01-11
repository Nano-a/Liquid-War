package fr.uparis.informatique.cpoo5.liquidwar.model.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le pattern Observer.
 * 
 * Teste :
 * - GameSubject (ajout/suppression observateurs)
 * - Notifications optimisées
 * - Thread-safety
 */
class ObserverPatternTest {
    
    private GameSubject subject;
    private TestObserver observer1;
    private TestObserver observer2;
    
    // Observateur de test
    private static class TestObserver implements GameObserver {
        int stateChangedCount = 0;
        int particleCountChangedCount = 0;
        int gameOverCount = 0;
        int pauseChangedCount = 0;
        
        @Override
        public void onGameStateChanged(GameEvent event, Object data) {
            stateChangedCount++;
        }
        
        @Override
        public void onParticleCountChanged(int[] teamCounts) {
            particleCountChangedCount++;
        }
        
        @Override
        public void onGameOver(int winnerTeam, long gameDuration) {
            gameOverCount++;
        }
        
        @Override
        public void onPauseStateChanged(boolean paused) {
            pauseChangedCount++;
        }
    }
    
    @BeforeEach
    void setUp() {
        subject = new GameSubject();
        observer1 = new TestObserver();
        observer2 = new TestObserver();
    }
    
    @Test
    @DisplayName("Ajout d'observateurs")
    void testAddObserver() {
        subject.addObserver(observer1);
        assertEquals(1, subject.getObserverCount(),
                    "Le nombre d'observateurs doit être 1");
        
        subject.addObserver(observer2);
        assertEquals(2, subject.getObserverCount(),
                    "Le nombre d'observateurs doit être 2");
    }
    
    @Test
    @DisplayName("Suppression d'observateurs")
    void testRemoveObserver() {
        subject.addObserver(observer1);
        subject.addObserver(observer2);
        
        subject.removeObserver(observer1);
        assertEquals(1, subject.getObserverCount(),
                    "Le nombre d'observateurs doit être 1 après suppression");
    }
    
    @Test
    @DisplayName("Notification de tous les observateurs")
    void testNotifyAllObservers() {
        subject.addObserver(observer1);
        subject.addObserver(observer2);
        
        subject.notifyStateChanged(GameEvent.STATE_UPDATED, null);
        
        assertEquals(1, observer1.stateChangedCount,
                    "L'observateur 1 doit être notifié");
        assertEquals(1, observer2.stateChangedCount,
                    "L'observateur 2 doit être notifié");
    }
    
    @Test
    @DisplayName("Optimisation - pas de notification si pas de changement")
    void testOptimizedParticleNotification() {
        subject.addObserver(observer1);
        
        int[] teamCounts = {1000, 1000};
        
        // Première notification
        subject.notifyParticleCountChanged(teamCounts);
        assertEquals(1, observer1.particleCountChangedCount,
                    "Première notification doit passer");
        
        // Même valeur - ne doit PAS notifier
        subject.notifyParticleCountChanged(teamCounts);
        assertEquals(1, observer1.particleCountChangedCount,
                    "Pas de notification si valeurs identiques");
        
        // Valeur différente - doit notifier
        teamCounts[0] = 999;
        subject.notifyParticleCountChanged(teamCounts);
        assertEquals(2, observer1.particleCountChangedCount,
                    "Notification si valeurs différentes");
    }
    
    @Test
    @DisplayName("Optimisation - pause state")
    void testOptimizedPauseNotification() {
        subject.addObserver(observer1);
        
        // Première pause
        subject.notifyPauseStateChanged(true);
        assertEquals(1, observer1.pauseChangedCount,
                    "Première notification pause");
        
        // Même état - ne doit PAS notifier
        subject.notifyPauseStateChanged(true);
        assertEquals(1, observer1.pauseChangedCount,
                    "Pas de notification si état identique");
        
        // État différent - doit notifier
        subject.notifyPauseStateChanged(false);
        assertEquals(2, observer1.pauseChangedCount,
                    "Notification si état change");
    }
    
    @Test
    @DisplayName("Notification Game Over")
    void testGameOverNotification() {
        subject.addObserver(observer1);
        subject.addObserver(observer2);
        
        subject.notifyGameOver(0, 120);
        
        assertEquals(1, observer1.gameOverCount);
        assertEquals(1, observer2.gameOverCount);
    }
    
    @Test
    @DisplayName("Clear tous les observateurs")
    void testClearObservers() {
        subject.addObserver(observer1);
        subject.addObserver(observer2);
        
        subject.clearObservers();
        
        assertEquals(0, subject.getObserverCount(),
                    "Tous les observateurs doivent être retirés");
    }
    
    @Test
    @DisplayName("Pas de doublon d'observateurs")
    void testNoDuplicateObservers() {
        subject.addObserver(observer1);
        subject.addObserver(observer1); // Ajouter 2 fois
        
        assertEquals(1, subject.getObserverCount(),
                    "Pas de doublon d'observateurs");
    }
    
    @Test
    @DisplayName("Thread-safety basique")
    void testThreadSafety() throws InterruptedException {
        // Ajouter/retirer dans plusieurs threads
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                subject.addObserver(new TestObserver());
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                subject.notifyStateChanged(GameEvent.STATE_UPDATED, null);
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        // Doit se terminer sans exception
        assertTrue(subject.getObserverCount() > 0,
                  "Des observateurs doivent être présents");
    }
}

