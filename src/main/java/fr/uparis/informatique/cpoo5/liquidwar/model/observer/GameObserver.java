package fr.uparis.informatique.cpoo5.liquidwar.model.observer;

/**
 * Observer Pattern : Interface pour les observateurs du modèle de jeu.
 * 
 * Les vues implémentent cette interface pour être notifiées des changements
 * dans le modèle de jeu, assurant la séparation Model-View.
 * 
 * @author Liquid War Team
 */
public interface GameObserver {
    
    /**
     * Appelé quand l'état du jeu change.
     * 
     * @param event Type d'événement
     * @param data Données associées à l'événement
     */
    void onGameStateChanged(GameEvent event, Object data);
    
    /**
     * Appelé quand le nombre de particules change.
     * 
     * @param teamCounts Nombre de particules par équipe
     */
    void onParticleCountChanged(int[] teamCounts);
    
    /**
     * Appelé quand le jeu se termine.
     * 
     * @param winnerTeam Équipe gagnante
     * @param gameDuration Durée de la partie en secondes
     */
    void onGameOver(int winnerTeam, long gameDuration);
    
    /**
     * Appelé quand le jeu est mis en pause ou reprend.
     * 
     * @param paused true si en pause, false sinon
     */
    void onPauseStateChanged(boolean paused);
}
