package fr.uparis.informatique.cpoo5.liquidwar.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.config.RenderConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;

/**
 * Moteur de rendu pour Liquid War.
 * 
 * IMPORTANT : Code extrait de GameCanvas.java (lignes 723-831) pour réduire la
 * taille du fichier.
 * Le code est déplacé TEL QUEL sans modification de la logique.
 * 
 * Responsabilités :
 * - Dessin du buffer statique (obstacles et fond)
 * - Rendu des combattants
 * - Rendu des curseurs
 * - Affichage des statistiques
 */
public class GameRenderer {

    private static final int MAP_WIDTH = GameConfig.MAP_WIDTH;
    private static final int MAP_HEIGHT = GameConfig.MAP_HEIGHT;
    private static final Color[] DEFAULT_TEAM_COLORS = RenderConfig.TEAM_COLORS;
    private static final int MAX_HEALTH = Math.max(1, GameConfig.FIGHTER_INITIAL_HEALTH);

    // Couleurs personnalisées (peuvent être définies par le menu)
    private static Color[] customTeamColors = null;

    /**
     * Couleurs pré-calculées par équipe et par niveau de santé, pour reproduire
     * l'effet palette du code C : plus un fighter est blessé, plus sa couleur est
     * sombre. Cela évite de recalculer à chaque frame.
     */
    private static int[][] TEAM_HEALTH_COLORS = buildHealthColorTable();

    // Empêcher l'instanciation
    private GameRenderer() {
    }

    /**
     * Définit les couleurs personnalisées pour les équipes.
     * 
     * @param colorIndices Tableau d'indices de couleurs (0-5) pour chaque équipe
     */
    public static void setCustomTeamColors(int[] colorIndices) {
        if (colorIndices == null || colorIndices.length == 0) {
            customTeamColors = null;
            TEAM_HEALTH_COLORS = buildHealthColorTable();
            return;
        }

        customTeamColors = new Color[colorIndices.length];
        for (int i = 0; i < colorIndices.length; i++) {
            int colorIndex = Math.max(0, Math.min(5, colorIndices[i]));
            customTeamColors[i] = DEFAULT_TEAM_COLORS[colorIndex];
        }

        // Reconstruire la table de couleurs avec les nouvelles couleurs
        TEAM_HEALTH_COLORS = buildHealthColorTable();
    }

    /**
     * Obtient les couleurs actuellement utilisées (personnalisées ou par défaut).
     */
    private static Color[] getTeamColors() {
        return customTeamColors != null ? customTeamColors : DEFAULT_TEAM_COLORS;
    }

    /**
     * Dessiner le buffer statique (obstacles et fond) - UNE SEULE FOIS
     */
    public static void drawStaticBuffer(int[][] map, int[] staticBufferPixels,
            boolean[] staticBufferDirtyRef) {
        if (!staticBufferDirtyRef[0])
            return;

        // OPTIMISATION : Utiliser l'accès direct aux pixels pour le fond (plus rapide)
        Color bgColor1Obj = RenderConfig.BACKGROUND_COLOR_1;
        Color bgColor2Obj = RenderConfig.BACKGROUND_COLOR_2;
        int obstacleColor = RenderConfig.OBSTACLE_COLOR.getRGB();

        for (int y = 0; y < MAP_HEIGHT; y++) {
            // Gradient vertical simple
            float ratio = (float) y / MAP_HEIGHT;
            int r = (int) ((bgColor1Obj.getRed() * (1 - ratio)) + (bgColor2Obj.getRed() * ratio));
            int g = (int) ((bgColor1Obj.getGreen() * (1 - ratio)) + (bgColor2Obj.getGreen() * ratio));
            int b = (int) ((bgColor1Obj.getBlue() * (1 - ratio)) + (bgColor2Obj.getBlue() * ratio));
            int lineColor = (r << 16) | (g << 8) | b;

            for (int x = 0; x < MAP_WIDTH; x++) {
                int idx = y * MAP_WIDTH + x;
                if (map[y][x] == -1) {
                    staticBufferPixels[idx] = obstacleColor;
                } else {
                    staticBufferPixels[idx] = lineColor;
                }
            }
        }

        staticBufferDirtyRef[0] = false;
    }

    /**
     * Dessiner les combattants sur le buffer
     */
    public static void drawFighters(ArrayList<Fighter> fighters, int[] gameBufferPixels) {
        // Dessiner les fighters pixel par pixel (accès direct aux pixels = très rapide)
        for (Fighter f : fighters) {
            int idx = f.y * MAP_WIDTH + f.x;
            if (idx >= 0 && idx < gameBufferPixels.length) {
                gameBufferPixels[idx] = getHealthTintedColor(f.team, f.health);
            }
        }
    }

    /**
     * Calcule une table de couleurs atténuées par la santé pour chaque équipe.
     * L'atténuation va de 35% (quasi mort) à 100% (pleinement en forme), en
     * reproduisant l'idée de palette utilisée dans la version C.
     */
    private static int[][] buildHealthColorTable() {
        Color[] colors = getTeamColors();
        int teams = colors.length;
        int[][] table = new int[teams][MAX_HEALTH];
        for (int t = 0; t < teams; t++) {
            Color base = colors[t];
            for (int h = 0; h < MAX_HEALTH; h++) {
                float ratio = (MAX_HEALTH <= 1) ? 1.0f : (float) h / (MAX_HEALTH - 1);
                float factor = 0.35f + 0.65f * ratio; // 35% à 100%
                int r = Math.min(255, Math.max(0, Math.round(base.getRed() * factor)));
                int g = Math.min(255, Math.max(0, Math.round(base.getGreen() * factor)));
                int b = Math.min(255, Math.max(0, Math.round(base.getBlue() * factor)));
                table[t][h] = (r << 16) | (g << 8) | b;
            }
        }
        return table;
    }

