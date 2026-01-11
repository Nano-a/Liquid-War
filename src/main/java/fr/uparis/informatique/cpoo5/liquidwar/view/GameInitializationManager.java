package fr.uparis.informatique.cpoo5.liquidwar.view;

import java.io.File;
import java.util.ArrayList;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.MapLoader;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Mesh;
import fr.uparis.informatique.cpoo5.liquidwar.service.GameInitializer;
import fr.uparis.informatique.cpoo5.liquidwar.service.OptimizedGradientEngine;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;
import fr.uparis.informatique.cpoo5.liquidwar.util.MeshBuilder;
import fr.uparis.informatique.cpoo5.liquidwar.util.PrecisionTimer;

/**
 * Gestionnaire de l'initialisation du jeu.
 * 
 * Responsabilités :
 * - Chargement de la carte
 * - Initialisation des équipes et curseurs
 * - Spawn des fighters initiaux
 * - Initialisation des structures optimisées (MESH)
 */
public class GameInitializationManager {

    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final int AREA_START_GRADIENT = GameConfig.AREA_START_GRADIENT;
    private static final int CURSOR_START_GRADIENT = GameConfig.CURSOR_START_GRADIENT;

    /**
     * Calcule les positions de spawn des équipes selon leur nombre.
     * 
     * Positions :
     * - 2 équipes : Bleu à gauche, Rouge à droite
     * - 3 équipes : Bleu haut-gauche, Rouge haut-droite, Vert bas-milieu
     * - 4 équipes : Bleu haut-gauche, Rouge haut-droite, Vert bas-gauche, Jaune
     * bas-droite
     */
    public static int[] getTeamSpawnPosition(int teamIndex, int totalTeams) {
        int x, y;

        switch (totalTeams) {
            case 2:
                // 2 équipes : gauche/droite au milieu verticalement
                if (teamIndex == 0) {
                    x = MAP_WIDTH / 4; // Bleu à gauche
                    y = MAP_HEIGHT / 2;
                } else {
                    x = MAP_WIDTH * 3 / 4; // Rouge à droite
                    y = MAP_HEIGHT / 2;
                }
                break;

            case 3:
                // 3 équipes : triangle
                switch (teamIndex) {
                    case 0:
                        x = MAP_WIDTH / 4; // Bleu haut-gauche
                        y = MAP_HEIGHT / 3;
                        break;
                    case 1:
                        x = MAP_WIDTH * 3 / 4; // Rouge haut-droite
                        y = MAP_HEIGHT / 3;
                        break;
                    case 2:
                    default:
                        x = MAP_WIDTH / 2; // Vert bas-milieu
                        y = MAP_HEIGHT * 2 / 3;
                        break;
                }
                break;

            case 4:
            default:
                // 4 équipes : quatre coins
                switch (teamIndex) {
                    case 0:
                        x = MAP_WIDTH / 4; // Bleu haut-gauche
                        y = MAP_HEIGHT / 4;
                        break;
                    case 1:
                        x = MAP_WIDTH * 3 / 4; // Rouge haut-droite
                        y = MAP_HEIGHT / 4;
                        break;
                    case 2:
                        x = MAP_WIDTH / 4; // Vert bas-gauche
                        y = MAP_HEIGHT * 3 / 4;
                        break;
                    case 3:
                    default:
                        x = MAP_WIDTH * 3 / 4; // Jaune bas-droite
                        y = MAP_HEIGHT * 3 / 4;
                        break;
                }
                break;
        }

        return new int[] { x, y };
    }

