package fr.uparis.informatique.cpoo5.liquidwar.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JPanel;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameOptions;
import fr.uparis.informatique.cpoo5.liquidwar.config.RenderConfig;
import fr.uparis.informatique.cpoo5.liquidwar.controller.command.CommandHistory;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Mesh;
import fr.uparis.informatique.cpoo5.liquidwar.service.GameInitializer;
import fr.uparis.informatique.cpoo5.liquidwar.service.MovementEngine;
import fr.uparis.informatique.cpoo5.liquidwar.service.OptimizedGradientEngine;
import fr.uparis.informatique.cpoo5.liquidwar.service.OptimizedMovementEngine;
import fr.uparis.informatique.cpoo5.liquidwar.service.ParallelMovementEngine;
import fr.uparis.informatique.cpoo5.liquidwar.service.ParallelPathfindingEngine;
import fr.uparis.informatique.cpoo5.liquidwar.service.PathfindingEngine;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;
import fr.uparis.informatique.cpoo5.liquidwar.util.PrecisionTimer;
import fr.uparis.informatique.cpoo5.liquidwar.view.input.GameInputHandler;
import fr.uparis.informatique.cpoo5.liquidwar.view.input.PlayerCursorController;

/**
 * Canvas de jeu principal pour Liquid War.
 * 
 * Cette classe orchestre tous les composants du jeu :
 * - Rendu graphique (délégué à GameRenderer)
 * - Gestion des entrées (délégué à GameInputHandler)
 * - Logique de jeu (updateLogic)
 * - Initialisation (délégué à GameInitializationManager)
 * - Configuration (délégué à GameCanvasConfiguration)
 * - IA (délégué à AIController)
 * 
 * Responsabilités principales :
 * - Coordination entre les différents composants
 * - Synchronisation thread-safe (locks)
 * - Boucle de jeu (updateLogic + paintComponent)
 */
public class GameCanvas extends JPanel {

    // Constantes
    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final int AREA_START_GRADIENT = GameConfig.AREA_START_GRADIENT;
    private static final int CURSOR_START_GRADIENT = GameConfig.CURSOR_START_GRADIENT;
    private static final int NB_TRY_MOVE = GameConfig.NB_TRY_MOVE;
    private static final int NB_LOCAL_DIRS = GameConfig.NB_LOCAL_DIRS;
    private static final boolean USE_MULTITHREADING = true;

    // Directions (12 directions comme dans le code C)
    private static final int[][] DIR_MOVE_X = {
            { 0, 1, 1, 1, 1, 0, 0, -1, -1, -1, -1, 0 }, // Table 0
            { 0, 1, 1, 1, 1, 0, 0, -1, -1, -1, -1, 0 } // Table 1
    };
    private static final int[][] DIR_MOVE_Y = {
            { -1, -1, 0, 0, 1, 1, 1, 1, 0, 0, -1, -1 }, // Table 0
            { -1, -1, 0, 0, 1, 1, 1, 1, 0, 0, -1, -1 } // Table 1
    };

    // ==================== SYNCHRONISATION MULTITHREADING ====================
    private final ReentrantReadWriteLock gameStateLock = new ReentrantReadWriteLock();
    private final Lock readLock = gameStateLock.readLock();
    private final Lock writeLock = gameStateLock.writeLock();

    // ==================== COMPOSANTS DÉLÉGUÉS ====================
    private GameCanvasConfiguration configuration;
    private GameInputHandler inputHandler;
    private PlayerCursorController cursorController;
    private AIController aiController;
    private CommandHistory commandHistory;

    // ==================== ÉTAT DU JEU ====================
    private ArrayList<Fighter> fighters;
    private Cursor[] cursors;
    private int[][] map;
    private int[][] gradient;
    private int[] cursorVal;
    private int activeTeams;
    private int globalClock = 0;
    private int[] teamFighterCount;
    private String selectedMapName = null; // Nom de la map sélectionnée
    // Flag pour forcer la propagation du gradient au prochain tick (pour la souris)
    private boolean forceGradientUpdate = false;

    // Curseur du joueur principal
    private int playerCursorX = MAP_WIDTH / 2;
    private int playerCursorY = MAP_HEIGHT / 2;
    private int playerTeam = 0;

