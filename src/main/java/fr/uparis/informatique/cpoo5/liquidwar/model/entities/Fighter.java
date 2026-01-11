package fr.uparis.informatique.cpoo5.liquidwar.model.entities;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

/**
 * Représente un combattant (fighter) dans le jeu Liquid War.
 * 
 * IMPORTANT: Code extrait tel quel de LiquidWarGame.java (lignes 959-970)
 * pour améliorer la structure sans changer le comportement.
 */
public class Fighter {
    public int x, y; // Gardé public pour compatibilité avec le code existant
    public int team;
    public int health;
    
    public Fighter(int x, int y, int team) {
        this.x = x;
        this.y = y;
        this.team = team;
        this.health = GameConfig.FIGHTER_INITIAL_HEALTH; // Anciennement GameArea.FIGHTER_HEALTH
    }
}

