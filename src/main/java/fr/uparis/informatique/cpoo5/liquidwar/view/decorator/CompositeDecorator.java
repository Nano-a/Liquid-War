package fr.uparis.informatique.cpoo5.liquidwar.view.decorator;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Décorateur composite permettant de combiner plusieurs décorateurs.
 * 
 * Applique tous les décorateurs activés dans l'ordre.
 * 
 * @author Liquid War Team
 */
public class CompositeDecorator implements ParticleDecorator {
    
    private final List<ParticleDecorator> decorators = new ArrayList<>();
    private boolean enabled = true;
    
    /**
     * Ajoute un décorateur à la liste.
     */
    public void addDecorator(ParticleDecorator decorator) {
        if (decorator != null && !decorators.contains(decorator)) {
            decorators.add(decorator);
        }
    }
    
    /**
     * Retire un décorateur de la liste.
     */
    public void removeDecorator(ParticleDecorator decorator) {
        decorators.remove(decorator);
    }
    
    /**
     * Retire tous les décorateurs.
     */
    public void clearDecorators() {
        decorators.clear();
    }
    
    @Override
    public void decorate(Graphics2D g, Fighter fighter, double zoom, int globalClock) {
        if (!enabled) {
            return;
        }
        
        // Appliquer tous les décorateurs activés
        for (ParticleDecorator decorator : decorators) {
            if (decorator.isEnabled()) {
                decorator.decorate(g, fighter, zoom, globalClock);
            }
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Retourne le nombre de décorateurs.
     */
    public int getDecoratorCount() {
        return decorators.size();
    }
    
    /**
     * Retourne la liste des décorateurs.
     */
    public List<ParticleDecorator> getDecorators() {
        return new ArrayList<>(decorators);
    }
}

