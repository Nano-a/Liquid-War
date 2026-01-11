package fr.uparis.informatique.cpoo5.liquidwar.view.input;

import java.awt.Point;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.controller.command.CommandHistory;
import fr.uparis.informatique.cpoo5.liquidwar.controller.command.MoveCursorCommand;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;

/**
 * Contrôleur pour gérer le mouvement du curseur du joueur.
 * 
 * Responsabilités :
 * - Calculer la nouvelle position du curseur selon les touches pressées
 * - Gérer les différents types de contrôle (Flèches, ZQSD, Souris)
 * - Appliquer les limites de la carte
 */
public class PlayerCursorController {

    private Cursor[] cursors;
    private String[] teamControlTypes;
    private String[] teamTypes;
    private int activeTeams;
    private int playerTeam = 0;
    private boolean networkMode = false;
    private CommandHistory commandHistory;
    private int[][] map; // Carte pour vérifier les obstacles

    // Positions du curseur du joueur principal
    private int playerCursorX = GameConfig.MAP_WIDTH / 2;
    private int playerCursorY = GameConfig.MAP_HEIGHT / 2;

    public PlayerCursorController(CommandHistory commandHistory) {
        this.commandHistory = commandHistory;
    }

    public void setConfiguration(Cursor[] cursors, String[] teamControlTypes,
            String[] teamTypes, int activeTeams,
            int playerTeam, boolean networkMode) {
        this.cursors = cursors;
        this.teamControlTypes = teamControlTypes;
        this.teamTypes = teamTypes;
        this.activeTeams = activeTeams;
        this.playerTeam = playerTeam;
        this.networkMode = networkMode;
    }

    /**
     * Définit la carte pour vérifier les obstacles.
     */
    public void setMap(int[][] map) {
        this.map = map;
    }

    public void setPlayerCursorPosition(int x, int y) {
        this.playerCursorX = x;
        this.playerCursorY = y;
    }

    public Point getPlayerCursorPosition() {
        return new Point(playerCursorX, playerCursorY);
    }

    /**
     * Met à jour le curseur de toutes les équipes humaines.
     */
    public void updatePlayerCursor(GameInputHandler.KeyState keyState) {
        if (!networkMode) {
            // Mode local : mettre à jour toutes les équipes humaines
            for (int team = 0; team < activeTeams; team++) {
                if (isTeamHuman(team)) {
                    updateCursorForTeam(team, keyState);
                }
            }
        } else {
            // Mode réseau : seulement l'équipe du joueur local
            if (isTeamHuman(0)) {
                updateCursorForTeam(0, keyState);
            }
        }
    }

    /**
     * Met à jour le curseur d'une équipe spécifique selon son type de contrôle.
     */
    private void updateCursorForTeam(int team, GameInputHandler.KeyState keyState) {
        if (!isTeamHuman(team) || cursors == null || cursors[team] == null) {
            return;
        }

        if (team >= teamControlTypes.length || teamControlTypes[team] == null) {
            return;
        }

        String controlType = teamControlTypes[team];

        // La souris est gérée dans mouseMoved
        if ("Souris".equals(controlType)) {
            return;
        }

        int speed = GameConfig.CURSOR_MOVE_SPEED;
        int horiz = 0, vert = 0;

        if ("Flèches".equals(controlType)) {
            // Utiliser les touches fléchées
            if ((keyState.right && keyState.left) || (!keyState.right && !keyState.left))
                horiz = 0;
            else if (keyState.right && !keyState.left)
                horiz = 1;
            else if (!keyState.right && keyState.left)
                horiz = -1;

            if ((keyState.up && keyState.down) || (!keyState.up && !keyState.down))
                vert = 0;
            else if (keyState.up && !keyState.down)
                vert = -1;
            else if (!keyState.up && keyState.down)
                vert = 1;
        } else if ("ZQSD".equals(controlType)) {
            // Utiliser les touches ZQSD
            if ((keyState.d && keyState.q) || (!keyState.d && !keyState.q))
                horiz = 0;
            else if (keyState.d && !keyState.q)
                horiz = 1;
            else if (!keyState.d && keyState.q)
                horiz = -1;

            if ((keyState.z && keyState.s) || (!keyState.z && !keyState.s))
                vert = 0;
            else if (keyState.z && !keyState.s)
                vert = -1;
            else if (!keyState.z && keyState.s)
                vert = 1;
        }

        // Calculer la nouvelle position
        int currentX = cursors[team].x;
        int currentY = cursors[team].y;
        int newX = currentX + horiz * speed;
        int newY = currentY + vert * speed;

        // Limiter aux bords
        newX = Math.max(10, Math.min(GameConfig.MAP_WIDTH - 10, newX));
        newY = Math.max(10, Math.min(GameConfig.MAP_HEIGHT - 10, newY));

        // Vérifier que la nouvelle position n'est pas un obstacle
        // Amélioration : trouver la case libre la plus proche si bloqué
        if (map != null && newY >= 0 && newY < map.length && newX >= 0 && newX < map[0].length) {
            if (map[newY][newX] == -1) {
                // La position exacte est un obstacle, chercher une case libre proche
                int[] freePos = findNearestFreePosition(newX, newY, currentX, currentY, map, 3);
                if (freePos != null) {
                    newX = freePos[0];
                    newY = freePos[1];
                } else {
                    // Aucune case libre trouvée, ne pas déplacer
                    return;
                }
            }
        }

        // Si la position a changé
        if (newX != currentX || newY != currentY) {
            // Pour l'équipe du joueur principal, utiliser le Command Pattern
            if (team == playerTeam) {
                Point newPos = new Point(newX, newY);
                MoveCursorCommand cmd = new MoveCursorCommand(cursors[team], newPos);
                commandHistory.execute(cmd);
                playerCursorX = newX;
                playerCursorY = newY;
            } else {
                // Pour les autres équipes humaines, mettre à jour directement
                cursors[team].x = newX;
                cursors[team].y = newY;
            }
        }
    }

    /**
     * Vérifie si une équipe est contrôlée par un humain.
     */
    private boolean isTeamHuman(int team) {
        if (teamTypes == null || team < 0 || team >= teamTypes.length) {
            return false;
        }
        return "Humain".equals(teamTypes[team]);
    }

    /**
     * Définit la position du curseur du joueur principal.
     */
    public void setPlayerCursor(int x, int y) {
        playerCursorX = x;
        playerCursorY = y;
        if (cursors != null && cursors[playerTeam] != null) {
            cursors[playerTeam].x = x;
            cursors[playerTeam].y = y;
        }
    }

    /**
     * Trouve la case libre la plus proche d'une position donnée.
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
    private int[] findNearestFreePosition(int targetX, int targetY, int currentX, int currentY, int[][] map,
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
}
