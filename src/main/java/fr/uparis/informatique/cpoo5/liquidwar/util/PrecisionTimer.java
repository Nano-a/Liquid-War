package fr.uparis.informatique.cpoo5.liquidwar.util;

/**
 * Timer haute précision pour Liquid War - Séparation logique/affichage.
 * 
 * Cette classe reproduit le système de timing du code C (game.c, lignes 723-819)
 * pour obtenir la même fluidité et performance.
 * 
 * PRINCIPE (comme dans game.c) :
 * - Logique à haute fréquence (200 Hz = 5ms) pour réactivité
 * - Affichage à fréquence modérée (60 Hz = 16ms) pour économie CPU
 * - Séparation complète entre les deux boucles
 * - Compensation du jitter avec mesure précise du temps
 * 
 * AVANTAGES :
 * - Mouvement fluide même si affichage ralentit
 * - CPU usage réduit (pas de rendu inutile à 200 FPS)
 * - Latence minimale (logique à 200 Hz)
 */
public class PrecisionTimer {
    
    // ===== CONSTANTES (configurables) =====
    
    /**
     * Fréquence de la boucle de logique (comme LOGIC_DELAY_MIN[4] = 5ms)
     * 200 Hz = mise à jour toutes les 5ms
     */
    public static final long LOGIC_INTERVAL_NS = 5_000_000L;  // 5ms en nanosecondes
    
    /**
     * Fréquence de la boucle d'affichage (comme DISPLAY_DELAY_MIN[6] = 16ms)
     * 60 Hz = affichage toutes les 16ms
     */
    public static final long DISPLAY_INTERVAL_NS = 16_666_667L;  // ~60 FPS
    
    /**
     * Temps maximum pour rattrapage (éviter spiral of death)
     */
    private static final long MAX_CATCHUP_NS = 100_000_000L;  // 100ms max
    
    // ===== ÉTAT DU TIMER =====
    
    private long lastLogicTime;      // Dernière mise à jour logique
    private long lastDisplayTime;    // Dernier affichage
    private long startTime;          // Temps de démarrage
    
    // ===== STATISTIQUES =====
    
    private int logicTickCount = 0;     // Nombre de ticks de logique
    private int displayFrameCount = 0;  // Nombre de frames affichées
    private long logicTimeAccum = 0;    // Temps total de logique (pour moyenne)
    private long displayTimeAccum = 0;  // Temps total d'affichage
    private int statsSampleCount = 0;   // Nombre d'échantillons pour stats
    
    // ===== TIMING PRÉCIS =====
    
    /**
     * Temps actuel en nanosecondes (haute précision).
     * 
     * Utilise System.nanoTime() qui est monotone et précis
     * (équivalent de get_ticker() dans le code C avec Allegro).
     */
    public static long nanoTime() {
        return System.nanoTime();
    }
    
    // ===== CONSTRUCTEUR =====
    
    public PrecisionTimer() {
        reset();
    }
    
    /**
     * Réinitialise le timer.
     */
    public void reset() {
        long now = nanoTime();
        this.lastLogicTime = now;
        this.lastDisplayTime = now;
        this.startTime = now;
        this.logicTickCount = 0;
        this.displayFrameCount = 0;
        this.logicTimeAccum = 0;
        this.displayTimeAccum = 0;
        this.statsSampleCount = 0;
    }
    
    // ===== MÉTHODES DE TIMING =====
    
