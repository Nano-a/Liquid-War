package fr.uparis.informatique.cpoo5.liquidwar.view.decorator;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import java.awt.Graphics2D;

/**
 * Decorator Pattern : Interface pour ajouter des effets visuels aux particules.
 * 
 * Permet d'ajouter dynamiquement des effets sans modifier la classe Fighter :
 * - Traînées de mouvement
 * - Auras d'énergie
 * - Particules de combat
 * - Effets de sélection
 * 
 * @author Liquid War Team
 */
public interface ParticleDecorator {
    
    /**
     * Dessine l'effet décoratif sur une particule.
     * 
     * @param g Graphics2D pour le rendu
     * @param fighter Particule à décorer
     * @param zoom Facteur de zoom
     * @param globalClock Horloge globale pour animations
     */
    void decorate(Graphics2D g, Fighter fighter, double zoom, int globalClock);
    
    /**
     * Vérifie si le décorateur est actif.
     */
    boolean isEnabled();
    
    /**
     * Active ou désactive le décorateur.
     */
    void setEnabled(boolean enabled);
}

