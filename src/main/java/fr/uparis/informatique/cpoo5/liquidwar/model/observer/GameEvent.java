package fr.uparis.informatique.cpoo5.liquidwar.model.observer;

/**
 * Énumération des événements du jeu observables.
 * 
 * @author Liquid War Team
 */
public enum GameEvent {
    /** État du jeu mis à jour (position particules, gradient, etc.) */
    STATE_UPDATED,
    
    /** Nombre de particules modifié */
    PARTICLE_COUNT_CHANGED,
    
    /** Combat en cours */
    COMBAT_OCCURRED,
    
    /** Particule changée de camp */
    TEAM_CONVERSION,
    
    /** Jeu terminé */
    GAME_OVER,
    
    /** Jeu mis en pause */
    GAME_PAUSED,
    
    /** Jeu repris */
    GAME_RESUMED,
    
    /** Curseur déplacé */
    CURSOR_MOVED,
    
    /** Configuration modifiée */
    CONFIG_CHANGED
}
