package fr.uparis.informatique.cpoo5.liquidwar.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.uparis.informatique.cpoo5.liquidwar.audio.AudioManager;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameOptions;
import fr.uparis.informatique.cpoo5.liquidwar.config.RenderConfig;
import fr.uparis.informatique.cpoo5.liquidwar.network.NetworkGameController;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;
import fr.uparis.informatique.cpoo5.liquidwar.view.GameCanvas;
import fr.uparis.informatique.cpoo5.liquidwar.view.GameRenderer;
import fr.uparis.informatique.cpoo5.liquidwar.view.GameStatsPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.PauseMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.VictoryPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.WaitingRoomPanel;

/**
 * Version réseau du jeu Liquid War.
 * 
 * <p>
 * Gère une partie multijoueur en réseau en mode serveur ou client.
 * Inclut une salle d'attente pour attendre les joueurs avant de démarrer.
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class NetworkLiquidWarGame {

    private GameCanvas gameCanvas;
    private GameStatsPanel statsPanel;
    private PauseMenuPanel pauseMenuPanel;
    private VictoryPanel victoryPanel;
    private WaitingRoomPanel waitingRoomPanel;
    private JFrame gameFrame;
    private JLayeredPane layeredPane;
    private JPanel mainPanel;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isWaiting = true; // En attente de joueurs
    private javax.swing.Timer logicTimer;
    private javax.swing.Timer displayTimer;
    private javax.swing.Timer statsTimer;
    private javax.swing.Timer connectionCheckTimer;

    private NetworkGameController networkController;
    private boolean isServer;
    private String host;
    private int port;
    private int minPlayers;

    private final GameLogger logger = GameLogger.getInstance();

    /**
     * Crée un nouveau jeu réseau.
     * 
     * @param isServer true pour créer un serveur, false pour se connecter
     * @param host     Adresse du serveur (ignoré si isServer=true)
     * @param port     Port du serveur
     */
    public NetworkLiquidWarGame(boolean isServer, String host, int port) {
        this(isServer, host, port, 2); // 2 joueurs par défaut
    }

    /**
     * Crée un nouveau jeu réseau avec nombre de joueurs minimum.
     * 
     * @param isServer   true pour créer un serveur, false pour se connecter
     * @param host       Adresse du serveur
     * @param port       Port du serveur
     * @param minPlayers Nombre minimum de joueurs pour démarrer
     */
    public NetworkLiquidWarGame(boolean isServer, String host, int port, int minPlayers) {
        this.isServer = isServer;
        this.host = host;
        this.port = port;
        this.minPlayers = minPlayers;
    }

    /**
     * Démarre le jeu en mode réseau.
     */
    public void start() {
        logger.info("\n==========================================");
        logger.info("🌐 MODE RÉSEAU " + (isServer ? "SERVEUR" : "CLIENT"));
        logger.info("Configuration:");
        logger.info("  - Adresse: %s:%d", host, port);
        logger.info("  - Joueurs minimum: %d", minPlayers);
        logger.info("==========================================\n");

        gameFrame = new JFrame("Liquid War - Mode Réseau (" + (isServer ? "Serveur" : "Client") + ")");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int contentWidth = GameConfig.MAP_WIDTH * RenderConfig.SCALE;
        int contentHeight = GameConfig.MAP_HEIGHT * RenderConfig.SCALE;

        // Initialiser le contrôleur réseau AVANT tout le reste
        try {
            networkController = new NetworkGameController(null);

            // Configurer le listener pour les mises à jour du lobby (côté client)
            networkController.setLobbyUpdateListener((connectedPlayers, serverMinPlayers) -> {
                // Mettre à jour sur l'EDT pour garantir la mise à jour de l'UI
                SwingUtilities.invokeLater(() -> {
                    // Mettre à jour le minPlayers avec la valeur du serveur
                    this.minPlayers = serverMinPlayers;
                    if (waitingRoomPanel != null) {
                        waitingRoomPanel.updateLobbyInfo(connectedPlayers, serverMinPlayers);
                    }
                });
            });

            if (isServer) {
                logger.info("Démarrage du serveur sur le port %d...", port);
                networkController.setMinPlayers(minPlayers); // Définir le minPlayers sur le serveur
                networkController.startServer(port);
                logger.info("✅ Serveur démarré - En attente de joueurs...");
            } else {
                logger.info("Connexion au serveur %s:%d...", host, port);
                networkController.connectToServer(host, port);
                logger.info("✅ Connecté au serveur");
            }
        } catch (IOException e) {
            logger.error("❌ Erreur réseau: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Impossible de démarrer le réseau:\n" + e.getMessage(),
                    "Erreur Réseau",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Créer la salle d'attente
        String displayAddress = isServer ? getLocalIPAddress() : host;
        waitingRoomPanel = new WaitingRoomPanel(isServer, displayAddress, port, minPlayers);
        waitingRoomPanel.setListener(new WaitingRoomPanel.WaitingRoomListener() {
            @Override
            public void onStartGame() {
                startActualGame();
            }

            @Override
            public void onCancel() {
                shutdown();
                SwingUtilities.invokeLater(() -> new MainWithMenu());
            }
        });

        // LayeredPane pour superposer les panneaux
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(contentWidth + 250, contentHeight));

        // Panneau d'attente (affiché en premier)
        waitingRoomPanel.setBounds(0, 0, contentWidth + 250, contentHeight);
        layeredPane.add(waitingRoomPanel, JLayeredPane.DEFAULT_LAYER);

        gameFrame.add(layeredPane);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
        waitingRoomPanel.requestFocus();

        // Timer pour vérifier les connexions (serveur seulement)
        // Les clients reçoivent les mises à jour via LOBBY_UPDATE
        connectionCheckTimer = new javax.swing.Timer(500, e -> {
            if (isWaiting && isServer) {
                int playerCount = networkController.getPlayerCount();
                waitingRoomPanel.setConnectedPlayers(playerCount);
            }
        });
        connectionCheckTimer.start();

        logger.info("⏳ Salle d'attente affichée - En attente de %d joueur(s)", minPlayers);
    }

    /**
     * Démarre la partie réelle une fois que tous les joueurs sont connectés.
     */
    private void startActualGame() {
        logger.info("🎮 Démarrage de la partie!");
        isWaiting = false;

        // Arrêter le timer de vérification
        if (connectionCheckTimer != null) {
            connectionCheckTimer.stop();
        }

        // Arrêter l'animation de la salle d'attente
        waitingRoomPanel.stopAnimation();

        int contentWidth = GameConfig.MAP_WIDTH * RenderConfig.SCALE;
        int contentHeight = GameConfig.MAP_HEIGHT * RenderConfig.SCALE;

        // Créer le GameCanvas SANS IA (mode réseau = pas d'IA)
        gameCanvas = new GameCanvas();
        gameCanvas.setNetworkMode(true); // Désactiver l'IA
        gameCanvas.setRequestedTeams(minPlayers); // Configurer le nombre d'équipes
        gameCanvas.reinitializeWithTeams(minPlayers); // Réinitialiser avec le bon nombre d'équipes
        gameCanvas.setPauseListener(() -> togglePause()); // ✅ Touche Échap pour pause !
        gameCanvas.setPreferredSize(new Dimension(contentWidth, contentHeight));

        logger.info("🎮 Partie configurée pour %d équipes", minPlayers);

        // Mettre à jour le NetworkGameController avec le vrai GameState
        networkController.setGameState(gameCanvas.getGameState());

        // Configurer l'équipe du joueur local
        int myTeamId = networkController.getMyTeamId();
        gameCanvas.setLocalTeamId(myTeamId);
        logger.info("🎮 Vous contrôlez l'équipe %d", myTeamId);

        // Créer le panneau de statistiques avec le bon nombre d'équipes
        statsPanel = new GameStatsPanel(minPlayers);
        statsPanel.setMenuButtonListener(e -> togglePause());

        // Panel principal
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(gameCanvas, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.EAST);

        // Menu pause
        pauseMenuPanel = new PauseMenuPanel();
        pauseMenuPanel.setSelectionListener(this::handlePauseMenuSelection);
        pauseMenuPanel.setVisible(false);

        // Panneau de victoire
        victoryPanel = new VictoryPanel();
        victoryPanel.setSelectionListener(this::handleVictorySelection);
        victoryPanel.setVisible(false);

        // Remplacer le contenu du layeredPane
        layeredPane.removeAll();

        mainPanel.setBounds(0, 0, contentWidth + 250, contentHeight);
        pauseMenuPanel.setBounds(0, 0, contentWidth + 250, contentHeight);
        victoryPanel.setBounds(0, 0, contentWidth + 250, contentHeight);

        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(pauseMenuPanel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(victoryPanel, JLayeredPane.POPUP_LAYER);

        layeredPane.revalidate();
        layeredPane.repaint();

        gameCanvas.requestFocus();

        // Démarrer les timers du jeu
        startGameTimers();

        // Démarrer la musique de manière asynchrone pour éviter les problèmes de timing
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

        // Notifier les clients que la partie commence
        if (isServer && networkController != null) {
            networkController.broadcastGameStart();
        }

        logger.info("✅ Partie réseau démarrée avec succès!");
    }

    /**
     * Obtient l'adresse IP locale pour l'affichage.
     */
    private String getLocalIPAddress() {
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * Démarre les timers du jeu.
     */
    private void startGameTimers() {
        // Timer de logique (vitesse ajustée selon les options)
        int logicDelay = GameOptions.getInstance().getLogicTimerDelay();
        logger.info("⏩ Vitesse du jeu : %d%% (délai logique : %dms)",
                GameOptions.getInstance().getGameSpeed(), logicDelay);

        logicTimer = new javax.swing.Timer(logicDelay, e -> {
            if (!isPaused && !isWaiting) {
                for (int i = 0; i < GameConfig.LOGIC_TICKS_PER_TIMER_EVENT; i++) {
                    gameCanvas.updateLogic();

                    // Incrémenter le tick réseau
                    if (networkController != null) {
                        networkController.tick();
                    }

                    // Envoyer la position du curseur au réseau
                    if (networkController != null && gameCanvas.getCursorPosition() != null) {
                        Point cursor = gameCanvas.getCursorPosition();
                        networkController.sendCursorMove(cursor.x, cursor.y);
                    }

                    checkGameOver();
                }
            }
        });
        logicTimer.start();

        // Timer d'affichage
        displayTimer = new javax.swing.Timer(GameConfig.DISPLAY_TIMER_INTERVAL_MS, e -> {
            if (!isPaused && !isWaiting) {
                gameCanvas.repaint();
            }
        });
        displayTimer.start();

        // Timer de statistiques (supporte jusqu'à 4 équipes)
        statsTimer = new javax.swing.Timer(1000, e -> {
            if (!isPaused && !isWaiting && statsPanel != null) {
                int[] counts = gameCanvas.getTeamFighterCounts();
                statsPanel.updateStats(counts);
            }
        });
        statsTimer.start();
    }

    /**
     * Bascule entre pause et jeu.
     */
    private void togglePause() {
        if (isGameOver || isWaiting) {
            return;
        }

        isPaused = !isPaused;
        pauseMenuPanel.setVisible(isPaused);

        if (isPaused) {
            pauseMenuPanel.requestFocusInWindow();
            logger.info("⏸ Jeu en pause");
        } else {
            gameCanvas.requestFocusInWindow();
            logger.info("▶ Jeu repris");
        }
    }

    /**
     * Gère les sélections du menu pause.
     */
    private void handlePauseMenuSelection(String item) {
        logger.info("Menu pause: %s sélectionné", item);

        switch (item) {
            case "CONTINUER":
                togglePause();
                break;
            case "RECOMMENCER":
                JOptionPane.showMessageDialog(gameFrame,
                        "Impossible de recommencer en mode réseau.\n" +
                                "Veuillez quitter et créer une nouvelle partie.",
                        "Mode Réseau",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
            case "MENU PRINCIPAL":
                shutdown();
                SwingUtilities.invokeLater(() -> new MainWithMenu());
                break;
            case "QUITTER":
                int choice = JOptionPane.showConfirmDialog(gameFrame,
                        "Voulez-vous vraiment quitter ?",
                        "Quitter",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    shutdown();
                    System.exit(0);
                }
                break;
        }
    }

    /**
     * Vérifie si la partie est terminée (supporte 2-4 équipes).
     */
    private void checkGameOver() {
        if (isGameOver || isWaiting || statsPanel == null)
            return;

        // Utiliser la nouvelle méthode qui supporte plusieurs équipes
        int winningTeam = statsPanel.getWinningTeam();

        if (winningTeam >= 0) {
            String[] teamNames = { "ÉQUIPE BLEUE", "ÉQUIPE ROUGE", "ÉQUIPE VERTE", "ÉQUIPE JAUNE" };
            announceWinner(teamNames[winningTeam], winningTeam);
        }
    }

    /**
     * Annonce le gagnant.
     */
    private void announceWinner(String teamName, int teamNumber) {
        isGameOver = true;
        isPaused = true;

        String duration = statsPanel.getElapsedTime();

        // Déterminer la couleur du gagnant (utilise les couleurs personnalisées si
        // définies)
        // Supporte jusqu'à 4 équipes : Bleu, Rouge, Vert, Jaune
        Color[] teamColors = GameRenderer.getCurrentTeamColors();
        Color winnerColor = (teamNumber >= 0 && teamNumber < teamColors.length)
                ? teamColors[teamNumber]
                : RenderConfig.TEAM_COLORS[teamNumber];

        victoryPanel.setVictoryInfo(winnerColor, teamName, duration);
        victoryPanel.resetSelection();
        victoryPanel.setVisible(true);
        victoryPanel.requestFocusInWindow();

        logger.info("👑 Victoire de %s après %s", teamName, duration);
    }

    /**
     * Gère les sélections du panneau de victoire.
     */
    private void handleVictorySelection(String item) {
        logger.info("Victoire: %s sélectionné", item);

        switch (item) {
            case "RECOMMENCER":
                JOptionPane.showMessageDialog(gameFrame,
                        "Impossible de recommencer en mode réseau.\n" +
                                "Veuillez créer une nouvelle partie.",
                        "Mode Réseau",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
            case "MENU PRINCIPAL":
                shutdown();
                SwingUtilities.invokeLater(() -> new MainWithMenu());
                break;
            case "QUITTER":
                shutdown();
                System.exit(0);
                break;
        }
    }

    /**
     * Arrête proprement le jeu et le réseau.
     */
    private void shutdown() {
        logger.info("Arrêt du jeu réseau...");

        // Arrêter les timers
        if (connectionCheckTimer != null)
            connectionCheckTimer.stop();
        if (logicTimer != null)
            logicTimer.stop();
        if (displayTimer != null)
            displayTimer.stop();
        if (statsTimer != null)
            statsTimer.stop();

        // Arrêter le réseau
        if (networkController != null) {
            networkController.shutdown();
        }

        // Arrêter l'audio
        AudioManager.getInstance().shutdown();

        // Fermer la fenêtre
        if (gameFrame != null) {
            gameFrame.dispose();
        }

        logger.info("Jeu réseau arrêté");
    }
}