    // Mode réseau
    private boolean networkMode = false;

    // ==================== OPTIMISATIONS ====================
    private boolean useOptimizedEngine = true;
    private Mesh[] meshArray;
    private Mesh[][] meshLookup;
    private PrecisionTimer precisionTimer;
    private WaterDistortionEffect waterFX;
    private long lastOptimizationLog = 0;

    // ==================== RÉGÉNÉRATION ====================
    // Régénération passive pour retrouver la couleur claire en se soignant (comme
    // en C)
    // Accélérer la régénération pour que la couleur redevienne claire plus vite
    private static final int HEAL_INTERVAL_TICKS = 10; // toutes les ~0.05 s à 200 Hz logique
    private static final int HEAL_AMOUNT = 2; // +2 PV par intervalle

    // Système de mouvement "slime-like"
    private int[] LOCAL_DIR = new int[NB_LOCAL_DIRS * 2];
    private int[][][] FIGHTER_MOVE_DIR = new int[2][12][NB_TRY_MOVE];
    private int[][][] FIGHTER_MOVE_X_ALT = new int[2][12][NB_TRY_MOVE];
    private int[][][] FIGHTER_MOVE_Y_ALT = new int[2][12][NB_TRY_MOVE];
    private int[][] cursorPosX = new int[6][MAP_HEIGHT * MAP_WIDTH];
    private int[][] cursorPosY = new int[6][MAP_HEIGHT * MAP_WIDTH];
    private int[][] updateTime = new int[6][MAP_HEIGHT * MAP_WIDTH];

    // ==================== RENDU ====================
    private BufferedImage gameBuffer;
    private BufferedImage staticBuffer;
    private boolean staticBufferDirty = true;
    private Graphics2D bufferGraphics;
    private int[] gameBufferPixels;
    private int[] staticBufferPixels;

    // ==================== LISTENER ====================
    private PauseListener pauseListener;

    /**
     * Interface pour notifier quand la pause est demandée.
     */
    public interface PauseListener {
        void onPauseRequested();
    }

    public GameCanvas() {
        setFocusable(true);
        setBackground(RenderConfig.GAME_AREA_BACKGROUND);
        setDoubleBuffered(true);
        setIgnoreRepaint(false);

        // Initialiser les composants
        commandHistory = new CommandHistory();
        configuration = new GameCanvasConfiguration();
        cursorController = new PlayerCursorController(commandHistory);
        aiController = new AIController();

        // Initialiser le gestionnaire d'entrées
        inputHandler = new GameInputHandler(new GameInputHandler.InputListener() {
            @Override
            public void onCursorMoveRequested(int team, int x, int y) {
                if (cursors != null && team >= 0 && team < cursors.length && cursors[team] != null) {
                    // Vérifier que la nouvelle position n'est pas un obstacle
                    // Amélioration : trouver la case libre la plus proche si bloqué
                    if (map != null && y >= 0 && y < map.length && x >= 0 && x < map[0].length) {
                        if (map[y][x] == -1) {
                            // La position exacte est un obstacle, chercher une case libre proche
                            int currentX = cursors[team].x;
                            int currentY = cursors[team].y;
                            int[] freePos = findNearestFreePositionForCursor(x, y, currentX, currentY, map, 3);
                            if (freePos != null) {
                                x = freePos[0];
                                y = freePos[1];
                            } else {
                                // Aucune case libre trouvée, ne pas déplacer
                                return;
                            }
                        }
                    }

                    // Vérifier si la position a vraiment changé
                    boolean positionChanged = (cursors[team].x != x || cursors[team].y != y);

                    cursors[team].x = x;
                    cursors[team].y = y;
                    if (team == playerTeam) {
                        playerCursorX = x;
                        playerCursorY = y;
                        // Mettre à jour PlayerCursorController pour qu'il soit synchronisé
                        if (cursorController != null) {
                            cursorController.setPlayerCursorPosition(x, y);
                        }
                    }

                    // Réinitialiser cursorVal pour que le gradient se propage à nouveau
                    // C'est crucial pour que les fighters suivent le curseur
                    if (positionChanged && cursorVal != null && team < cursorVal.length) {
                        cursorVal[team] = CURSOR_START_GRADIENT;
                        // Forcer la mise à jour du gradient au prochain tick
                        forceGradientUpdate = true;
                    }
                }
            }

            @Override
            public void onPauseRequested() {
                if (pauseListener != null) {
                    pauseListener.onPauseRequested();
                }
            }

            @Override
            public void onWaterEffectToggle() {
                if (waterFX != null) {
                    waterFX.setEnabled(!waterFX.isEnabled());
                    GameLogger.getInstance().info("Distorsion d'eau : %s",
                            waterFX.isEnabled() ? "ACTIVÉE" : "DÉSACTIVÉE");
                }
            }

            @Override
            public void onOptimizedEngineToggle() {
                useOptimizedEngine = !useOptimizedEngine;
                GameLogger.getInstance().info("Moteur optimisé : %s",
                        useOptimizedEngine ? "ACTIVÉ" : "DÉSACTIVÉ (fallback classique)");
            }
        }, commandHistory);

        // Ajouter les listeners
        addKeyListener(inputHandler);
        addMouseMotionListener(inputHandler);
        addMouseListener(inputHandler);

        // Initialiser l'IA par défaut
        configuration.setAIDifficulty("Moyen");
    }

