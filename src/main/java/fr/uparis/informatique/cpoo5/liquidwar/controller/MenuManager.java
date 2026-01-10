package fr.uparis.informatique.cpoo5.liquidwar.controller;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.uparis.informatique.cpoo5.liquidwar.config.RenderConfig;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.ClientConnectPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.GameModeMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.HelpMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.MainMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.MapSelectionMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.NetworkMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.OptionsMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.ServerSetupPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.TeamConfigMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.TimeModeMenuPanel;
import fr.uparis.informatique.cpoo5.liquidwar.view.menu.TrioConfigMenuPanel;

/**
 * Gestionnaire des menus du jeu.
 * 
 * Gère la navigation entre les différents menus et le lancement du jeu.
 */
public class MenuManager {

    private JFrame menuFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    // Les différents menus
    private MainMenuPanel mainMenuPanel;
    private TimeModeMenuPanel timeModeMenuPanel;
    private GameModeMenuPanel gameModeMenuPanel;
    private TeamConfigMenuPanel teamConfigMenuPanel;
    private TrioConfigMenuPanel trioConfigMenuPanel;
    private MapSelectionMenuPanel mapSelectionMenuPanel;
    private OptionsMenuPanel optionsMenuPanel;
    private HelpMenuPanel helpMenuPanel;
    private NetworkMenuPanel networkMenuPanel;
    private ServerSetupPanel serverSetupPanel;
    private ClientConnectPanel clientConnectPanel;

    // État actuel
    private String selectedTimeMode = "CHRONO"; // Par défaut : chrono
    private Integer selectedDurationMinutes = null; // null = chrono, sinon durée en minutes
    private String selectedGameMode;
    private TeamConfigMenuPanel.TeamConfiguration pendingTeamConfig;
    private TrioConfigMenuPanel.TrioConfiguration pendingTrioConfig;

    // Noms des cartes
    private static final String MAIN_MENU = "MAIN";
    private static final String TIME_MODE = "TIME_MODE";
    private static final String GAME_MODE = "MODE";
    private static final String TEAM_CONFIG = "CONFIG";
    private static final String TRIO_CONFIG = "TRIO_CONFIG";
    private static final String MAP_SELECTION = "MAP_SELECTION";
    private static final String OPTIONS = "OPTIONS";
    private static final String HELP = "HELP";
    private static final String NETWORK = "NETWORK";
    private static final String SERVER_SETUP = "SERVER_SETUP";
    private static final String CLIENT_CONNECT = "CLIENT_CONNECT";

    public MenuManager() {
        initializeMenus();
    }

