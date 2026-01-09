package fr.uparis.informatique.cpoo5.liquidwar.view;

import java.awt.Point;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.service.ai.AIStrategy;
import fr.uparis.informatique.cpoo5.liquidwar.service.ai.AggressiveAI;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;

/**
 * Contrôleur pour gérer l'IA des équipes.
 * 
 * Responsabilités :
 * - Mise à jour des curseurs IA
 * - Adaptation du GameState pour l'IA
 * - Logs de débogage
 */
public class AIController {

    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;

    private AIStrategy aiStrategy;
    private Cursor[] cursors;
    private int[][] map;
    private int[][] gradient;
    private int[] teamFighterCount;
    private int globalClock;

    // Mémoire pour détecter si l'IA est bloquée (position qui ne change pas)
    private int[] lastCursorX = new int[6];
    private int[] lastCursorY = new int[6];
    private int[] stuckCounter = new int[6]; // Compteur de frames bloquées

    public AIController() {
        // Par défaut, utiliser AggressiveAI
        this.aiStrategy = new AggressiveAI();
    }

    public void setAIStrategy(AIStrategy strategy) {
        this.aiStrategy = strategy;
    }

    public void setGameState(Cursor[] cursors, int[][] map, int[][] gradient,
            int[] teamFighterCount, int globalClock) {
        this.cursors = cursors;
        this.map = map;
        this.gradient = gradient;
        this.teamFighterCount = teamFighterCount;
        this.globalClock = globalClock;
    }

