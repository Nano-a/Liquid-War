package fr.uparis.informatique.cpoo5.liquidwar.view;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Visualiseur optionnel du gradient.
 * 
 * Affiche une heatmap colorée représentant les valeurs du gradient
 * pour aider au débogage et à la compréhension du pathfinding.
 * 
 * Activation : Touche 'G' pour toggle la visualisation
 */
public class GradientVisualizer {
    
    private boolean enabled = false;
    private int maxGradientValue = 2000000; // AREA_START_GRADIENT
    
    /**
     * Active ou désactive la visualisation du gradient.
     */
    public void toggle() {
        enabled = !enabled;
        System.out.println("🔍 Visualisation gradient: " + (enabled ? "ON" : "OFF"));
    }
    
    /**
     * Vérifie si la visualisation est activée.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Définit si la visualisation est activée.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Dessine le gradient sous forme de heatmap.
     * 
     * @param g Graphics2D pour le rendu
     * @param gradient Tableau 2D du gradient [team][idx]
     * @param team Équipe dont on affiche le gradient
     * @param mapWidth Largeur de la carte
     * @param mapHeight Hauteur de la carte
     * @param zoom Facteur de zoom
     */
    public void renderGradient(Graphics2D g, int[][] gradient, int team, 
                               int mapWidth, int mapHeight, double zoom) {
        if (!enabled || gradient == null || team >= gradient.length) {
            return;
        }
        
        int[] teamGradient = gradient[team];
        
        // Transparence pour voir les particules en dessous
        g.setComposite(java.awt.AlphaComposite.getInstance(
            java.awt.AlphaComposite.SRC_OVER, 0.4f));
        
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int idx = y * mapWidth + x;
                int gradValue = teamGradient[idx];
                
                // Normaliser la valeur (0.0 = proche curseur, 1.0 = loin)
                float normalized = Math.min(1.0f, (float) gradValue / maxGradientValue);
                
                // Heatmap : Bleu (proche) -> Vert -> Jaune -> Rouge (loin)
                Color color = getHeatmapColor(normalized);
                g.setColor(color);
                
                // Dessiner le pixel zoomé
                g.fillRect((int) (x * zoom), (int) (y * zoom), 
                          (int) Math.ceil(zoom), (int) Math.ceil(zoom));
            }
        }
        
        // Remettre l'opacité normale
        g.setComposite(java.awt.AlphaComposite.getInstance(
            java.awt.AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * Calcule une couleur heatmap basée sur une valeur normalisée.
     * 
     * @param value Valeur entre 0.0 (proche) et 1.0 (loin)
     * @return Couleur correspondante
     */
    private Color getHeatmapColor(float value) {
        // Échelle de couleur : Bleu -> Cyan -> Vert -> Jaune -> Rouge
        if (value < 0.25f) {
            // Bleu -> Cyan
            float t = value * 4.0f;
            return new Color(0, (int) (t * 255), 255);
        } else if (value < 0.5f) {
            // Cyan -> Vert
            float t = (value - 0.25f) * 4.0f;
            return new Color(0, 255, (int) ((1 - t) * 255));
        } else if (value < 0.75f) {
            // Vert -> Jaune
            float t = (value - 0.5f) * 4.0f;
            return new Color((int) (t * 255), 255, 0);
        } else {
            // Jaune -> Rouge
            float t = (value - 0.75f) * 4.0f;
            return new Color(255, (int) ((1 - t) * 255), 0);
        }
    }
    
    /**
     * Affiche une légende pour la heatmap.
     * 
     * @param g Graphics2D pour le rendu
     * @param x Position X de la légende
     * @param y Position Y de la légende
     */
    public void renderLegend(Graphics2D g, int x, int y) {
        if (!enabled) {
            return;
        }
        
        // Fond semi-transparent
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(x, y, 200, 80);
        
        // Titre
        g.setColor(Color.WHITE);
        g.drawString("Gradient Heatmap:", x + 10, y + 20);
        
        // Barre de couleur
        int barWidth = 180;
        int barHeight = 20;
        int barX = x + 10;
        int barY = y + 30;
        
        for (int i = 0; i < barWidth; i++) {
            float normalized = (float) i / barWidth;
            g.setColor(getHeatmapColor(normalized));
            g.fillRect(barX + i, barY, 1, barHeight);
        }
        
        // Labels
        g.setColor(Color.WHITE);
        g.drawString("Proche curseur", barX, barY + barHeight + 15);
        g.drawString("Loin", barX + barWidth - 30, barY + barHeight + 15);
    }
}