    /**
     * Initialise tous les menus
     */
    private void initializeMenus() {
        // Créer la fenêtre principale avec la même taille que le jeu
        menuFrame = new JFrame("Liquid War - Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Utiliser la même taille que la fenêtre de jeu (avec panneau stats)
        int menuWidth = RenderConfig.GAME_WIDTH + 250; // Même largeur que le jeu avec stats
        int menuHeight = RenderConfig.GAME_HEIGHT;
        menuFrame.setSize(menuWidth, menuHeight);
        menuFrame.setLocationRelativeTo(null);

        // CardLayout pour changer de menu
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Créer les menus
        createMainMenu();
        createTimeModeMenu();
        createGameModeMenu();
        createTeamConfigMenu();
        createTrioConfigMenu();
        createMapSelectionMenu();
        createOptionsMenu();
        createHelpMenu();
        createNetworkMenus();

        // Ajouter le panel à la fenêtre
        menuFrame.add(cardPanel);
        menuFrame.setVisible(true);

        // Forcer la fenêtre au premier plan
        menuFrame.toFront();
        menuFrame.requestFocus();
        // Utiliser setAlwaysOnTop temporairement pour forcer l'affichage
        menuFrame.setAlwaysOnTop(true);
        // Après un court délai, désactiver alwaysOnTop (pour permettre l'interaction
        // normale)
        javax.swing.Timer timer = new javax.swing.Timer(500, e -> {
            menuFrame.setAlwaysOnTop(false);
            menuFrame.toFront();
            menuFrame.requestFocus();
            // S'assurer que le panel du menu a le focus de manière asynchrone
            SwingUtilities.invokeLater(() -> {
                if (mainMenuPanel != null) {
                    mainMenuPanel.requestFocusInWindow();
                    // Si requestFocusInWindow ne fonctionne pas, forcer avec requestFocus
                    if (!mainMenuPanel.hasFocus()) {
                        mainMenuPanel.requestFocus();
                    }
                }
            });
        });
        timer.setRepeats(false);
        timer.start();

        // Afficher le menu principal
        showMainMenu();
    }

    /**
     * Crée le menu principal
     */
    private void createMainMenu() {
        mainMenuPanel = new MainMenuPanel();
        mainMenuPanel.setSelectionListener(item -> {
            System.out.println("Menu principal: " + item + " sélectionné");

            switch (item) {
                case "JOUER":
                    showTimeModeMenu();
                    break;
                case "OPTIONS":
                    showOptionsMenu();
                    break;
                case "AIDE":
                    showHelpMenu();
                    break;
                case "QUITTER":
                    System.exit(0);
                    break;
            }
        });

        cardPanel.add(mainMenuPanel, MAIN_MENU);
    }

    /**
     * Crée le menu de sélection du mode de temps (Chrono/Minuterie)
     */

    private void createTimeModeMenu() {
        timeModeMenuPanel = new TimeModeMenuPanel();
        timeModeMenuPanel.setSelectionListener((mode, durationMinutes) -> {
            System.out.println("Mode de temps sélectionné: " + mode
                    + (durationMinutes != null ? " (" + durationMinutes + " min)" : ""));
            selectedTimeMode = mode;
            selectedDurationMinutes = durationMinutes;

            // Après avoir choisi le mode de temps, aller au menu de sélection du mode de
            // jeu
            showGameModeMenu();
        });
        timeModeMenuPanel.setNavigationListener(this::showMainMenu);

        cardPanel.add(timeModeMenuPanel, TIME_MODE);
    }

    /**
     * Affiche le menu de sélection du mode de temps
     */
    private void showTimeModeMenu() {
        cardLayout.show(cardPanel, TIME_MODE);
        timeModeMenuPanel.resetSelection();
        timeModeMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu de sélection du mode de temps affiché");
    }

    /**
     * Crée le menu de sélection du mode
     */
    private void createGameModeMenu() {
        gameModeMenuPanel = new GameModeMenuPanel();
        gameModeMenuPanel.setSelectionListener(mode -> {
            System.out.println("Mode sélectionné: " + mode);
            selectedGameMode = mode;

            // Si mode réseau, aller au menu réseau, sinon configuration d'équipes
            if ("MULTIJOUEUR RÉSEAU".equals(mode)) {
                showNetworkMenu();
            } else if ("TRIO LOCAL".equals(mode)) {
                showTrioConfigMenu();
            } else {
                showTeamConfigMenu();
            }
        });
        gameModeMenuPanel.setNavigationListener(this::showMainMenu);

        cardPanel.add(gameModeMenuPanel, GAME_MODE);
    }

    /**
     * Crée le menu de configuration des équipes
     */
    private void createTeamConfigMenu() {
        teamConfigMenuPanel = new TeamConfigMenuPanel();
        teamConfigMenuPanel.setCompleteListener(config -> {
            System.out.println("=== Configuration des équipes terminée ===");
            System.out.println("Mode: " + selectedGameMode);
            System.out.println("Équipe 1: " + config.teamTypes[0]);
            System.out.println("Équipe 2: " + config.teamTypes[1]);

            // Stocker la configuration et aller vers le menu de sélection de map
            pendingTeamConfig = config;
            showMapSelectionMenu();
        });
        teamConfigMenuPanel.setNavigationListener(this::showGameModeMenu);

        cardPanel.add(teamConfigMenuPanel, TEAM_CONFIG);
    }

    /**
     * Crée le menu de configuration pour le mode Trio Local (3 joueurs)
     */
    private void createTrioConfigMenu() {
        trioConfigMenuPanel = new TrioConfigMenuPanel();
        trioConfigMenuPanel.setCompleteListener(config -> {
            System.out.println("=== Configuration Trio terminée ===");
            System.out.println("Mode: " + selectedGameMode);
            System.out.println("Équipe 1: " + config.teamTypes[0]);
            System.out.println("Équipe 2: " + config.teamTypes[1]);
            System.out.println("Équipe 3: " + config.teamTypes[2]);

            // Stocker la configuration et aller vers le menu de sélection de map
            pendingTrioConfig = config;
            showMapSelectionMenu();
        });
        trioConfigMenuPanel.setNavigationListener(this::showGameModeMenu);

        cardPanel.add(trioConfigMenuPanel, TRIO_CONFIG);
    }

    /**
     * Crée le menu de sélection de map
     */
    private void createMapSelectionMenu() {
        mapSelectionMenuPanel = new MapSelectionMenuPanel();
        mapSelectionMenuPanel.setSelectionListener(mapName -> {
            System.out.println("=== Map sélectionnée: " + mapName + " ===");

            // Lancer le jeu avec la map sélectionnée
            if (pendingTeamConfig != null) {
                startGame(pendingTeamConfig, mapName);
            } else if (pendingTrioConfig != null) {
                startTrioGame(pendingTrioConfig, mapName);
            }
        });
        mapSelectionMenuPanel.setNavigationListener(() -> {
            // Retour vers le menu de configuration approprié
            if (pendingTeamConfig != null) {
                showTeamConfigMenu();
            } else if (pendingTrioConfig != null) {
                showTrioConfigMenu();
            }
        });

        cardPanel.add(mapSelectionMenuPanel, MAP_SELECTION);
    }

    /**
     * Crée le menu des options
     */
    private void createOptionsMenu() {
        optionsMenuPanel = new OptionsMenuPanel();
        optionsMenuPanel.setNavigationListener(this::showMainMenu);

        cardPanel.add(optionsMenuPanel, OPTIONS);
    }

    /**
     * Crée le menu d'aide
     */
    private void createHelpMenu() {
        helpMenuPanel = new HelpMenuPanel();
        helpMenuPanel.setNavigationListener(this::showMainMenu);

        cardPanel.add(helpMenuPanel, HELP);
    }

    /**
     * Affiche le menu principal
     */
    private void showMainMenu() {
        cardLayout.show(cardPanel, MAIN_MENU);
        // Demander le focus de manière asynchrone pour s'assurer qu'il est bien reçu
        SwingUtilities.invokeLater(() -> {
            mainMenuPanel.requestFocusInWindow();
            // Si requestFocusInWindow ne fonctionne pas, forcer avec requestFocus
            if (!mainMenuPanel.hasFocus()) {
                mainMenuPanel.requestFocus();
            }
        });
        System.out.println("→ Menu principal affiché");
    }

    /**
     * Affiche le menu de sélection du mode
     */
    private void showGameModeMenu() {
        cardLayout.show(cardPanel, GAME_MODE);
        gameModeMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu de sélection du mode affiché");
    }

    /**
     * Affiche le menu de configuration des équipes
     */
    private void showTeamConfigMenu() {
        cardLayout.show(cardPanel, TEAM_CONFIG);
        teamConfigMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu de configuration des équipes affiché");
    }

    /**
     * Affiche le menu de configuration Trio Local (3 joueurs)
     */
    private void showTrioConfigMenu() {
        cardLayout.show(cardPanel, TRIO_CONFIG);
        trioConfigMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu de configuration Trio Local affiché");
    }

    /**
     * Affiche le menu de sélection de map
     */
    private void showMapSelectionMenu() {
        cardLayout.show(cardPanel, MAP_SELECTION);
        mapSelectionMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu de sélection de map affiché");
    }

    /**
     * Affiche le menu des options
     */
    private void showOptionsMenu() {
        cardLayout.show(cardPanel, OPTIONS);
        optionsMenuPanel.saveCurrentValues(); // Sauvegarder les valeurs pour pouvoir annuler
        optionsMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu des options affiché");
    }

    /**
     * Affiche le menu d'aide
     */
    private void showHelpMenu() {
        cardLayout.show(cardPanel, HELP);
        helpMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu d'aide affiché");
    }

    /**
     * Crée les menus réseau
     */
    private void createNetworkMenus() {
        // Menu principal réseau
        networkMenuPanel = new NetworkMenuPanel();
        networkMenuPanel.setSelectionListener(item -> {
            System.out.println("Menu réseau: " + item + " sélectionné");

            switch (item) {
                case "CRÉER UNE PARTIE":
                    showServerSetupMenu();
                    break;
                case "REJOINDRE UNE PARTIE":
                    showClientConnectMenu();
                    break;
                case "RETOUR":
                    showGameModeMenu();
                    break;
            }
        });
        cardPanel.add(networkMenuPanel, NETWORK);

        // Menu configuration serveur
        serverSetupPanel = new ServerSetupPanel();
        serverSetupPanel.setListener(new ServerSetupPanel.ServerSetupListener() {
            @Override
            public void onStartServer(String serverName, int port, int minPlayers) {
                System.out.println("Démarrage du serveur \"" + serverName + "\" sur le port " + port +
                        " avec " + minPlayers + " joueurs minimum");

                // Vérifier à nouveau les doublons (au cas où un serveur aurait été créé entre
                // temps)
                fr.uparis.informatique.cpoo5.liquidwar.network.ServerRegistry registry = fr.uparis.informatique.cpoo5.liquidwar.network.ServerRegistry
                        .getInstance();
                if (registry.isNameTaken(serverName)) {
                    serverSetupPanel.showNameConflictError();
                    return;
                }
                if (registry.isPortTaken(port)) {
                    serverSetupPanel.showPortConflictError();
                    return;
                }

                // Démarrer le serveur (les erreurs seront gérées dans startNetworkGame)
                startNetworkGameWithErrorHandling(true, "localhost", port, minPlayers, serverName, serverSetupPanel);
            }

            @Override
            public void onBack() {
                showNetworkMenu();
            }
        });
        cardPanel.add(serverSetupPanel, SERVER_SETUP);

        // Menu connexion client - utiliser ServerListPanel
        clientConnectPanel = new ClientConnectPanel();
        clientConnectPanel.setListener(new ClientConnectPanel.ClientConnectListener() {
            @Override
            public void onConnect(String hostname, int port) {
                System.out.println("Connexion au serveur " + hostname + ":" + port);
                startNetworkGame(false, hostname, port, 2, null); // Le client ne décide pas du nombre de joueurs
            }

            @Override
            public void onBack() {
                showNetworkMenu();
            }
        });
        cardPanel.add(clientConnectPanel, CLIENT_CONNECT);

        // Démarrer la découverte réseau pour les clients (dès le démarrage du menu)
        // Cela permet de découvrir les serveurs même avant d'ouvrir le menu de
        // connexion
        // Utiliser une instance singleton pour éviter les conflits de port
        try {
            fr.uparis.informatique.cpoo5.liquidwar.network.ServerDiscovery discovery = new fr.uparis.informatique.cpoo5.liquidwar.network.ServerDiscovery();
            discovery.startListening();
            System.out.println("✅ Découverte réseau démarrée");
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de démarrer la découverte réseau: " + e.getMessage());
            // Continuer sans découverte (mode manuel toujours possible)
        }
    }

    /**
     * Affiche le menu réseau principal
     */
    private void showNetworkMenu() {
        cardLayout.show(cardPanel, NETWORK);
        networkMenuPanel.resetSelection();
        networkMenuPanel.requestFocusInWindow();
        System.out.println("→ Menu réseau affiché");
    }

    /**
     * Affiche le menu de configuration serveur
     */
    private void showServerSetupMenu() {
        cardLayout.show(cardPanel, SERVER_SETUP);
        serverSetupPanel.requestFocusInWindow();
        System.out.println("→ Menu configuration serveur affiché");
    }

    /**
     * Affiche le menu de connexion client
     */
    private void showClientConnectMenu() {
        cardLayout.show(cardPanel, CLIENT_CONNECT);
        SwingUtilities.invokeLater(() -> {
            clientConnectPanel.requestFocusInWindow();
            if (!clientConnectPanel.hasFocus()) {
                clientConnectPanel.requestFocus();
            }
            // S'assurer que le ServerListPanel reçoit aussi le focus
            if (clientConnectPanel.getComponentCount() > 0) {
                java.awt.Component firstComp = clientConnectPanel.getComponent(0);
                if (firstComp != null) {
                    firstComp.requestFocusInWindow();
                }
            }
        });
        System.out.println("→ Menu connexion client affiché");
    }

    /**
     * Lance le jeu avec la configuration choisie
     */
    private void startGame(TeamConfigMenuPanel.TeamConfiguration config, String mapName) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🎮 LANCEMENT DU JEU LIQUID WAR 🎮   ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("");
        System.out.println("Mode de jeu: " + selectedGameMode);
        System.out
                .println("Équipe 1 (Bleue): " + config.teamTypes[0] + " - " + config.fighterCounts[0] + " combattants");
        System.out
                .println("Équipe 2 (Rouge): " + config.teamTypes[1] + " - " + config.fighterCounts[1] + " combattants");
        System.out.println("🗺️ Map sélectionnée: " + mapName);

        // Afficher les contrôles si les deux équipes sont humaines
        if ("Humain".equals(config.teamTypes[0]) && "Humain".equals(config.teamTypes[1])) {
            System.out.println("🎮 Contrôle Équipe 1: " + config.controlTypes[0]);
            System.out.println("🎮 Contrôle Équipe 2: " + config.controlTypes[1]);
        }
        System.out.println("");

        // Fermer la fenêtre des menus
        menuFrame.dispose();

        // Lancer le jeu avec notre code organisé (LiquidWarGame)
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("→ Lancement de LiquidWarGame...");
                LiquidWarGame game = new LiquidWarGame();

                // Configurer le mode de temps
                game.setTimeMode(selectedTimeMode, selectedDurationMinutes);

                // ⚠️ ORDRE CRITIQUE : Configurer TOUT avant setAIDifficulty() !
                // Raison : setAIDifficulty() peut déclencher l'initialisation

                // 1. Configurer les types d'équipe (Humain/IA)
                if (config.teamTypes != null) {
                    game.setTeamTypes(config.teamTypes);
                }

                // 2. Configurer les types de contrôle pour chaque équipe
                if (config.controlTypes != null) {
                    game.setTeamControlTypes(config.controlTypes);
                }

                // 3. Configurer le nombre de combattants par équipe (AVANT setAIDifficulty !)
                if (config.fighterCounts != null) {
                    System.out.println("📊 [MenuManager] Configuration fighterCounts: "
                            + java.util.Arrays.toString(config.fighterCounts));
                    game.setFighterCounts(config.fighterCounts);
                }

                // 4. Configurer la difficulté de l'IA (EN DERNIER !)
                if ("IA".equals(config.teamTypes[1])) {
                    String aiLevel = config.aiLevels[1];
                    System.out.println("🤖 Configuration IA - Difficulté: " + aiLevel);
                    game.setAIDifficulty(aiLevel);
                }

                // 5. Configurer les couleurs personnalisées
                if (config.colorIndices != null && config.colorIndices.length > 0) {
                    System.out.println("🎨 Configuration couleurs: " + java.util.Arrays.toString(config.colorIndices));
                    fr.uparis.informatique.cpoo5.liquidwar.view.GameRenderer.setCustomTeamColors(config.colorIndices);
                }

                // 6. Configurer la map
                game.setMapName(mapName);

                game.start(); // Appeler .start() pour démarrer le jeu !
                System.out.println("✅ Jeu lancé avec succès !");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du lancement du jeu:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erreur lors du lancement du jeu:\n" + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Lance le jeu avec la configuration Trio (3 équipes)
     */
    private void startTrioGame(TrioConfigMenuPanel.TrioConfiguration config, String mapName) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🎮 LANCEMENT DU JEU TRIO LOCAL 🎮   ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("");
        System.out.println("Mode de jeu: " + selectedGameMode);
        System.out
                .println("Équipe 1 (Bleue): " + config.teamTypes[0] + " - " + config.fighterCounts[0] + " combattants");
        System.out
                .println("Équipe 2 (Rouge): " + config.teamTypes[1] + " - " + config.fighterCounts[1] + " combattants");
        System.out
                .println("Équipe 3 (Verte): " + config.teamTypes[2] + " - " + config.fighterCounts[2] + " combattants");
        System.out.println("🗺️ Map sélectionnée: " + mapName);

        // Afficher les contrôles pour les équipes humaines
        for (int i = 0; i < 3; i++) {
            if ("Humain".equals(config.teamTypes[i])) {
                System.out.println("🎮 Contrôle Équipe " + (i + 1) + ": " + config.controlTypes[i]);
            }
        }
        System.out.println("");

        // Fermer la fenêtre des menus
        menuFrame.dispose();

        // Lancer le jeu avec 3 équipes
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("→ Lancement de LiquidWarGame avec 3 équipes...");
                LiquidWarGame game = new LiquidWarGame();

                // ⚠️ ORDRE CRITIQUE : Configurer TOUT avant setAIDifficulty() !
                // Raison : setAIDifficulty() peut déclencher l'initialisation

                // 1. Configurer les types d'équipe (Humain/IA)
                if (config.teamTypes != null) {
                    game.setTeamTypes(config.teamTypes);
                }

                // 2. Configurer le nombre d'équipes
                game.setNumTeams(3);

                // 3. Configurer les types de contrôle pour chaque équipe
                if (config.controlTypes != null) {
                    game.setTeamControlTypes(config.controlTypes);
                }

                // 4. Configurer le nombre de combattants par équipe (AVANT setAIDifficulty !)
                if (config.fighterCounts != null) {
                    System.out.println("📊 [MenuManager] Configuration fighterCounts: "
                            + java.util.Arrays.toString(config.fighterCounts));
                    game.setFighterCounts(config.fighterCounts);
                }

                // 5. Configurer la difficulté de l'IA (EN DERNIER !)
                for (int i = 0; i < 3; i++) {
                    if ("IA".equals(config.teamTypes[i])) {
                        String aiLevel = config.aiLevels[i];
                        System.out.println("🤖 Configuration IA Équipe " + (i + 1) + " - Difficulté: " + aiLevel);
                        game.setAIDifficulty(aiLevel);
                    }
                }

                // 6. Configurer les couleurs personnalisées
                if (config.colorIndices != null && config.colorIndices.length > 0) {
                    System.out.println("🎨 Configuration couleurs: " + java.util.Arrays.toString(config.colorIndices));
                    fr.uparis.informatique.cpoo5.liquidwar.view.GameRenderer.setCustomTeamColors(config.colorIndices);
                }

                // 7. Configurer la map
                game.setMapName(mapName);

                game.start(); // Appeler .start() pour démarrer le jeu !
                System.out.println("✅ Jeu Trio lancé avec succès !");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du lancement du jeu:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erreur lors du lancement du jeu:\n" + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Lance le jeu en mode réseau avec gestion d'erreur dans le menu
     */
    private void startNetworkGameWithErrorHandling(boolean isServer, String host, int port, int minPlayers,
            String serverName, ServerSetupPanel setupPanel) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🌐 MODE MULTIJOUEUR RÉSEAU 🌐      ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("");
        System.out.println("Type: " + (isServer ? "Serveur" : "Client"));
        if (serverName != null) {
            System.out.println("Nom du salon: " + serverName);
        }
        System.out.println("Adresse: " + host + ":" + port);
        System.out.println("Joueurs minimum: " + minPlayers);
        System.out.println("");

        // Lancer le jeu en mode réseau
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("→ Lancement du jeu en mode réseau...");
                NetworkLiquidWarGame networkGame = new NetworkLiquidWarGame(isServer, host, port, minPlayers,
                        serverName);
                networkGame.start();
                System.out.println("✅ Jeu réseau lancé avec succès !");
                // Fermer la fenêtre des menus seulement si le démarrage réussit
                menuFrame.dispose();
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du lancement du jeu réseau:");
                e.printStackTrace();

                // Afficher l'erreur dans le menu au lieu d'une popup
                if (setupPanel != null) {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && (errorMsg.contains("Address already in use") ||
                            errorMsg.contains("port") || errorMsg.contains("Port") ||
                            errorMsg.contains("already in use"))) {
                        setupPanel.showPortConflictError();
                    } else {
                        setupPanel.showServerStartError(errorMsg);
                    }
                    // Revenir au menu de configuration serveur
                    showServerSetupMenu();
                } else {
                    // Fallback : popup si pas de setupPanel
                    JOptionPane.showMessageDialog(menuFrame,
                            "Erreur lors de la connexion réseau:\n" + e.getMessage(),
                            "Erreur Réseau",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Lance le jeu en mode réseau
     * 
     * @param isServer   true si serveur, false si client
     * @param host       Adresse du serveur
     * @param port       Port du serveur
     * @param minPlayers Nombre minimum de joueurs pour démarrer
     * @param serverName Nom du serveur (null pour client)
     */
    private void startNetworkGame(boolean isServer, String host, int port, int minPlayers, String serverName) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🌐 MODE MULTIJOUEUR RÉSEAU 🌐      ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("");
        System.out.println("Type: " + (isServer ? "Serveur" : "Client"));
        if (serverName != null) {
            System.out.println("Nom du salon: " + serverName);
        }
        System.out.println("Adresse: " + host + ":" + port);
        System.out.println("Joueurs minimum: " + minPlayers);
        System.out.println("");

        // Fermer la fenêtre des menus
        menuFrame.dispose();

        // Lancer le jeu en mode réseau
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("→ Lancement du jeu en mode réseau...");
                NetworkLiquidWarGame networkGame = new NetworkLiquidWarGame(isServer, host, port, minPlayers,
                        serverName);
                networkGame.start();
                System.out.println("✅ Jeu réseau lancé avec succès !");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du lancement du jeu réseau:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erreur lors de la connexion réseau:\n" + e.getMessage(),
                        "Erreur Réseau",
                        JOptionPane.ERROR_MESSAGE);
                // Retourner au menu
                SwingUtilities.invokeLater(() -> new MainWithMenu());
            }
        });
    }
}
