 package fr.uparis.informatique.cpoo5.liquidwar.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fr.uparis.informatique.cpoo5.liquidwar.audio.AudioManager;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameOptions;
import fr.uparis.informatique.cpoo5.liquidwar.config.RenderConfig;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;
import fr.uparis.informatique.cpoo5.liquidwar.view.GameCanvas;
import fr.uparis.informatique.cpoo5.liquidwar.view.GameRenderer;
import fr.uparis.informatique.cpoo5.liquidwar.view.GameStatsPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.PauseMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.VictoryPanel;

/**
 * Point d'entrée principal du jeu Liquid War.
 * 
 * REFACTORING : Classe simplifiée - la logique du jeu a été déplacée vers
 * GameCanvas.
 * Ce fichier ne contient plus que l'initialisation de la fenêtre et le
 * démarrage du jeu.
 * 
 * Architecture :
 * - LiquidWarGame : Point d'entrée et configuration de la fenêtre
 * - GameCanvas (view/) : Logique de jeu et rendu
 * - GameConfig/RenderConfig (config/) : Constantes
 * - Fighter/Cursor (model/entities/) : Entités
 */
public class LiquidWarGame {
    // Constantes déplacées vers RenderConfig pour meilleure organisation
    private static final int SCALE = RenderConfig.SCALE;
    private static final int GAME_WIDTH = RenderConfig.GAME_WIDTH;
    private static final int GAME_HEIGHT = RenderConfig.GAME_HEIGHT;

    private GameCanvas gameCanvas;
    private GameStatsPanel statsPanel;
    private PauseMenuPanel pauseMenuPanel;
    private VictoryPanel victoryPanel;
    private JFrame gameFrame;
    private JLayeredPane layeredPane;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private javax.swing.Timer logicTimer; // Timer pour la logique (rapide)
    private javax.swing.Timer displayTimer; // Timer pour l'affichage (fluide)
    private javax.swing.Timer statsTimer; // Timer pour les stats (1 fois par seconde)

    // Barre de stats en bas
    private JPanel bottomStatsBar;
    private JLabel[] bottomStatsLabels;
    private long startTime;
    private int logicTickCount = 0;
    private int displayFrameCount = 0;