    /**
     * Charge la carte depuis un fichier BMP.
     * 
     * @param mapName Nom de la map à charger (sans extension), ou null pour la map
     *                par défaut
     */
    public static int[][] loadMap(String mapName) {
        // Si aucune map spécifiée, utiliser 2d par défaut
        if (mapName == null || mapName.isEmpty()) {
            mapName = "2d";
        }

        String[] possiblePaths = {
                "src/main/resources/maps/" + mapName + ".bmp",
                System.getProperty("user.dir") + "/src/main/resources/maps/" + mapName + ".bmp",
                "/home/ajinou/Bureau/CPOO/liquidwar5/data/map/" + mapName + ".bmp",
                "/home/ajinou/Bureau/Projet CPOO/liquidwar-Java(3)/Liquid-War-UPC/src/main/resources/maps/" + mapName
                        + ".bmp",
                "../liquidwar5/data/map/" + mapName + ".bmp",
                "liquidwar5/data/map/" + mapName + ".bmp"
        };

        String[] possibleTxtPaths = {
                "src/main/resources/maps/" + mapName + ".txt",
                System.getProperty("user.dir") + "/src/main/resources/maps/" + mapName + ".txt",
                "/home/ajinou/Bureau/CPOO/liquidwar5/data/map/" + mapName + ".txt",
                "/home/ajinou/Bureau/Projet CPOO/liquidwar-Java(3)/Liquid-War-UPC/src/main/resources/maps/" + mapName
                        + ".txt",
                "../liquidwar5/data/map/" + mapName + ".txt",
                "liquidwar5/data/map/" + mapName + ".txt"
        };

        int[][] map = null;
        String loadedMapName = "";

        GameLogger logger = GameLogger.getInstance();
        logger.debug("\n==========================================");
        logger.debug("DEBUG LiquidWarGame - CHARGEMENT DE LA CARTE");
        logger.debug("==========================================");
        logger.debug("Répertoire de travail: %s", System.getProperty("user.dir"));
        logger.debug("Chargement de la map: %s", mapName);
        logger.debug("\nChemins à tester:");
        for (int i = 0; i < possiblePaths.length; i++) {
            logger.debug("  [%d] %s", i, possiblePaths[i]);
        }
        logger.debug("");

        // Essayer de charger la carte
        boolean found = false;
        for (int i = 0; i < possiblePaths.length; i++) {
            logger.debug("Test chemin [%d]: %s", i, possiblePaths[i]);
            File bmpFile = new File(possiblePaths[i]);
            logger.debug("  → Chemin absolu: %s", bmpFile.getAbsolutePath());
            logger.debug("  → Existe? %s", bmpFile.exists());

            if (bmpFile.exists()) {
                logger.info("  ✓ TROUVÉ ! Chargement...\n");
                found = true;
                map = MapLoader.loadMapFromBMP(possiblePaths[i], MAP_WIDTH, MAP_HEIGHT);
                File txtFile = new File(possibleTxtPaths[i]);
                if (txtFile.exists()) {
                    loadedMapName = MapLoader.readMapName(possibleTxtPaths[i]);
                    logger.info("Nom de la carte: %s", loadedMapName);
                }
                break;
            } else {
                logger.warn("  ✗ Non trouvé\n");
            }
        }

        // Si la carte n'a pas pu être chargée, utiliser MapLoader.createDefaultMap
        if (map == null) {
            logger.error("==========================================");
            logger.error("ATTENTION: Aucune carte trouvée !");
            logger.error("==========================================");
            logger.warn("→ Utilisation de MapLoader.createDefaultMap()...\n");
            map = MapLoader.loadMapFromBMP("__DEFAULT__", MAP_WIDTH, MAP_HEIGHT);
        } else {
            logger.info("==========================================");
            logger.info("✓ CARTE CHARGÉE AVEC SUCCÈS !");
            logger.info("==========================================\n");
        }

        return map;
    }

    /**
     * Initialise les curseurs pour toutes les équipes actives.
     * 
     * @param cursors        Tableau des curseurs à initialiser
     * @param activeTeams    Nombre d'équipes actives
     * @param requestedTeams Nombre d'équipes demandées
     * @param map            Carte du jeu pour vérifier les obstacles (peut être
     *                       null)
     */
    public static void initializeCursors(Cursor[] cursors, int activeTeams, int requestedTeams, int[][] map) {
        String[] teamNames = { "Bleu", "Rouge", "Vert", "Jaune" };
        for (int team = 0; team < activeTeams; team++) {
            int[] spawnPos = getTeamSpawnPosition(team, requestedTeams);

            // Vérifier si la position de spawn est un obstacle
            int finalX = spawnPos[0];
            int finalY = spawnPos[1];

            if (map != null && finalY >= 0 && finalY < map.length
                    && finalX >= 0 && finalX < map[0].length
                    && map[finalY][finalX] == -1) {
                // Position initiale est un obstacle, trouver la position libre la plus proche
                int[] freePos = findNearestFreePosition(finalX, finalY, map);
                finalX = freePos[0];
                finalY = freePos[1];
                GameLogger.getInstance().warn(
                        "⚠️ Équipe %d (%s) spawn initial sur obstacle, déplacée à (%d, %d)",
                        team, teamNames[team], finalX, finalY);
            }

            cursors[team] = new Cursor();
            cursors[team].x = finalX;
            cursors[team].y = finalY;
            cursors[team].team = team;
            cursors[team].active = 1;

            GameLogger.getInstance().info("🎮 Équipe %d (%s) positionnée à (%d, %d)",
                    team, teamNames[team], finalX, finalY);
        }
    }