    // ==================== CONFIGURATION ====================

    /**
     * Définit la difficulté de l'IA.
     */
    public void setAIDifficulty(String difficulty) {
        configuration.setAIDifficulty(difficulty);
        if (aiController != null) {
            aiController.setAIStrategy(configuration.getAIStrategy());
        }
    }

    /**
     * Définit le nombre d'équipes pour la partie (2-4).
     */
    public void setRequestedTeams(int teams) {
        int newTeams = Math.max(2, Math.min(4, teams));
        if (newTeams != configuration.getRequestedTeams()) {
            configuration.setRequestedTeams(newTeams);
            GameLogger.getInstance().info("🔄 Réinitialisation du jeu avec %d équipes", newTeams);
            initializeGame();
        }
    }

    /**
     * Définit les types de contrôle pour chaque équipe.
     */
    public void setTeamControlTypes(String[] controlTypes) {
        configuration.setTeamControlTypes(controlTypes);
        updateInputHandlerConfiguration();
    }

    /**
     * Définit les types d'équipe (Humain ou IA) pour chaque équipe.
     */
    public void setTeamTypes(String[] types) {
        configuration.setTeamTypes(types);
        updateInputHandlerConfiguration();
    }

    /**
     * Définit le nombre de combattants par équipe.
     */
    public void setFighterCounts(int[] counts) {
        configuration.setFighterCounts(counts);
    }

    /**
     * Met à jour la configuration du gestionnaire d'entrées.
     */
    private void updateInputHandlerConfiguration() {
        if (inputHandler != null && cursors != null) {
            inputHandler.setConfiguration(
                    configuration.getTeamControlTypes(),
                    configuration.getTeamTypes(),
                    activeTeams,
                    playerTeam,
                    networkMode,
                    cursors,
                    getWidth() > 0 ? getWidth() : MAP_WIDTH * RenderConfig.SCALE,
                    getHeight() > 0 ? getHeight() : MAP_HEIGHT * RenderConfig.SCALE);
        }
    }

    /**
     * Définit le listener de pause.
     */
    public void setPauseListener(PauseListener listener) {
        this.pauseListener = listener;
    }

    // ==================== INITIALISATION ====================

    /**
     * Initialiser le jeu après que toutes les configurations aient été définies.
     */
    public void initialize() {
        initializeGame();
    }

    /**
     * Réinitialise le jeu avec un nombre d'équipes spécifique.
     */
    public void reinitializeWithTeams(int teams) {
        setRequestedTeams(teams);
        initializeGame();
    }

    /**
     * Définit le nom de la map à charger.
     * 
     * @param mapName Nom de la map (sans extension), ou null pour la map par défaut
     */
    public void setMapName(String mapName) {
        this.selectedMapName = mapName;
    }

    /**
     * Obtient le nom de la map actuellement sélectionnée.
     * 
     * @return Nom de la map (sans extension), ou null si aucune map spécifique
     */
    public String getMapName() {
        return selectedMapName;
    }

