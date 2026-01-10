package fr.uparis.informatique.cpoo5.liquidwar.model.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject du pattern Observer.
 * 
 * Gère la liste des observateurs et les notifie des changements.
 * Thread-safe avec CopyOnWriteArrayList.
 * 
 * @author Liquid War Team
 */
public class GameSubject {
    
    // Thread-safe pour multithreading
    private final List<GameObserver> observers = new CopyOnWriteArrayList<>();
    
    // Optimisation : ne notifier que si changement significatif
    private int[] lastTeamCounts = new int[2];
    private boolean lastPausedState = false;
    
    /**
     * Ajoute un observateur.
     * 
     * @param observer Observateur à ajouter
     */
    public void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Retire un observateur.
     * 
     * @param observer Observateur à retirer
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Retire tous les observateurs.
     */
    public void clearObservers() {
        observers.clear();
    }
    
    /**
     * Notifie tous les observateurs d'un changement d'état.
     * 
     * @param event Type d'événement
     * @param data Données associées
     */
    public void notifyStateChanged(GameEvent event, Object data) {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged(event, data);
        }
    }
    
    /**
     * Notifie tous les observateurs d'un changement de particules.
     * Optimisé : ne notifie que si changement réel.
     * 
     * @param teamCounts Nombre de particules par équipe
     */
    public void notifyParticleCountChanged(int[] teamCounts) {
        // Optimisation : ne notifier que si changement
        if (hasCountChanged(teamCounts)) {
            System.arraycopy(teamCounts, 0, lastTeamCounts, 0, 
                           Math.min(teamCounts.length, lastTeamCounts.length));
            
            for (GameObserver observer : observers) {
                observer.onParticleCountChanged(teamCounts);
            }
        }
    }
    
    /**
     * Notifie tous les observateurs de la fin du jeu.
     * 
     * @param winnerTeam Équipe gagnante
     * @param gameDuration Durée de la partie
     */
    public void notifyGameOver(int winnerTeam, long gameDuration) {
        for (GameObserver observer : observers) {
            observer.onGameOver(winnerTeam, gameDuration);
        }
    }
    
    /**
     * Notifie tous les observateurs d'un changement de pause.
     * Optimisé : ne notifie que si changement réel.
     * 
     * @param paused État de pause
     */
    public void notifyPauseStateChanged(boolean paused) {
        // Optimisation : ne notifier que si changement
        if (paused != lastPausedState) {
            lastPausedState = paused;
            
            for (GameObserver observer : observers) {
                observer.onPauseStateChanged(paused);
            }
        }
    }
    
    /**
     * Vérifie si le nombre de particules a changé.
     */
    private boolean hasCountChanged(int[] teamCounts) {
        if (teamCounts.length != lastTeamCounts.length) {
            return true;
        }
        
        for (int i = 0; i < teamCounts.length; i++) {
            if (teamCounts[i] != lastTeamCounts[i]) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Retourne le nombre d'observateurs enregistrés.
     */
    public int getObserverCount() {
        return observers.size();
    }
}