    /**
     * Trouve la position libre la plus proche d'une position donnée.
     * Utilise une recherche en spirale pour trouver la première case libre.
     * 
     * @param startX Position X de départ
     * @param startY Position Y de départ
     * @param map    Carte du jeu
     * @return Position [x, y] de la case libre la plus proche
     */
    private static int[] findNearestFreePosition(int startX, int startY, int[][] map) {
        if (map == null || map.length == 0 || map[0].length == 0) {
            return new int[] { startX, startY };
        }

        int mapWidth = map[0].length;
        int mapHeight = map.length;
        int maxRadius = Math.max(mapWidth, mapHeight);

        // Recherche en spirale autour de la position de départ
        for (int radius = 1; radius < maxRadius; radius++) {
            // Parcourir toutes les positions à distance 'radius'
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    // Vérifier que c'est bien à distance 'radius' (pas à l'intérieur)
                    int dist = Math.max(Math.abs(dx), Math.abs(dy));
                    if (dist != radius) {
                        continue;
                    }

                    int x = startX + dx;
                    int y = startY + dy;

                    // Vérifier les limites
                    if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
                        // Vérifier que ce n'est pas un obstacle
                        if (map[y][x] != -1) {
                            return new int[] { x, y };
                        }
                    }
                }
            }
        }

        // Si aucune position libre trouvée (cas extrême), retourner le centre de la
        // carte
        GameLogger.getInstance().warn(
                "⚠️ Aucune position libre trouvée près de (%d, %d), utilisation du centre",
                startX, startY);
        return new int[] { mapWidth / 2, mapHeight / 2 };
    }

    /**
     * Initialise les fighters pour toutes les équipes.
     */
    public static void initializeFighters(ArrayList<Fighter> fighters, Cursor[] cursors,
            int activeTeams, int[] customFighterCounts,
            int[][] map, int[] teamFighterCount) {
        int totalFighters = 0;

        for (int team = 0; team < activeTeams; team++) {
            // Utiliser le nombre personnalisé si défini, sinon la valeur par défaut
            int fightersForTeam;
            if (customFighterCounts != null && team < customFighterCounts.length) {
                fightersForTeam = customFighterCounts[team];
            } else {
                fightersForTeam = GameConfig.TOTAL_FIGHTERS / activeTeams;
            }

            GameInitializer.spawnInitialFighters(team, cursors[team].x, cursors[team].y,
                    fightersForTeam, fighters, teamFighterCount, map);
            totalFighters += fightersForTeam;

            GameLogger.getInstance().info("Équipe %d: %d combattants", team, fightersForTeam);
        }

        GameLogger.getInstance().info("%d équipes initialisées (total: %d fighters)",
                activeTeams, totalFighters);
    }

    /**
     * Initialise les gradients pour toutes les équipes.
     */
    public static void initializeGradients(int[][] gradient, int[] cursorVal, int activeTeams) {
        // Initialiser le gradient à une grande valeur
        for (int team = 0; team < 6; team++) {
            for (int i = 0; i < MAP_HEIGHT * MAP_WIDTH; i++) {
                gradient[team][i] = AREA_START_GRADIENT;
            }
            cursorVal[team] = CURSOR_START_GRADIENT / 2;
        }
    }

    /**
     * Initialise la structure MESH optimisée.
     */
    public static MeshInitializationResult initializeMesh(int[][] map, int activeTeams) {
        GameLogger logger = GameLogger.getInstance();
        logger.info("Activation du moteur optimisé");

        // Construire le graphe MESH optimisé
        logger.info("⚙️ Construction du graphe MESH...");
        Mesh[] meshArray = MeshBuilder.buildMeshArray(map);

        // Créer table de correspondance (x,y) → Mesh pour accès O(1)
        Mesh[][] meshLookup = new Mesh[GameConfig.MAP_HEIGHT][GameConfig.MAP_WIDTH];
        for (Mesh mesh : meshArray) {
            meshLookup[mesh.y][mesh.x] = mesh;
        }

        OptimizedGradientEngine.resetAllGradients(meshArray, activeTeams);
        logger.info("   ✅ %d cellules MESH avec liens directs", meshArray.length);

        // Initialiser le timer précis
        PrecisionTimer precisionTimer = new PrecisionTimer();
        logger.info("⚙️ Timer haute précision initialisé");
        logger.info("   → Logique : 200 Hz (5ms)");
        logger.info("   → Affichage : 60 Hz (16ms)");

        // Initialiser l'effet d'eau (désactivé par défaut)
        WaterDistortionEffect waterFX = new WaterDistortionEffect(MAP_WIDTH, MAP_HEIGHT);
        waterFX.setEnabled(false);
        logger.info("⚙️ Distorsion d'eau disponible (touche W pour activer)");

        logger.info("\n╔═══════════════════════════════════════════════════════╗");
        logger.info("║  ✅ MOTEUR OPTIMISÉ ACTIVÉ !                         ║");
        logger.info("║  → Structure MESH : Accès O(1) aux voisins           ║");
        logger.info("║  → Gradient : Propagation directionnelle             ║");
        logger.info("║  → Timing : Séparation logique/affichage             ║");
        logger.info("║  → Gain attendu : +38%% de fluidité                   ║");
        logger.info("╚═══════════════════════════════════════════════════════╝\n");

        return new MeshInitializationResult(meshArray, meshLookup, precisionTimer, waterFX);
    }

    /**
     * Résultat de l'initialisation MESH.
     */
    public static class MeshInitializationResult {
        public final Mesh[] meshArray;
        public final Mesh[][] meshLookup;
        public final PrecisionTimer precisionTimer;
        public final WaterDistortionEffect waterFX;

        public MeshInitializationResult(Mesh[] meshArray, Mesh[][] meshLookup,
                PrecisionTimer precisionTimer, WaterDistortionEffect waterFX) {
            this.meshArray = meshArray;
            this.meshLookup = meshLookup;
            this.precisionTimer = precisionTimer;
            this.waterFX = waterFX;
        }
    }
}
