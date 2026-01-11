package fr.uparis.informatique.cpoo5.liquidwar.view.decorator;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

/**
 * Décorateur ajoutant une traînée de mouvement aux particules.
 * 
 * Crée un effet visuel de trainée colorée derrière les particules
 * pour mieux visualiser leur mouvement et direction.
 * 
 * @author Liquid War Team
 */
public class TrailDecorator implements ParticleDecorator {
    
    private boolean enabled = false;
    private final int trailLength = 5; // Longueur de la traînée en pixels
    
    @Override
    public void decorate(Graphics2D g, Fighter fighter, double zoom, int globalClock) {
        if (!enabled) {
            return;
        }
        
        // Couleur basée sur l'équipe
        Color baseColor = (fighter.team == 0) ? Color.BLUE : Color.RED;
        
        // Sauvegarder la composite d'origine
        var originalComposite = g.getComposite();
        
        // Dessiner la traînée avec transparence décroissante
        for (int i = 1; i <= trailLength; i++) {
            float alpha = 0.3f * (1.0f - (float) i / trailLength);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(baseColor);
            
            int x = (int) ((fighter.x - i) * zoom);
            int y = (int) (fighter.y * zoom);
            int size = (int) Math.max(1, zoom * 0.8);
            
            g.fillOval(x, y, size, size);
        }
        
        // Restaurer la composite
        g.setComposite(originalComposite);
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("✨ Traînées particules: " + (enabled ? "ON" : "OFF"));
    }
}