    /**
     * Initialise le jeu complet.
     */
    private void initializeGame() {
        // Charger la carte
        map = GameInitializationManager.loadMap(selectedMapName);

        // Initialiser les équipes
        activeTeams = configuration.getRequestedTeams();
        cursors = new Cursor[6];
        teamFighterCount = new int[6];
        gradient = new int[6][MAP_HEIGHT * MAP_WIDTH];
        cursorVal = new int[6];

        // Initialiser les gradients
        GameInitializationManager.initializeGradients(gradient, cursorVal, activeTeams);

        // Initialiser les curseurs (avec vérification des obstacles)
        GameInitializationManager.initializeCursors(cursors, activeTeams, configuration.getRequestedTeams(), map);

        // Initialiser les fighters
        fighters = new ArrayList<>();
        GameInitializationManager.initializeFighters(fighters, cursors, activeTeams,
                configuration.getCustomFighterCounts(), map, teamFighterCount);

        // Initialiser les buffers de rendu
        gameBuffer = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
        staticBuffer = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
        bufferGraphics = gameBuffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        gameBufferPixels = ((java.awt.image.DataBufferInt) gameBuffer.getRaster().getDataBuffer()).getData();
        staticBufferPixels = ((java.awt.image.DataBufferInt) staticBuffer.getRaster().getDataBuffer()).getData();
        staticBufferDirty = true;

        // Initialiser le système de mouvement
        GameInitializer.initMoveFighters(LOCAL_DIR, FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT,
                FIGHTER_MOVE_Y_ALT, DIR_MOVE_X, DIR_MOVE_Y,
                updateTime, cursorPosX, cursorPosY);

        // Initialiser les optimisations
        if (useOptimizedEngine) {
            GameInitializationManager.MeshInitializationResult meshResult = GameInitializationManager
                    .initializeMesh(map, activeTeams);
            meshArray = meshResult.meshArray;
            meshLookup = meshResult.meshLookup;
            precisionTimer = meshResult.precisionTimer;
            waterFX = meshResult.waterFX;
            inputHandler.setWaterEffect(waterFX);
        }

        // Mettre à jour les contrôleurs
        cursorController.setConfiguration(cursors, configuration.getTeamControlTypes(),
                configuration.getTeamTypes(), activeTeams, playerTeam, networkMode);
        // Passer la carte pour vérifier les obstacles
        if (map != null) {
            cursorController.setMap(map);
        }
        aiController.setGameState(cursors, map, gradient, teamFighterCount, globalClock);
        aiController.setAIStrategy(configuration.getAIStrategy());
        updateInputHandlerConfiguration();

        // Initialiser la position du curseur du joueur
        if (cursors[0] != null) {
            playerCursorX = cursors[0].x;
            playerCursorY = cursors[0].y;
            cursorController.setPlayerCursor(playerCursorX, playerCursorY);
        }
    }

    // ==================== LOGIQUE DE JEU ====================