    /**
     * Vérifie si c'est le moment de mettre à jour la logique.
     * 
     * Équivalent de :
     * while (get_ticker() < last_logic_time + LOGIC_DELAY_MIN[CONFIG_ROUNDS_PER_SEC_LIMIT])
     * 
     * @return true si la logique doit être mise à jour
     */
    public boolean shouldUpdateLogic() {
        long now = nanoTime();
        long elapsed = now - lastLogicTime;
        
        // Si assez de temps s'est écoulé
        if (elapsed >= LOGIC_INTERVAL_NS) {
            // Protection contre spiral of death
            if (elapsed > MAX_CATCHUP_NS) {
                System.err.println("⚠️ Logique trop lente ! Rattrapant " + 
                                 (elapsed / 1_000_000) + "ms");
                lastLogicTime = now - LOGIC_INTERVAL_NS;
                return true;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Marque que la logique a été mise à jour.
     * 
     * Équivalent de : last_logic_time = get_ticker()
     */
    public void markLogicUpdated() {
        long now = nanoTime();
        long elapsed = now - lastLogicTime;
        
        // Statistiques
        logicTimeAccum += elapsed;
        logicTickCount++;
        statsSampleCount++;
        
        // Mise à jour du temps
        lastLogicTime = now;
        
        // Afficher stats toutes les 1000 frames
        if (statsSampleCount >= 1000) {
            printStats();
            statsSampleCount = 0;
            logicTimeAccum = 0;
            displayTimeAccum = 0;
        }
    }
    
    /**
     * Vérifie si c'est le moment d'afficher.
     * 
     * Équivalent de :
     * while (get_ticker() < last_display_time + DISPLAY_DELAY_MIN[CONFIG_FPS_LIMIT])
     * 
     * @return true si l'affichage doit être mis à jour
     */
    public boolean shouldUpdateDisplay() {
        long now = nanoTime();
        long elapsed = now - lastDisplayTime;
        
        return elapsed >= DISPLAY_INTERVAL_NS;
    }
    
    /**
     * Marque que l'affichage a été mis à jour.
     */
    public void markDisplayUpdated() {
        long now = nanoTime();
        long elapsed = now - lastDisplayTime;
        
        // Statistiques
        displayTimeAccum += elapsed;
        displayFrameCount++;
        
        // Mise à jour du temps
        lastDisplayTime = now;
    }
    
    /**
     * Attend activement jusqu'au prochain tick de logique.
     * 
     * Équivalent de la boucle d'attente dans game.c (lignes 789-807).
     * 
     * Note : Utilise yield() pour ne pas monopoliser le CPU.
     */
    public void waitForNextLogicTick() {
        long targetTime = lastLogicTime + LOGIC_INTERVAL_NS;
        
        while (nanoTime() < targetTime) {
            // Yield pour laisser d'autres threads s'exécuter
            Thread.yield();
            
            // Sleep court si beaucoup de temps restant (>1ms)
            long remaining = targetTime - nanoTime();
            if (remaining > 1_000_000L) {  // >1ms
                try {
                    Thread.sleep(0, 500_000);  // Sleep 0.5ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    
    /**
     * Attend activement jusqu'à la prochaine frame d'affichage.
     */
    public void waitForNextDisplayFrame() {
        long targetTime = lastDisplayTime + DISPLAY_INTERVAL_NS;
        
        while (nanoTime() < targetTime) {
            Thread.yield();
            
            long remaining = targetTime - nanoTime();
            if (remaining > 2_000_000L) {  // >2ms
                try {
                    Thread.sleep(1);  // Sleep 1ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    
    // ===== STATISTIQUES =====
    
    /**
     * Obtient le FPS moyen de logique.
     */
    public double getLogicFPS() {
        long elapsed = nanoTime() - startTime;
        if (elapsed == 0) return 0;
        return (double) logicTickCount / (elapsed / 1_000_000_000.0);
    }
    
    /**
     * Obtient le FPS moyen d'affichage.
     */
    public double getDisplayFPS() {
        long elapsed = nanoTime() - startTime;
        if (elapsed == 0) return 0;
        return (double) displayFrameCount / (elapsed / 1_000_000_000.0);
    }
    
    /**
     * Obtient le temps moyen de logique (ms).
     */
    public double getAverageLogicTime() {
        if (logicTickCount == 0) return 0;
        return (double) logicTimeAccum / logicTickCount / 1_000_000.0;
    }
    
    /**
     * Obtient le temps moyen d'affichage (ms).
     */
    public double getAverageDisplayTime() {
        if (displayFrameCount == 0) return 0;
        return (double) displayTimeAccum / displayFrameCount / 1_000_000.0;
    }
    
    /**
     * Affiche les statistiques de performance.
     */
    private void printStats() {
        double logicFPS = getLogicFPS();
        double displayFPS = getDisplayFPS();
        double avgLogic = getAverageLogicTime();
        double avgDisplay = getAverageDisplayTime();
        
        System.out.printf("⚡ TIMING STATS | " +
                        "Logique: %.1f Hz (%.2fms) | " +
                        "Affichage: %.1f FPS (%.2fms) | " +
                        "Ratio: %.1f:1%n",
                        logicFPS, avgLogic,
                        displayFPS, avgDisplay,
                        logicFPS / displayFPS);
    }
    
    /**
     * Obtient le temps écoulé depuis le démarrage (secondes).
     */
    public double getElapsedSeconds() {
        return (nanoTime() - startTime) / 1_000_000_000.0;
    }
}