    // Configuration du jeu (difficulté IA, etc.)
    private String aiDifficulty = "Moyen"; // Par défaut
    private String[] teamControlTypes = { "Flèches", "Souris", "ZQSD", "Flèches" }; // Types de contrôle par équipe (max
                                                                                    // 4)
    private String[] teamTypes = { "Humain", "IA", "IA", "IA" }; // Types d'équipe (Humain ou IA) (max 4)
    private int numTeams = 2; // Nombre d'équipes (2, 3 ou 4)
    private int[] fighterCounts = { 2000, 2000, 2000, 2000 }; // Nombre de combattants par équipe (max 4)
    private String pendingMapName = null; // Nom de la map choisie via le menu
    private String timeMode = "CHRONO"; // Mode de temps : "CHRONO" ou "MINUTERIE"
    private Integer timerDurationMinutes = null; // Durée en minutes (null = chrono)
    private long gameStartTime = 0; // Temps de début de partie (pour minuterie)

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LiquidWarGame().start();
        });
    }

    /**
     * Définir la difficulté de l'IA.
     * 
     * @param difficulty "Facile", "Moyen" ou "Difficile"
     */
    public void setAIDifficulty(String difficulty) {
        this.aiDifficulty = difficulty;
        GameLogger.getInstance().info("╔════════════════════════════════════════════╗");
        GameLogger.getInstance().info("║  🤖 DIFFICULTÉ IA CONFIGURÉE: %s       ║", difficulty);
        GameLogger.getInstance().info("╚════════════════════════════════════════════╝");
    }

    /**
     * Définir le nom de la map à charger.
     * 
     * @param mapName Nom de la map (sans extension), ou null pour la map par défaut
     */
    public void setMapName(String mapName) {
        this.pendingMapName = mapName;
        if (gameCanvas != null) {
            gameCanvas.setMapName(mapName);
        }
    }

    /**
     * Définir le mode de temps (Chrono ou Minuterie).
     * 
     * @param mode            "CHRONO" ou "MINUTERIE"
     * @param durationMinutes Durée en minutes (null si mode CHRONO)
     */
    public void setTimeMode(String mode, Integer durationMinutes) {
        this.timeMode = mode;
        this.timerDurationMinutes = durationMinutes;
        GameLogger.getInstance().info("⏱️ Mode de temps: %s%s", mode,
                durationMinutes != null ? " (" + durationMinutes + " minutes)" : "");
    }

    /**
     * Définir les types de contrôle pour chaque équipe.
     * 
     * @param controlTypes Tableau de types : "Flèches", "Souris", ou "ZQSD"
     */
    public void setTeamControlTypes(String[] controlTypes) {
        if (controlTypes != null && controlTypes.length >= 2) {
            // Copier les valeurs dans le tableau existant (max 4 équipes)
            for (int i = 0; i < Math.min(controlTypes.length, 4); i++) {
                this.teamControlTypes[i] = controlTypes[i];
            }
            GameLogger.getInstance().info("╔════════════════════════════════════════════╗");
            GameLogger.getInstance().info("║  🎮 CONTRÔLES CONFIGURÉS                   ║");
            for (int i = 0; i < controlTypes.length; i++) {
                GameLogger.getInstance().info("║  Équipe %d: %s                        ║", i + 1, controlTypes[i]);
            }
            GameLogger.getInstance().info("╚════════════════════════════════════════════╝");
        }
    }

    /**
     * Définir les types d'équipe (Humain ou IA).
     * 
     * @param types Tableau de types : "Humain" ou "IA"
     */
    public void setTeamTypes(String[] types) {
        if (types != null && types.length >= 2) {
            // Copier les valeurs dans le tableau existant (max 4 équipes)
            for (int i = 0; i < Math.min(types.length, 4); i++) {
                this.teamTypes[i] = types[i];
            }
            GameLogger.getInstance().info("╔════════════════════════════════════════════╗");
            GameLogger.getInstance().info("║  👥 TYPES D'ÉQUIPE CONFIGURÉS              ║");
            for (int i = 0; i < types.length; i++) {
                GameLogger.getInstance().info("║  Équipe %d: %s                        ║", i + 1, types[i]);
            }
            GameLogger.getInstance().info("╚════════════════════════════════════════════╝");
        }
    }

    /**
     * Définir le nombre d'équipes (2, 3 ou 4).
     * 
     * @param num Nombre d'équipes
     */
    public void setNumTeams(int num) {
        this.numTeams = Math.max(2, Math.min(4, num));
        GameLogger.getInstance().info("╔════════════════════════════════════════════╗");
        GameLogger.getInstance().info("║  👥 NOMBRE D'ÉQUIPES: %d                    ║", this.numTeams);
        GameLogger.getInstance().info("╚════════════════════════════════════════════╝");
    }

    /**
     * Définir le nombre de combattants par équipe.
     * 
     * @param counts Tableau du nombre de combattants par équipe
     */
    public void setFighterCounts(int[] counts) {
        if (counts != null && counts.length >= 2) {
            for (int i = 0; i < Math.min(counts.length, 4); i++) {
                this.fighterCounts[i] = counts[i];
            }
            GameLogger.getInstance().info("Combattants par équipe configurés");
            for (int i = 0; i < counts.length; i++) {
                GameLogger.getInstance().info("Équipe %d: %d combattants", i + 1, counts[i]);
            }
        }
    }

    public void start() {
        GameLogger logger = GameLogger.getInstance();
        logger.info("\n==========================================");
        logger.info("Configuration fenêtre:");
        logger.info("  - Taille map: %dx%d pixels (taille réelle de 2d.bmp)",
                GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT);
        logger.info("  - Zoom: x%d", RenderConfig.SCALE);
        logger.info("  - Taille GameCanvas: %dx%d",
                GameConfig.MAP_WIDTH * RenderConfig.SCALE,
                GameConfig.MAP_HEIGHT * RenderConfig.SCALE);
        logger.info("  - Taille fenêtre (avec bordures): %dx%d", GAME_WIDTH, GAME_HEIGHT);
        logger.info("==========================================\n");

        gameFrame = new JFrame("Liquid War - Map 2d (" + GameConfig.MAP_WIDTH + "x" + GameConfig.MAP_HEIGHT
                + ") - Zoom x" + RenderConfig.SCALE + "");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Créer le GameCanvas avec la taille exacte (sans les bordures)
        gameCanvas = new GameCanvas();
        // Appliquer la map choisie (si définie avant start())
        if (pendingMapName != null) {
            gameCanvas.setMapName(pendingMapName);
        }
        // Ordre critique : setFighterCounts AVANT setAIDifficulty
        // setAIDifficulty appelle initializeGame() qui a besoin des fighterCounts
        gameCanvas.setFighterCounts(fighterCounts);
        gameCanvas.setTeamControlTypes(teamControlTypes);
        gameCanvas.setTeamTypes(teamTypes);
        gameCanvas.setRequestedTeams(numTeams);
        gameCanvas.setAIDifficulty(aiDifficulty);
        gameCanvas.setPauseListener(() -> togglePause());
        gameCanvas.initialize();
        int contentWidth = GameConfig.MAP_WIDTH * RenderConfig.SCALE;
        int contentHeight = GameConfig.MAP_HEIGHT * RenderConfig.SCALE;
        gameCanvas.setPreferredSize(new java.awt.Dimension(contentWidth, contentHeight));

        // Créer le panneau de statistiques à droite avec le nombre d'équipes correct
        statsPanel = new GameStatsPanel(numTeams);
        // Configurer le mode de temps dans le panneau de stats
        statsPanel.setTimeMode(timeMode, timerDurationMinutes);
        statsPanel.setMenuButtonListener(e -> togglePause());
        // Synchroniser le temps de début avec le jeu
        statsPanel.setGameStartTime(gameStartTime);

        // Créer la barre de stats en bas
        bottomStatsBar = createBottomStatsBar();
        startTime = System.currentTimeMillis();
        gameStartTime = System.currentTimeMillis(); // Pour la minuterie

        // Synchroniser le temps de début avec le panneau de stats (après sa création)
        // IMPORTANT : Doit être fait APRÈS avoir défini gameStartTime
        if (statsPanel != null) {
            statsPanel.setGameStartTime(gameStartTime);
        }

        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel pour le jeu + stats droite
        JPanel gameAndStatsPanel = new JPanel(new BorderLayout());
        gameAndStatsPanel.add(gameCanvas, BorderLayout.CENTER);
        gameAndStatsPanel.add(statsPanel, BorderLayout.EAST);

        mainPanel.add(gameAndStatsPanel, BorderLayout.CENTER);
        mainPanel.add(bottomStatsBar, BorderLayout.SOUTH);

        // Créer le menu pause (affiché par-dessus avec LayeredPane)
        pauseMenuPanel = new PauseMenuPanel();
        pauseMenuPanel.setSelectionListener(this::handlePauseMenuSelection);
        pauseMenuPanel.setVisible(false);

        // LayeredPane pour afficher le menu pause par-dessus
        layeredPane = new JLayeredPane();
        int totalHeight = contentHeight + 50; // +50 pour la barre de stats en bas
        layeredPane.setPreferredSize(new java.awt.Dimension(
                contentWidth + 250, // +250 pour le panneau de stats
                totalHeight));

        mainPanel.setBounds(0, 0, contentWidth + 250, totalHeight);
        pauseMenuPanel.setBounds(0, 0, contentWidth + 250, totalHeight);

        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(pauseMenuPanel, JLayeredPane.PALETTE_LAYER);

        // Créer le panneau de victoire (au-dessus de tout)
        victoryPanel = new VictoryPanel();
        victoryPanel.setSelectionListener(this::handleVictorySelection);
        victoryPanel.setVisible(false);
        victoryPanel.setBounds(0, 0, contentWidth + 250, totalHeight);
        layeredPane.add(victoryPanel, JLayeredPane.POPUP_LAYER);

        gameFrame.add(layeredPane);
        gameFrame.pack(); // Ajuster la fenêtre à la taille du contenu

        logger.info("Fenêtre créée:");
        logger.info("  - Taille totale fenêtre: %dx%d", gameFrame.getWidth(), gameFrame.getHeight());
        logger.info("  - Taille zone affichage: %dx%d", gameCanvas.getWidth(), gameCanvas.getHeight());
        logger.info("");

        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
        gameCanvas.requestFocus();

        // SÉPARATION LOGIQUE/AFFICHAGE - OPTIMISÉ
        // Logique : ~120 ticks/seconde (synchronisé avec l'affichage)
        // La vitesse est ajustée selon les options du jeu
        int logicDelay = GameOptions.getInstance().getLogicTimerDelay();
        logger.info("⏩ Vitesse du jeu : %d%% (délai logique : %dms)",
                GameOptions.getInstance().getGameSpeed(), logicDelay);

        logicTimer = new javax.swing.Timer(logicDelay, e -> {
            if (!isPaused) {
                // Exécuter la logique (1 tick par appel pour ne pas saturer le CPU)
                for (int i = 0; i < GameConfig.LOGIC_TICKS_PER_TIMER_EVENT; i++) {
                    gameCanvas.updateLogic();
                    logicTickCount++;

                    // Vérifier la fin de partie
                    checkGameOver();
                }
            }
        });
        logicTimer.start();

        // Affichage : ~120 FPS pour fluidité maximale
        displayTimer = new javax.swing.Timer(GameConfig.DISPLAY_TIMER_INTERVAL_MS, e -> {
            if (!isPaused) {
                // Force un repaint immédiat pour fluidité maximale
                gameCanvas.repaint();
                displayFrameCount++;
                // Optionnel : forcer la synchronisation (décommenter si besoin)
                // java.awt.Toolkit.getDefaultToolkit().sync();
            }
        });
        displayTimer.start();

        // Timer pour mettre à jour les statistiques (1 fois par seconde)
        statsTimer = new javax.swing.Timer(1000, e -> {
            if (!isPaused) {
                int[] counts = gameCanvas.getTeamFighterCounts();
                statsPanel.updateStats(counts); // Passer le tableau complet pour toutes les équipes
            }
        });
        statsTimer.start();

        // Démarrer la musique de fond de manière asynchrone pour éviter les problèmes
        // de timing
        // (comme dans le code C - start_music() est appelé après l'initialisation
        // complète)
        new Thread(() -> {
            try {
                // Petit délai pour laisser le jeu s'initialiser complètement
                Thread.sleep(200);
                AudioManager.getInstance().playMusic("/music/fodder.mid");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Interruption lors du lancement de la musique");
            }
        }).start();
    }

    /**
     * Bascule entre pause et jeu
     */
    private void togglePause() {
        // Ne pas permettre de mettre en pause si le jeu est terminé
        if (isGameOver) {
            return;
        }

        isPaused = !isPaused;
        pauseMenuPanel.setVisible(isPaused);

        if (isPaused) {
            pauseMenuPanel.requestFocusInWindow();
            GameLogger.getInstance().info("⏸ Jeu en pause");
        } else {
            gameCanvas.requestFocusInWindow();
            GameLogger.getInstance().info("▶ Jeu repris");
        }
    }

    /**
     * Gère les sélections du menu pause
     */
    private void handlePauseMenuSelection(String item) {
        GameLogger.getInstance().info("Menu pause: %s sélectionné", item);

        switch (item) {
            case "CONTINUER":
                togglePause();
                break;
            case "RECOMMENCER":
                isPaused = false;
                pauseMenuPanel.setVisible(false);
                restartGame();
                break;
            case "OPTIONS":
                // Afficher le menu options dans la même fenêtre
                showOptionsMenu();
                break;
            case "MENU PRINCIPAL":
                logicTimer.stop();
                displayTimer.stop();
                statsTimer.stop();
                AudioManager.getInstance().shutdown();
                gameFrame.dispose();
                SwingUtilities.invokeLater(() -> {
                    new MainWithMenu();
                });
                break;
            case "QUITTER":
                int choice = JOptionPane.showConfirmDialog(gameFrame,
                        "Voulez-vous vraiment quitter ?",
                        "Quitter",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
                break;
        }
    }

    /**
     * Affiche le menu des options en overlay
     */
    private void showOptionsMenu() {
        // Créer un panneau d'options temporaire
        fr.uparis.informatique.cpoo5.liquidwar.view.menu.OptionsMenuPanel optionsPanel = new fr.uparis.informatique.cpoo5.liquidwar.view.menu.OptionsMenuPanel();

        // Sauvegarder les valeurs actuelles pour pouvoir annuler
        optionsPanel.saveCurrentValues();

        optionsPanel.setNavigationListener(() -> {
            // Mettre à jour la vitesse du timer si elle a changé
            updateGameSpeed();

            // Retour au menu pause
            layeredPane.remove(optionsPanel);
            pauseMenuPanel.setVisible(true);
            pauseMenuPanel.requestFocusInWindow();
            layeredPane.repaint();
        });

        // Masquer le menu pause
        pauseMenuPanel.setVisible(false);

        // Afficher le menu options
        optionsPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        layeredPane.add(optionsPanel, JLayeredPane.POPUP_LAYER);
        optionsPanel.setVisible(true);
        optionsPanel.requestFocusInWindow();
    }

    /**
     * 🆕 Met à jour la vitesse du timer de logique selon les options
     */
    private void updateGameSpeed() {
        int newDelay = GameOptions.getInstance().getLogicTimerDelay();
        int currentDelay = logicTimer.getDelay();

        if (newDelay != currentDelay) {
            logicTimer.setDelay(newDelay);
            GameLogger.getInstance().info("⏩ Vitesse du jeu mise à jour : %d%% (délai: %dms)",
                    GameOptions.getInstance().getGameSpeed(), newDelay);
            System.out.println("⚡ Timer de logique ajusté : " + newDelay + "ms (vitesse: " +
                    GameOptions.getInstance().getGameSpeed() + "%)");
        }
    }

    /**
     * Redémarre le jeu en conservant tous les paramètres actuels
     */
    private void restartGame() {
        logicTimer.stop();
        displayTimer.stop();
        statsTimer.stop();
        gameFrame.dispose();

        // Sauvegarder les paramètres actuels
        final String savedAiDifficulty = this.aiDifficulty;
        final String[] savedTeamControlTypes = this.teamControlTypes.clone();
        final String[] savedTeamTypes = this.teamTypes.clone();
        final int savedNumTeams = this.numTeams;
        final int[] savedFighterCounts = this.fighterCounts.clone();
        final String savedMapName = (gameCanvas != null) ? gameCanvas.getMapName() : null;

        SwingUtilities.invokeLater(() -> {
            LiquidWarGame newGame = new LiquidWarGame();
            // Restaurer tous les paramètres
            newGame.setAIDifficulty(savedAiDifficulty);
            newGame.setTeamControlTypes(savedTeamControlTypes);
            newGame.setTeamTypes(savedTeamTypes);
            newGame.setNumTeams(savedNumTeams);
            newGame.setFighterCounts(savedFighterCounts);
            // Restaurer la map
            if (savedMapName != null) {
                newGame.setMapName(savedMapName);
            }
            newGame.start();
        });
    }

    /**
     * Vérifie si la partie est terminée (une seule équipe restante)
     */
    private void checkGameOver() {
        if (isGameOver)
            return; // Ne pas vérifier si le jeu est déjà terminé

        // Vérifier d'abord si le mode minuterie est activé et si le temps est écoulé
        if ("MINUTERIE".equals(timeMode) && timerDurationMinutes != null && gameStartTime > 0) {
            long elapsedMillis = System.currentTimeMillis() - gameStartTime;
            long elapsedMinutes = elapsedMillis / 60000;

            if (elapsedMinutes >= timerDurationMinutes) {
                // Le temps est écoulé, déterminer le gagnant selon le nombre de particules
                int[] teamFighterCounts = gameCanvas.getTeamFighterCounts();
                if (teamFighterCounts != null) {
                    int maxCount = -1;
                    int winningTeam = -1;
                    int teamsWithMaxCount = 0;

                    // Trouver le score maximum
                    for (int i = 0; i < teamFighterCounts.length; i++) {
                        if (teamFighterCounts[i] > maxCount) {
                            maxCount = teamFighterCounts[i];
                        }
                    }

                    // Compter combien d'équipes ont ce score maximum
                    for (int i = 0; i < teamFighterCounts.length; i++) {
                        if (teamFighterCounts[i] == maxCount && maxCount >= 0) {
                            teamsWithMaxCount++;
                            if (winningTeam < 0) {
                                winningTeam = i; // Première équipe avec le score max
                            }
                        }
                    }

                    if (teamsWithMaxCount > 1) {
                        // Égalité : plusieurs équipes ont le même score maximum
                        announceTie(teamFighterCounts);
                    } else if (winningTeam >= 0) {
                        // Il y a un gagnant unique
                        String[] teamNames = { "ÉQUIPE BLEUE", "ÉQUIPE ROUGE", "ÉQUIPE VERTE", "ÉQUIPE JAUNE" };
                        announceWinner(teamNames[winningTeam], winningTeam);
                    }
                }
                return;
            }
        }

        // Vérification normale : une seule équipe restante
        // Utiliser getWinningTeam() qui supporte jusqu'à 4 équipes
        int winningTeam = statsPanel.getWinningTeam();

        if (winningTeam >= 0) {
            String[] teamNames = { "ÉQUIPE BLEUE", "ÉQUIPE ROUGE", "ÉQUIPE VERTE", "ÉQUIPE JAUNE" };
            announceWinner(teamNames[winningTeam], winningTeam);
        }
    }

    /**
     * Annonce le gagnant avec un panneau de victoire élégant (comme le menu pause)
     */
    private void announceWinner(String teamName, int teamNumber) {
        isGameOver = true;
        isPaused = true;

        // Récupérer la durée de la partie
        String duration = statsPanel.getElapsedTime();

        // Déterminer la couleur du gagnant (utilise les couleurs personnalisées si
        // définies)
        // Supporte jusqu'à 4 équipes : Bleu, Rouge, Vert, Jaune
        Color[] teamColors = GameRenderer.getCurrentTeamColors();
        Color winnerColor = (teamNumber >= 0 && teamNumber < teamColors.length)
                ? teamColors[teamNumber]
                : RenderConfig.TEAM_COLORS[teamNumber];

        // Configurer et afficher le panneau de victoire
        victoryPanel.setVictoryInfo(winnerColor, teamName, duration);
        victoryPanel.resetSelection();
        victoryPanel.setVisible(true);
        victoryPanel.requestFocusInWindow();

        GameLogger.getInstance().info("👑 Victoire de %s après %s", teamName, duration);
    }

    /**
     * Annonce une égalité (mode minuterie avec scores égaux).
     */
    private void announceTie(int[] teamFighterCounts) {
        isGameOver = true;
        isPaused = true;

        // Récupérer la durée de la partie
        String duration = statsPanel.getElapsedTime();

        // Configurer et afficher le panneau de victoire avec égalité
        victoryPanel.setTieInfo(teamFighterCounts, duration, timeMode);
        victoryPanel.resetSelection();
        victoryPanel.setVisible(true);
        victoryPanel.requestFocusInWindow();

        GameLogger.getInstance().info("🤝 ÉGALITÉ après %s", duration);
    }

    /**
     * Gère les sélections du panneau de victoire
     */
    private void handleVictorySelection(String item) {
        GameLogger.getInstance().info("Victoire: %s sélectionné", item);

        switch (item) {
            case "RECOMMENCER":
                victoryPanel.setVisible(false);
                isGameOver = false;
                isPaused = false;
                restartGame();
                break;
            case "MENU PRINCIPAL":
                // Stopper complètement le jeu avant de retourner au menu
                victoryPanel.setVisible(false);
                isGameOver = false;
                logicTimer.stop();
                displayTimer.stop();
                statsTimer.stop();
                AudioManager.getInstance().shutdown();
                gameFrame.dispose();
                SwingUtilities.invokeLater(() -> {
                    new MainWithMenu();
                });
                break;
            case "QUITTER":
                victoryPanel.setVisible(false);
                logicTimer.stop();
                displayTimer.stop();
                statsTimer.stop();
                AudioManager.getInstance().shutdown();
                gameFrame.dispose();
                System.exit(0);
                break;
        }
    }

    /**
     * 🆕 Crée la barre de statistiques en bas (comme dans debug_run.sh)
     */
    private JPanel createBottomStatsBar() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(Color.BLACK);

        bottomStatsLabels = new JLabel[8];
        for (int i = 0; i < bottomStatsLabels.length; i++) {
            bottomStatsLabels[i] = new JLabel("", SwingConstants.CENTER);
            bottomStatsLabels[i].setForeground(Color.GREEN);
            bottomStatsLabels[i].setFont(new Font("Monospaced", Font.BOLD, 12));
            panel.add(bottomStatsLabels[i]);
        }

        // Timer pour mettre à jour les stats visuelles (10 fois par seconde)
        javax.swing.Timer bottomStatsTimer = new javax.swing.Timer(100, e -> {
            if (isPaused)
                return; // Ne pas mettre à jour en pause

            long elapsed = System.currentTimeMillis() - startTime;
            double logicFPS = logicTickCount / (elapsed / 1000.0);
            double displayFPS = displayFrameCount / (elapsed / 1000.0);

            int[] teamCounts = gameCanvas.getTeamFighterCounts();

            bottomStatsLabels[0].setText("Logique: " + String.format("%.1f Hz", logicFPS));
            bottomStatsLabels[1].setText("Affichage: " + String.format("%.1f FPS", displayFPS));
            bottomStatsLabels[2].setText("Team 0: " + teamCounts[0]);
            bottomStatsLabels[3].setText("Team 1: " + teamCounts[1]);
            bottomStatsLabels[4].setText("Temps: " + (elapsed / 1000) + "s");
            bottomStatsLabels[5].setText("Logic Ticks: " + logicTickCount);
            bottomStatsLabels[6].setText("Display Frames: " + displayFrameCount);
            bottomStatsLabels[7].setText("Ratio: " + String.format("%.1f:1", logicFPS / Math.max(displayFPS, 1)));
        });
        bottomStatsTimer.start();

        return panel;
    }
}