    /**
     * Mise à jour de la logique du jeu (appelée par le timer de logique).
     */
    public void updateLogic() {
        writeLock.lock();
        try {
            globalClock++;

            // Mettre à jour le curseur du joueur
            if (inputHandler != null) {
                GameInputHandler.KeyState keyState = inputHandler.getKeyState();
                cursorController.updatePlayerCursor(keyState);
            }

            // Mettre à jour le curseur du joueur principal (seulement si contrôle clavier)
            // Si contrôle souris, la position est déjà mise à jour dans
            // onCursorMoveRequested
            if (cursors[0] != null && configuration.isTeamHuman(0)) {
                String controlType = configuration.getTeamControlTypes()[0];
                // Ne pas écraser la position si c'est un contrôle souris
                if (!"Souris".equals(controlType)) {
                    Point playerPos = cursorController.getPlayerCursorPosition();
                    cursors[0].x = playerPos.x;
                    cursors[0].y = playerPos.y;
                    playerCursorX = playerPos.x;
                    playerCursorY = playerPos.y;
                }
            }

            // Mettre à jour les curseurs IA
            if (!networkMode) {
                for (int team = 0; team < activeTeams; team++) {
                    if (cursors[team] != null && !configuration.isTeamHuman(team)) {
                        aiController.updateAICursor(team);
                    }
                }
            }

            // Décrémenter la valeur du curseur (mais pas en dessous d'un minimum)
            for (int team = 0; team < activeTeams; team++) {
                if (cursors[team] != null && cursors[team].active != 0) {
                    // Ne décrémenter que si cursorVal est positif
                    // Si cursorVal est déjà bas, cela signifie que le curseur n'a pas bougé
                    // récemment
                    if (cursorVal[team] > 0) {
                        cursorVal[team]--;
                    }
                }
            }

            // Régénération passive des fighters (rééclaircissement progressif des couleurs)
            if (globalClock % HEAL_INTERVAL_TICKS == 0) {
                for (Fighter fighter : fighters) {
                    if (fighter.health < GameConfig.FIGHTER_INITIAL_HEALTH) {
                        fighter.health = Math.min(GameConfig.FIGHTER_INITIAL_HEALTH,
                                fighter.health + HEAL_AMOUNT);
                    }
                }
            }

            // Log des positions des curseurs (toutes les 2 secondes)
            if (globalClock % 240 == 0) {
                for (int team = 0; team < activeTeams; team++) {
                    if (cursors[team] != null && cursors[team].active != 0) {
                        GameLogger.getInstance().debug(
                                "Team %d curseur: (%d, %d) cursorVal=%d Clock=%d",
                                team, cursors[team].x, cursors[team].y, cursorVal[team], globalClock);
                    }
                }
            }

            // Mettre à jour l'état de l'IA
            aiController.setGameState(cursors, map, gradient, teamFighterCount, globalClock);

            // Choix entre moteur optimisé et moteur classique
            if (useOptimizedEngine && meshArray != null) {
                // Version optimisée - Structure MESH
                OptimizedGradientEngine.updateCursorPositions(meshArray, cursors, activeTeams, globalClock);
                OptimizedGradientEngine.applyAllCursors(meshArray, cursors, cursorVal, activeTeams);

                // Propager le gradient tous les 3 ticks, ou immédiatement si forcé (pour la
                // souris)
                if (globalClock % 3 == 0 || forceGradientUpdate) {
                    OptimizedGradientEngine.spreadSingleGradient(meshArray, activeTeams, globalClock);

                    // Synchroniser : copier le gradient de MESH vers gradient[][]
                    for (Mesh mesh : meshArray) {
                        int idx = mesh.y * MAP_WIDTH + mesh.x;
                        for (int team = 0; team < activeTeams; team++) {
                            gradient[team][idx] = mesh.teamInfo[team].gradient;
                        }
                    }

                    // Réinitialiser le flag après avoir propagé
                    forceGradientUpdate = false;
                }

                OptimizedMovementEngine.moveFighters(fighters, meshArray, meshLookup, cursors, teamFighterCount,
                        FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT, FIGHTER_MOVE_Y_ALT,
                        globalClock, map, MAP_WIDTH, MAP_HEIGHT);

                // LOG stats toutes les 5 secondes
                if (precisionTimer != null && System.currentTimeMillis() - lastOptimizationLog > 5000) {
                    double logicFPS = precisionTimer.getLogicFPS();
                    double displayFPS = precisionTimer.getDisplayFPS();
                    if (logicFPS > 0 && displayFPS > 0) {
                        GameLogger.getInstance().info(
                                "⚡ OPTIMISÉ | Logique: %.1f Hz | Affichage: %.1f FPS | Ratio: %.1f:1",
                                logicFPS, displayFPS, logicFPS / displayFPS);
                    }
                    lastOptimizationLog = System.currentTimeMillis();
                }

            } else {
                // Version classique - Tableaux 2D
                PathfindingEngine.updateCursorPositions(cursors, activeTeams, map, cursorPosX, cursorPosY, updateTime,
                        globalClock);

                if (USE_MULTITHREADING) {
                    ParallelPathfindingEngine.calculateGradientParallel(map, gradient, cursors,
                            cursorPosX, cursorPosY, cursorVal, activeTeams, globalClock);
                } else {
                    PathfindingEngine.applyAllCursors(cursors, activeTeams, map, gradient, cursorVal);
                    PathfindingEngine.spreadSingleGradient(map, gradient, activeTeams, globalClock);
                }

                if (USE_MULTITHREADING) {
                    ParallelMovementEngine.moveFightersParallel(fighters, cursors, teamFighterCount, map, gradient,
                            updateTime, cursorPosX, cursorPosY, LOCAL_DIR,
                            FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT, FIGHTER_MOVE_Y_ALT,
                            DIR_MOVE_X, DIR_MOVE_Y, globalClock);
                } else {
                    MovementEngine.moveFighters(fighters, cursors, teamFighterCount, map, gradient,
                            updateTime, cursorPosX, cursorPosY, LOCAL_DIR,
                            FIGHTER_MOVE_DIR, FIGHTER_MOVE_X_ALT, FIGHTER_MOVE_Y_ALT,
                            DIR_MOVE_X, DIR_MOVE_Y, globalClock);
                }
            }

        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Obtient les compteurs de combattants par équipe.
     */
    public int[] getTeamFighterCounts() {
        return teamFighterCount;
    }

    // ==================== RENDU ====================

    /**
     * Rendu du jeu (appelé par Swing).
     */
    @Override
    protected void paintComponent(Graphics g) {
        readLock.lock();
        try {
            super.paintComponent(g);

            // Créer le buffer statique si nécessaire
            boolean[] staticBufferDirtyRef = { staticBufferDirty };
            GameRenderer.drawStaticBuffer(map, staticBufferPixels, staticBufferDirtyRef);
            staticBufferDirty = staticBufferDirtyRef[0];

            // Copier les pixels directement
            System.arraycopy(staticBufferPixels, 0, gameBufferPixels, 0, MAP_WIDTH * MAP_HEIGHT);

            // Dessiner les fighters
            GameRenderer.drawFighters(fighters, gameBufferPixels);

            // Dessiner les curseurs
            GameRenderer.drawCursors(cursors, activeTeams, bufferGraphics);

            // Configurer les hints de rendu selon la qualité graphique
            Graphics2D g2d = (Graphics2D) g;
            GameOptions options = GameOptions.getInstance();
            String quality = options.getGraphicsQuality();

            // Inversion de la logique pour corriger le problème d'inversion
            switch (quality) {
                case "Élevée":
                    // Si "Élevée" est sélectionnée, appliquer les paramètres de "Basse"
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                    break;
                case "Moyenne":
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
                    break;
                case "Basse":
                default:
                    // Si "Basse" est sélectionnée, appliquer les paramètres de "Élevée"
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    break;
            }

            // Log une seule fois au premier affichage
            GameRenderer.printDebugLogs(globalClock, gameBuffer, getWidth(), getHeight());

            // Étirer le buffer de jeu à l'écran
            g2d.drawImage(gameBuffer, 0, 0, getWidth(), getHeight(), null);

            // Afficher un indicateur de qualité graphique
            g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
            String qualityIndicator = "Qualité: " + quality;
            Color qualityColor;
            switch (quality) {
                case "Élevée":
                    qualityColor = new Color(0, 255, 0, 200);
                    break;
                case "Moyenne":
                    qualityColor = new Color(255, 255, 0, 200);
                    break;
                default:
                    qualityColor = new Color(255, 100, 100, 200);
                    break;
            }
            g2d.setColor(qualityColor);
            g2d.drawString(qualityIndicator, 10, 20);

            // Afficher les statistiques
            GameRenderer.drawHUD(g2d, activeTeams, teamFighterCount, getWidth(), getHeight());

        } finally {
            readLock.unlock();
        }
    }

    // ==================== SUPPORT RÉSEAU ====================

    /**
     * Obtient l'état du jeu pour la synchronisation réseau.
     */
    public fr.uparis.informatique.cpoo5.liquidwar.model.GameState getGameState() {
        return new fr.uparis.informatique.cpoo5.liquidwar.model.GameState(
                map, gradient, fighters, cursors, teamFighterCount, activeTeams);
    }

    /**
     * Définit l'ID de l'équipe locale (pour le mode réseau).
     */
    public void setLocalTeamId(int teamId) {
        GameLogger.getInstance().info("Équipe locale définie: %d", teamId);
    }

    /**
     * Obtient la position actuelle du curseur du joueur.
     */
    public Point getCursorPosition() {
        if (cursors != null && cursors.length > 0 && cursors[0] != null) {
            return new Point(cursors[0].x, cursors[0].y);
        }
        return null;
    }

    /**
     * Active ou désactive le mode réseau.
     */
    public void setNetworkMode(boolean enabled) {
        this.networkMode = enabled;
        if (enabled) {
            GameLogger.getInstance().info("🌐 Mode réseau activé - IA désactivée");
        }
        updateInputHandlerConfiguration();
        if (cursorController != null) {
            cursorController.setConfiguration(cursors, configuration.getTeamControlTypes(),
                    configuration.getTeamTypes(), activeTeams, playerTeam, networkMode);
        }
    }

    /**
     * Vérifie si le mode réseau est actif.
     */
    public boolean isNetworkMode() {
        return networkMode;
    }

    /**
     * Trouve la case libre la plus proche d'une position donnée pour le curseur.
     * Recherche en spirale autour de la position cible, en privilégiant la
     * direction du mouvement.
     * 
     * @param targetX   Position X cible (peut être un obstacle)
     * @param targetY   Position Y cible (peut être un obstacle)
     * @param currentX  Position X actuelle
     * @param currentY  Position Y actuelle
     * @param map       Carte du jeu
     * @param maxRadius Rayon maximum de recherche
     * @return Position [x, y] de la case libre la plus proche, ou null si aucune
     *         trouvée
     */
    private int[] findNearestFreePositionForCursor(int targetX, int targetY, int currentX, int currentY, int[][] map,
            int maxRadius) {
        if (map == null || map.length == 0 || map[0].length == 0) {
            return null;
        }

        int mapWidth = map[0].length;
        int mapHeight = map.length;

        // Prioriser les directions proches de la direction du mouvement
        int dx = targetX - currentX;
        int dy = targetY - currentY;

        // Directions prioritaires : d'abord dans la direction du mouvement, puis
        // adjacentes
        int[][] priorityDirs = {
                { dx, dy }, // Direction exacte du mouvement
                { dx, 0 }, // Horizontal
                { 0, dy }, // Vertical
                { dx > 0 ? 1 : -1, 0 }, // Horizontal vers la cible
                { 0, dy > 0 ? 1 : -1 }, // Vertical vers la cible
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Directions cardinales
                { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 } // Directions diagonales
        };

        // Essayer d'abord les directions prioritaires
        for (int[] dir : priorityDirs) {
            int testX = targetX + dir[0];
            int testY = targetY + dir[1];
            if (testX >= 0 && testX < mapWidth && testY >= 0 && testY < mapHeight) {
                if (map[testY][testX] != -1) {
                    return new int[] { testX, testY };
                }
            }
        }

        // Si aucune direction prioritaire n'a fonctionné, recherche en spirale
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int dx2 = -radius; dx2 <= radius; dx2++) {
                for (int dy2 = -radius; dy2 <= radius; dy2++) {
                    // Vérifier que c'est bien à distance 'radius' (pas à l'intérieur)
                    int dist = Math.max(Math.abs(dx2), Math.abs(dy2));
                    if (dist != radius) {
                        continue;
                    }

                    int testX = targetX + dx2;
                    int testY = targetY + dy2;

                    if (testX >= 0 && testX < mapWidth && testY >= 0 && testY < mapHeight) {
                        if (map[testY][testX] != -1) {
                            return new int[] { testX, testY };
                        }
                    }
                }
            }
        }

        return null; // Aucune case libre trouvée
    }

    /**
     * Définit la position du curseur d'une équipe (utilisé par le réseau).
     */
    public void setCursorPosition(int team, int x, int y) {
        if (cursors != null && team >= 0 && team < cursors.length && cursors[team] != null) {
            cursors[team].x = Math.max(0, Math.min(MAP_WIDTH - 1, x));
            cursors[team].y = Math.max(0, Math.min(MAP_HEIGHT - 1, y));
        }
    }
}
