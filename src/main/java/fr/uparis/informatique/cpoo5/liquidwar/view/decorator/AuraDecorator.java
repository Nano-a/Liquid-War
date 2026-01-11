package fr.uparis.informatique.cpoo5.liquidwar.view.decorator;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

/**
 * Décorateur ajoutant une aura d'énergie autour des particules.
 * 
 * Affiche une aura colorée dont l'intensité dépend de la santé de la particule.
 * Crée un effet visuel attractif pour montrer l'énergie des particules.
 * 
 * @author Liquid War Team
 */
public class AuraDecorator implements ParticleDecorator {
    
    private boolean enabled = false;
    
    @Override
    public void decorate(Graphics2D g, Fighter fighter, double zoom, int globalClock) {
        if (!enabled) {
            return;
        }
        
        // Calculer l'intensité basée sur la santé
        float healthRatio = fighter.health / 100.0f;
        if (healthRatio > 1.0f) healthRatio = 1.0f;
        
        // Couleur de l'aura
        Color teamColor = (fighter.team == 0) ? new Color(0, 100, 255) : new Color(255, 50, 50);
        
        // Centre de l'aura
        float centerX = (float) ((fighter.x + 0.5) * zoom);
        float centerY = (float) ((fighter.y + 0.5) * zoom);
        float radius = (float) (zoom * 2.5);
        
        // Gradient radial pour l'aura
        Point2D center = new Point2D.Float(centerX, centerY);
        float[] dist = {0.0f, 0.5f, 1.0f};
        Color[] colors = {
            new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 
                     (int) (100 * healthRatio)),
            new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 
                     (int) (50 * healthRatio)),
            new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 0)
        };
        
        RadialGradientPaint paint = new RadialGradientPaint(center, radius, dist, colors);
        
        // Sauvegarder l'état
        var originalPaint = g.getPaint();
        var originalComposite = g.getComposite();
        
        // Dessiner l'aura
        g.setPaint(paint);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g.fillOval((int) (centerX - radius), (int) (centerY - radius), 
                  (int) (radius * 2), (int) (radius * 2));
        
        // Restaurer l'état
        g.setPaint(originalPaint);
        g.setComposite(originalComposite);
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("🌟 Auras d'énergie: " + (enabled ? "ON" : "OFF"));
    }
}

