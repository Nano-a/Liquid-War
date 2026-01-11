package fr.uparis.informatique.cpoo5.liquidwar.controller;

import javax.swing.*;
import java.awt.*;

import fr.uparis.informatique.cpoo5.liquidwar.view.GameCanvas;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.config.RenderConfig;

/**
 * Launcher de DEBUG pour accéder DIRECTEMENT au jeu sans passer par les menus.
 * Permet de tester rapidement la fluidité et d'afficher des logs détaillés.
 * 
 * USAGE :
 *   ./gradlew run -PmainClass=fr.uparis.informatique.cpoo5.liquidwar.controller.DebugLauncher
 * 
 * OU créer un script :
 *   cd liquid-war-java
 *   java -cp build/classes/java/main:build/resources/main fr.uparis.informatique.cpoo5.liquidwar.controller.DebugLauncher
 */
public class DebugLauncher {
    
    private static JFrame gameFrame;
    private static GameCanvas gameCanvas;
    private static javax.swing.Timer logicTimer;
    private static javax.swing.Timer displayTimer;
    private static JPanel statsPanel;
    
    // Statistiques pour debug
    private static long lastLogicTime = System.nanoTime();
    private static long lastDisplayTime = System.nanoTime();
    private static int logicTickCount = 0;
    private static int displayFrameCount = 0;
    private static long startTime = System.currentTimeMillis();
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          🐛 DEBUG LAUNCHER - Liquid War Java                  ║");
        System.out.println("║  Lance le jeu DIRECTEMENT sans menu pour tests de fluidité   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Erreur lors du lancement : " + e.getMessage());
            }
        });
    }
    
    private static void createAndShowGUI() {
        // Créer la fenêtre
        gameFrame = new JFrame("🐛 DEBUG MODE - Liquid War - Fluidité Test");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLayout(new BorderLayout());
        
        System.out.println("📊 Configuration du jeu :");
        System.out.println("   - Carte : " + GameConfig.MAP_WIDTH + "x" + GameConfig.MAP_HEIGHT);
        System.out.println("   - Zoom : x" + RenderConfig.SCALE);
        System.out.println("   - Max fighters par équipe : " + GameConfig.MAX_FIGHTERS_PER_TEAM);
        System.out.println();
        
        // Créer le canvas de jeu
        gameCanvas = new GameCanvas();
        int contentWidth = GameConfig.MAP_WIDTH * RenderConfig.SCALE;
        int contentHeight = GameConfig.MAP_HEIGHT * RenderConfig.SCALE;
        gameCanvas.setPreferredSize(new Dimension(contentWidth, contentHeight));
        
        // Configurer le jeu DIRECTEMENT (2 équipes, difficulté Moyen)
        System.out.println("🎮 Configuration du jeu...");
        gameCanvas.setRequestedTeams(2);
        gameCanvas.setTeamTypes(new String[]{"Humain", "IA", "IA", "IA"});
        gameCanvas.setTeamControlTypes(new String[]{"Flèches", "Souris", "ZQSD", "Flèches"});
        gameCanvas.setFighterCounts(new int[]{2000, 2000, 2000, 2000});
        gameCanvas.setAIDifficulty("Moyen");
        // ✅ Initialiser APRÈS toutes les configs !
        gameCanvas.initialize();
        System.out.println("✅ Jeu configuré !");
        System.out.println();
        
        // Panneau de stats en temps réel
        statsPanel = createStatsPanel();
        
        gameFrame.add(gameCanvas, BorderLayout.CENTER);
        gameFrame.add(statsPanel, BorderLayout.SOUTH);
        
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
        
        // Focus sur le canvas pour les inputs clavier
        gameCanvas.requestFocusInWindow();
        
        // Démarrer les timers avec LOGS DÉTAILLÉS
        startGameLoopWithLogs();
        
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  🎮 JEU LANCÉ EN MODE DEBUG                                   ║");
        System.out.println("║                                                                ║");
        System.out.println("║  📊 Les statistiques s'affichent en temps réel ci-dessous     ║");
        System.out.println("║  ⌨️  Touches : Flèches = Déplacer, P = Pause, W = Distortion ║");
        System.out.println("║  📈 Surveillez la console pour les logs de performance        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    private static JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(Color.BLACK);
        
        JLabel[] labels = new JLabel[8];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("", SwingConstants.CENTER);
            labels[i].setForeground(Color.GREEN);
            labels[i].setFont(new Font("Monospaced", Font.BOLD, 12));
            panel.add(labels[i]);
        }
        
        // Timer pour mettre à jour les stats visuelles
        javax.swing.Timer statsTimer = new javax.swing.Timer(100, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double logicFPS = logicTickCount / (elapsed / 1000.0);
            double displayFPS = displayFrameCount / (elapsed / 1000.0);
            
            int[] teamCounts = gameCanvas.getTeamFighterCounts();
            
            labels[0].setText("Logique: " + String.format("%.1f Hz", logicFPS));
            labels[1].setText("Affichage: " + String.format("%.1f FPS", displayFPS));
            labels[2].setText("Team 0: " + teamCounts[0]);
            labels[3].setText("Team 1: " + teamCounts[1]);
            labels[4].setText("Temps: " + (elapsed / 1000) + "s");
            labels[5].setText("Logic Ticks: " + logicTickCount);
            labels[6].setText("Display Frames: " + displayFrameCount);
            labels[7].setText("Ratio: " + String.format("%.1f:1", logicFPS / Math.max(displayFPS, 1)));
        });
        statsTimer.start();
        
        return panel;
    }
    
    private static void startGameLoopWithLogs() {
        System.out.println("⏰ Démarrage des timers avec LOGS DÉTAILLÉS...");
        System.out.println();
        
        // Timer de LOGIQUE avec logs
        int logicDelay = 5; // 5ms = 200 Hz
        System.out.println("🔄 Timer LOGIQUE : " + logicDelay + "ms (cible: 200 Hz)");
        
        logicTimer = new javax.swing.Timer(logicDelay, e -> {
            long startLogic = System.nanoTime();
            
            try {
                // APPELER updateLogic() et MESURER le temps
                gameCanvas.updateLogic();
                logicTickCount++;
                
                long endLogic = System.nanoTime();
                long durationLogic = endLogic - startLogic;
                long intervalLogic = endLogic - lastLogicTime;
                lastLogicTime = endLogic;
                
                // Log tous les 200 ticks (1 seconde à 200 Hz)
                if (logicTickCount % 200 == 0) {
                    System.out.printf("⚡ LOGIQUE [Tick %d] : Durée=%.3fms | Intervalle=%.3fms | Hz réel=%.1f%n",
                        logicTickCount,
                        durationLogic / 1_000_000.0,
                        intervalLogic / 1_000_000.0,
                        1_000_000_000.0 / intervalLogic
                    );
                }
                
                // ALERTE si trop lent
                if (durationLogic > 10_000_000) { // > 10ms
                    System.err.printf("⚠️ LOGIQUE LENTE ! Tick %d a pris %.3fms (>10ms)%n",
                        logicTickCount, durationLogic / 1_000_000.0);
                }
                
            } catch (Exception ex) {
                System.err.println("❌ ERREUR dans updateLogic() : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        logicTimer.start();
        System.out.println("✅ Timer LOGIQUE démarré");
        
        // Timer d'AFFICHAGE avec logs
        int displayDelay = 16; // 16ms = 60 FPS
        System.out.println("🖼️  Timer AFFICHAGE : " + displayDelay + "ms (cible: 60 FPS)");
        
        displayTimer = new javax.swing.Timer(displayDelay, e -> {
            long startDisplay = System.nanoTime();
            
            try {
                // APPELER repaint() et MESURER le temps
                gameCanvas.repaint();
                displayFrameCount++;
                
                long endDisplay = System.nanoTime();
                long durationDisplay = endDisplay - startDisplay;
                long intervalDisplay = endDisplay - lastDisplayTime;
                lastDisplayTime = endDisplay;
                
                // Log tous les 60 frames (1 seconde à 60 FPS)
                if (displayFrameCount % 60 == 0) {
                    System.out.printf("🖼️  AFFICHAGE [Frame %d] : Durée=%.3fms | Intervalle=%.3fms | FPS réel=%.1f%n",
                        displayFrameCount,
                        durationDisplay / 1_000_000.0,
                        intervalDisplay / 1_000_000.0,
                        1_000_000_000.0 / intervalDisplay
                    );
                }
                
                // ALERTE si trop lent
                if (durationDisplay > 30_000_000) { // > 30ms
                    System.err.printf("⚠️ AFFICHAGE LENT ! Frame %d a pris %.3fms (>30ms)%n",
                        displayFrameCount, durationDisplay / 1_000_000.0);
                }
                
            } catch (Exception ex) {
                System.err.println("❌ ERREUR dans repaint() : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        displayTimer.start();
        System.out.println("✅ Timer AFFICHAGE démarré");
        System.out.println();
        
        // Thread de monitoring continu
        Thread monitorThread = new Thread(() -> {
            try {
                Thread.sleep(5000); // Attendre 5 secondes
                
                while (true) {
                    Thread.sleep(5000); // Tous les 5 secondes
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    double logicHz = logicTickCount / (elapsed / 1000.0);
                    double displayFps = displayFrameCount / (elapsed / 1000.0);
                    
                    System.out.println("═══════════════════════════════════════════════════════");
                    System.out.printf("📊 STATS GLOBALES [%ds écoulées]%n", elapsed / 1000);
                    System.out.printf("   ⚡ Logique : %.1f Hz (cible: 200 Hz) - %s%n", 
                        logicHz, 
                        logicHz >= 180 ? "✅ OK" : "❌ TROP LENT");
                    System.out.printf("   🖼️  Affichage : %.1f FPS (cible: 60 FPS) - %s%n", 
                        displayFps,
                        displayFps >= 50 ? "✅ OK" : "❌ TROP LENT");
                    System.out.printf("   📈 Ratio : %.1f:1 (cible: 3.3:1)%n", logicHz / displayFps);
                    
                    int[] teamCounts = gameCanvas.getTeamFighterCounts();
                    System.out.printf("   👥 Fighters : Team0=%d | Team1=%d | Total=%d%n",
                        teamCounts[0], teamCounts[1], teamCounts[0] + teamCounts[1]);
                    System.out.println("═══════════════════════════════════════════════════════");
                    System.out.println();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
}