    /**
     * Récupère la couleur en fonction de l'équipe et de la santé actuelle.
     */
    private static int getHealthTintedColor(int team, int health) {
        int t = Math.floorMod(team, TEAM_HEALTH_COLORS.length);
        int h = Math.min(Math.max(health, 0), MAX_HEALTH - 1);
        return TEAM_HEALTH_COLORS[t][h];
    }

    /**
     * Réinitialise les couleurs personnalisées (utilise les couleurs par défaut).
     */
    public static void resetTeamColors() {
        customTeamColors = null;
        TEAM_HEALTH_COLORS = buildHealthColorTable();
    }

    /**
     * Obtient les couleurs actuellement utilisées (pour les autres composants).
     */
    public static Color[] getCurrentTeamColors() {
        return getTeamColors();
    }

    /**
     * Dessiner les curseurs sur le buffer
     * Le curseur a un contour contrasté pour rester visible même entouré de
     * particules
     */
    public static void drawCursors(Cursor[] cursors, int activeTeams, Graphics2D bufferGraphics) {
        Color[] colors = getTeamColors();
        for (int i = 0; i < activeTeams; i++) {
            if (cursors[i] != null && cursors[i].active != 0) {
                Color cursorColor = colors[i % colors.length];
                int size = RenderConfig.CURSOR_SIZE;
                int x = cursors[i].x;
                int y = cursors[i].y;

                // Calculer la luminosité de la couleur pour choisir le contour approprié
                // Si la couleur est sombre, utiliser un contour blanc, sinon noir
                double luminance = (0.299 * cursorColor.getRed() + 0.587 * cursorColor.getGreen()
                        + 0.114 * cursorColor.getBlue()) / 255.0;
                Color outlineColor = (luminance < 0.5) ? Color.WHITE : Color.BLACK;

                // Dessiner le contour (outline) pour que le curseur soit toujours visible
                bufferGraphics.setStroke(new BasicStroke(3)); // Contour plus épais
                bufferGraphics.setColor(outlineColor);

                // Cercle avec contour
                bufferGraphics.drawOval(x - size - 1, y - size - 1, (size + 1) * 2, (size + 1) * 2);
                // Croix avec contour
                bufferGraphics.drawLine(x - size * 2 - 1, y, x + size * 2 + 1, y);
                bufferGraphics.drawLine(x, y - size * 2 - 1, x, y + size * 2 + 1);

                // Dessiner le curseur principal avec la couleur de l'équipe
                bufferGraphics.setStroke(new BasicStroke(2)); // Ligne principale plus épaisse
                bufferGraphics.setColor(cursorColor);

                // Cercle principal
                bufferGraphics.drawOval(x - size, y - size, size * 2, size * 2);
                // Croix principale
                bufferGraphics.drawLine(x - size * 2, y, x + size * 2, y);
                bufferGraphics.drawLine(x, y - size * 2, x, y + size * 2);

                // Point central pour plus de visibilité
                bufferGraphics.setStroke(new BasicStroke(1));
                bufferGraphics.fillOval(x - 1, y - 1, 3, 3);
            }
        }
    }

    /**
     * Afficher les statistiques (HUD)
     */
    public static void drawHUD(Graphics2D g2d, int activeTeams, int[] teamFighterCount,
            int panelWidth, int panelHeight) {
        // Afficher les statistiques
        g2d.setColor(RenderConfig.HUD_TEXT_COLOR);
        g2d.setFont(new Font(RenderConfig.HUD_FONT_NAME, Font.BOLD, RenderConfig.HUD_FONT_SIZE_LARGE));
        int yPos = 30;
        Color[] colors = getTeamColors();
        for (int i = 0; i < activeTeams; i++) {
            String teamName = (i == 0) ? "Vous" : "Adversaire " + i;
            g2d.setColor(colors[i % colors.length]);
            g2d.drawString(teamName + ": " + teamFighterCount[i] + " fighters", 20, yPos);
            yPos += 25;
        }

        // Instructions
        g2d.setColor(RenderConfig.HUD_TEXT_COLOR);
        g2d.setFont(new Font(RenderConfig.HUD_FONT_NAME, Font.PLAIN, RenderConfig.HUD_FONT_SIZE_SMALL));
        g2d.drawString("Flèches ou souris pour déplacer | ESC pour menu", 20, panelHeight - 30);
    }

    /**
     * Afficher les logs de debug une seule fois
     */
    public static void printDebugLogs(int globalClock, BufferedImage gameBuffer,
            int panelWidth, int panelHeight) {
        if (globalClock <= 2) {
            System.out.println("==========================================");
            System.out.println("DEBUG paintComponent - AFFICHAGE (frame " + globalClock + ")");
            System.out.println("==========================================");
            System.out.println("gameBuffer dimensions: " + gameBuffer.getWidth() + "x" + gameBuffer.getHeight());
            System.out.println("GameArea getWidth(): " + panelWidth);
            System.out.println("GameArea getHeight(): " + panelHeight);
            System.out.println(
                    "Zoom appliqué: " + String.format("%.2f", panelWidth / (float) gameBuffer.getWidth()) + "x");
            System.out.println("Map visible: " + (panelWidth >= gameBuffer.getWidth() * 3 ? "OUI" : "PARTIELLE"));
            System.out.println("==========================================\n");
        }
    }
}