    /**
     * Met à jour le curseur d'une équipe IA.
     */
    public void updateAICursor(int team) {
        if (cursors == null || cursors[team] == null) {
            return;
        }

        // Si aucune stratégie n'est définie, utiliser AggressiveAI par défaut
        if (aiStrategy == null) {
            aiStrategy = new AggressiveAI();
            GameLogger.getInstance().warn("IA non initialisée, utilisation de AggressiveAI par défaut");
        }

        // LOGS pour débogage (toutes les 5 secondes environ)
        if (globalClock % 1000 == 0) {
            GameLogger.getInstance().debug("IA Team %d [%s] - Clock: %d", team, aiStrategy.getName(), globalClock);
        }

        // Utiliser la stratégie d'IA sélectionnée
        // Créer un adaptateur GameState pour l'IA
        AIStrategy.GameState gameStateAdapter = new AIStrategy.GameState() {
            @Override
            public Point getCursorPosition(int t) {
                if (cursors != null && cursors[t] != null) {
                    return new Point(cursors[t].x, cursors[t].y);
                }
                return new Point(0, 0);
            }

            @Override
            public int getFighterCount(int t) {
                return teamFighterCount != null && t >= 0 && t < teamFighterCount.length
                        ? teamFighterCount[t]
                        : 0;
            }

            @Override
            public boolean isValidPosition(int x, int y) {
                return x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT;
            }

            @Override
            public boolean isWall(int x, int y) {
                if (!isValidPosition(x, y))
                    return true;
                return map != null && map[y][x] == -1;
            }

            @Override
            public int getGradient(int t, int x, int y) {
                if (!isValidPosition(x, y))
                    return GameConfig.AREA_START_GRADIENT;
                if (gradient == null || t < 0 || t >= gradient.length)
                    return GameConfig.AREA_START_GRADIENT;
                int idx = y * MAP_WIDTH + x;
                if (idx < 0 || idx >= gradient[t].length)
                    return GameConfig.AREA_START_GRADIENT;
                return gradient[t][idx];
            }

            @Override
            public int getMapWidth() {
                return MAP_WIDTH;
            }

            @Override
            public int getMapHeight() {
                return MAP_HEIGHT;
            }
        };

        // Calculer la prochaine position avec la stratégie d'IA
        Point nextPos = aiStrategy.calculateNextMove(gameStateAdapter, team);

        // Vérifier que la nouvelle position n'est pas un obstacle
        // Amélioration : trouver la case libre la plus proche si bloqué
        int currentX = cursors[team].x;
        int currentY = cursors[team].y;

        // Toujours vérifier et corriger la position, même si elle semble valide
        // (car l'IA peut calculer une position sur un obstacle)
        if (map != null) {
            // Vérifier si la position calculée est valide
            boolean isValid = (nextPos.y >= 0 && nextPos.y < map.length
                    && nextPos.x >= 0 && nextPos.x < map[0].length
                    && map[nextPos.y][nextPos.x] != -1);

            if (!isValid) {
                // La position est invalide ou un obstacle, chercher une case libre proche
                int[] freePos = findNearestFreePositionForAI(nextPos.x, nextPos.y, currentX, currentY, map, 8);
                if (freePos != null && (freePos[0] != currentX || freePos[1] != currentY)) {
                    // On a trouvé une position différente de la position actuelle
                    nextPos.x = freePos[0];
                    nextPos.y = freePos[1];
                } else {
                    // Aucune position libre trouvée autour de la cible, chercher autour de la
                    // position actuelle
                    // Essayer toutes les directions autour de la position actuelle
                    boolean found = false;
                    for (int radius = 1; radius <= 5 && !found; radius++) {
                        for (int angle = 0; angle < 360 && !found; angle += 10) {
                            double rad = Math.toRadians(angle);
                            int testX = currentX + (int) (Math.cos(rad) * radius);
                            int testY = currentY + (int) (Math.sin(rad) * radius);

                            if (testX >= 0 && testX < map[0].length && testY >= 0 && testY < map.length) {
                                if (map[testY][testX] != -1) {
                                    nextPos.x = testX;
                                    nextPos.y = testY;
                                    found = true;
                                }
                            }
                        }
                    }

                    // Si vraiment rien ne fonctionne, ne pas déplacer (rester sur place)
                    if (!found) {
                        return; // Ne pas déplacer le curseur
                    }
                }
            }
        }

        // Vérifier une dernière fois que la position finale n'est pas un obstacle
        // et forcer un mouvement si nécessaire
        if (map != null && nextPos.y >= 0 && nextPos.y < map.length
                && nextPos.x >= 0 && nextPos.x < map[0].length) {
            if (map[nextPos.y][nextPos.x] == -1) {
                // La position finale est toujours un obstacle, chercher n'importe quelle case
                // libre
                boolean found = false;
                // Recherche exhaustive autour de la position actuelle
                for (int radius = 1; radius <= 10 && !found; radius++) {
                    for (int angle = 0; angle < 360 && !found; angle += 5) {
                        double rad = Math.toRadians(angle);
                        int testX = currentX + (int) (Math.cos(rad) * radius);
                        int testY = currentY + (int) (Math.sin(rad) * radius);

                        if (testX >= 0 && testX < map[0].length && testY >= 0 && testY < map.length) {
                            if (map[testY][testX] != -1) {
                                nextPos.x = testX;
                                nextPos.y = testY;
                                found = true;
                            }
                        }
                    }
                }

                // Si vraiment aucune case libre n'est trouvée, ne pas déplacer
                if (!found) {
                    return;
                }
            }
        }

        // Appliquer le mouvement seulement si la position a changé
        int oldX = cursors[team].x;
        int oldY = cursors[team].y;

        // Détecter si l'IA est bloquée (même position pendant plusieurs frames)
        if (oldX == lastCursorX[team] && oldY == lastCursorY[team]) {
            stuckCounter[team]++;
        } else {
            stuckCounter[team] = 0; // Réinitialiser le compteur si le curseur bouge
        }

        // Si l'IA est bloquée depuis plus de 10 frames, forcer un mouvement de
        // contournement
        if (stuckCounter[team] > 10 && map != null) {
            // Forcer un mouvement dans une direction aléatoire mais valide
            boolean forcedMove = false;
            int attempt = 0;
            while (!forcedMove && attempt < 50) {
                int randomAngle = (int) (Math.random() * 360);
                double rad = Math.toRadians(randomAngle);
                int testX = oldX + (int) (Math.cos(rad) * 5);
                int testY = oldY + (int) (Math.sin(rad) * 5);

                if (testX >= 0 && testX < map[0].length && testY >= 0 && testY < map.length) {
                    if (map[testY][testX] != -1) {
                        nextPos.x = testX;
                        nextPos.y = testY;
                        forcedMove = true;
                        stuckCounter[team] = 0; // Réinitialiser le compteur
                        GameLogger.getInstance().debug("IA Team %d débloquée par mouvement forcé", team);
                    }
                }
                attempt++;
            }
        }

        // Ne déplacer que si la nouvelle position est différente
        if (nextPos.x != oldX || nextPos.y != oldY) {
            cursors[team].x = nextPos.x;
            cursors[team].y = nextPos.y;
            lastCursorX[team] = nextPos.x;
            lastCursorY[team] = nextPos.y;
        } else {
            // Même si on ne bouge pas, mettre à jour la mémoire
            lastCursorX[team] = oldX;
            lastCursorY[team] = oldY;
        }

        // Logs détaillés (toutes les secondes)
        if (globalClock % 120 == 0) {
            GameLogger.getInstance().debug("IA Team %d [%s] → (%d,%d) → (%d,%d)",
                    team, aiStrategy.getName(), oldX, oldY, nextPos.x, nextPos.y);
        }
    }

    /**
     * Trouve la case libre la plus proche d'une position donnée pour l'IA.
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
    private int[] findNearestFreePositionForAI(int targetX, int targetY, int currentX, int currentY, int[][] map,
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
        // Augmenter le rayon maximum pour éviter de rester bloqué
        int searchRadius = Math.max(maxRadius, 8); // Au moins 8 pixels de recherche
        for (int radius = 1; radius <= searchRadius; radius++) {
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

        // Si toujours rien trouvé, chercher autour de la position actuelle (recul)
        for (int radius = 1; radius <= 3; radius++) {
            for (int dx2 = -radius; dx2 <= radius; dx2++) {
                for (int dy2 = -radius; dy2 <= radius; dy2++) {
                    int dist = Math.max(Math.abs(dx2), Math.abs(dy2));
                    if (dist != radius) {
                        continue;
                    }

                    int testX = currentX + dx2;
                    int testY = currentY + dy2;

                    if (testX >= 0 && testX < mapWidth && testY >= 0 && testY < mapHeight) {
                        if (map[testY][testX] != -1) {
                            return new int[] { testX, testY };
                        }
                    }
                }
            }
        }

        // Dernière option : retourner la position actuelle (rester sur place)
        return new int[] { currentX, currentY };
    }
}
