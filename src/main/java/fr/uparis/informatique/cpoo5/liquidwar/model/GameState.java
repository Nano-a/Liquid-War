package fr.uparis.informatique.cpoo5.liquidwar.model;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * État du jeu encapsulé pour multithreading thread-safe.
 * 
 * <p>Cette classe centralise toutes les données partagées entre threads
 * et fournit une synchronisation explicite via des locks.
 * 
 * <h2>Synchronisation</h2>
 * <ul>
 *   <li><b>ReadWriteLock</b> : Permet plusieurs lectures simultanées, une seule écriture</li>
 *   <li><b>Gradient</b> : Locks séparés par équipe pour parallélisation</li>
 *   <li><b>Fighters</b> : Lock global pour cohérence</li>
 *   <li><b>Cursors</b> : Lock global pour simplicité</li>
 * </ul>
 * 
 * <h2>Usage Multithreading</h2>
 * <pre>{@code
 * // Thread de calcul
 * gameState.lockGradientWrite(0);
 * try {
 *     int[][] grad = gameState.getGradient(0);
 *     // Modifier grad...
 * } finally {
 *     gameState.unlockGradientWrite(0);
 * }
 * 
 * // Thread de rendu
 * gameState.lockFightersRead();
 * try {
 *     List<Fighter> fighters = gameState.getFighters();
 *     // Lire fighters...
 * } finally {
 *     gameState.unlockFightersRead();
 * }
 * }</pre>
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 * @see Fighter
 * @see Cursor
 */
public class GameState {
    
    // ==================== DONNÉES DU JEU ====================
    
    /** Carte du jeu (immuable après chargement) */
    private final int[][] map;
    
    /** Gradients par équipe [team][position] */
    private final int[][] gradient;
    
    /** Liste des fighters (mutable) */
    private final ArrayList<Fighter> fighters;
    
    /** Curseurs par équipe */
    private final Cursor[] cursors;
    
    /** Compteurs de fighters par équipe */
    private final int[] teamFighterCount;
    
    /** Nombre d'équipes actives */
    private final int activeTeams;
    
    // ==================== LOCKS POUR SYNCHRONISATION ====================
    
    /** Locks pour les gradients (un par équipe pour parallélisation) */
    private final ReentrantReadWriteLock[] gradientLocks;
    
    /** Lock pour les fighters (partagé) */
    private final ReentrantReadWriteLock fightersLock;
    
    /** Lock pour les curseurs */
    private final ReentrantReadWriteLock cursorsLock;
    
    /** Lock pour les compteurs */
    private final ReentrantReadWriteLock countersLock;
    
    // ==================== CONSTRUCTEUR ====================
    
    /**
     * Crée un nouvel état de jeu.
     * 
     * @param map Carte du jeu (sera copiée pour immutabilité)
     * @param gradient Gradients initiaux
     * @param fighters Liste initiale de fighters
     * @param cursors Curseurs initiaux
     * @param teamFighterCount Compteurs initiaux
     * @param activeTeams Nombre d'équipes actives
     */
    public GameState(int[][] map, int[][] gradient, ArrayList<Fighter> fighters,
                     Cursor[] cursors, int[] teamFighterCount, int activeTeams) {
        
        // Copier la map pour immutabilité
        this.map = new int[map.length][];
        for (int i = 0; i < map.length; i++) {
            this.map[i] = map[i].clone();
        }
        
        // Références (seront synchronisées)
        this.gradient = gradient;
        this.fighters = fighters;
        this.cursors = cursors;
        this.teamFighterCount = teamFighterCount;
        this.activeTeams = activeTeams;
        
        // Initialiser les locks
        this.gradientLocks = new ReentrantReadWriteLock[activeTeams];
        for (int i = 0; i < activeTeams; i++) {
            this.gradientLocks[i] = new ReentrantReadWriteLock();
        }
        
        this.fightersLock = new ReentrantReadWriteLock();
        this.cursorsLock = new ReentrantReadWriteLock();
        this.countersLock = new ReentrantReadWriteLock();
    }
    
    // ==================== ACCÈS À LA CARTE (READ-ONLY) ====================
    
    /**
     * Vérifie si une position est valide (dans les limites de la carte).
     */
    public boolean isValid(int x, int y) {
        return x >= 0 && x < GameConfig.MAP_WIDTH && 
               y >= 0 && y < GameConfig.MAP_HEIGHT;
    }
    
    /**
     * Vérifie si une position est un mur.
     */
    public boolean isWall(int x, int y) {
        if (!isValid(x, y)) return true;
        return map[y][x] == -1;
    }
    
    /**
     * Obtient la valeur de la carte à une position.
     * Thread-safe (map est immuable).
     */
    public int getMapValue(int x, int y) {
        if (!isValid(x, y)) return -1;
        return map[y][x];
    }
    
    // ==================== ACCÈS AU GRADIENT ====================
    
    /**
     * Lock en lecture pour un gradient (plusieurs threads peuvent lire).
     */
    public void lockGradientRead(int team) {
        gradientLocks[team].readLock().lock();
    }
    
    /**
     * Unlock en lecture pour un gradient.
     */
    public void unlockGradientRead(int team) {
        gradientLocks[team].readLock().unlock();
    }
    
    /**
     * Lock en écriture pour un gradient (exclusif).
     */
    public void lockGradientWrite(int team) {
        gradientLocks[team].writeLock().lock();
    }
    
    /**
     * Unlock en écriture pour un gradient.
     */
    public void unlockGradientWrite(int team) {
        gradientLocks[team].writeLock().unlock();
    }
    
    /**
     * Obtient la valeur du gradient à une position (NON THREAD-SAFE).
     * Le caller DOIT avoir le lock de lecture/écriture.
     */
    public int getGradient(int team, int x, int y) {
        return gradient[team][y * GameConfig.MAP_WIDTH + x];
    }
    
    /**
     * Définit la valeur du gradient à une position (NON THREAD-SAFE).
     * Le caller DOIT avoir le lock d'écriture.
     */
    public void setGradient(int team, int x, int y, int value) {
        gradient[team][y * GameConfig.MAP_WIDTH + x] = value;
    }
    
    /**
     * Obtient une référence au gradient (NON THREAD-SAFE).
     * Le caller DOIT gérer la synchronisation.
     */
    public int[] getGradientArray(int team) {
        return gradient[team];
    }
    
    /**
     * Crée une copie thread-safe du gradient d'une équipe.
     * Utile pour les threads qui veulent travailler sur une snapshot.
     */
    public int[] getGradientCopy(int team) {
        lockGradientRead(team);
        try {
            return gradient[team].clone();
        } finally {
            unlockGradientRead(team);
        }
    }
    
    // ==================== ACCÈS AUX FIGHTERS ====================
    
    /**
     * Lock en lecture pour les fighters.
     */
    public void lockFightersRead() {
        fightersLock.readLock().lock();
    }
    
    /**
     * Unlock en lecture pour les fighters.
     */
    public void unlockFightersRead() {
        fightersLock.readLock().unlock();
    }
    
    /**
     * Lock en écriture pour les fighters.
     */
    public void lockFightersWrite() {
        fightersLock.writeLock().lock();
    }
    
    /**
     * Unlock en écriture pour les fighters.
     */
    public void unlockFightersWrite() {
        fightersLock.writeLock().unlock();
    }
    
    /**
     * Obtient la liste des fighters (NON THREAD-SAFE).
     * Le caller DOIT avoir le lock approprié.
     */
    public ArrayList<Fighter> getFighters() {
        return fighters;
    }
    
    /**
     * Crée une copie thread-safe de la liste des fighters.
     */
    public List<Fighter> getFightersCopy() {
        lockFightersRead();
        try {
            return new ArrayList<>(fighters);
        } finally {
            unlockFightersRead();
        }
    }
    
    /**
     * Obtient une vue immuable de la liste des fighters (thread-safe pour lecture).
     */
    public List<Fighter> getFightersUnmodifiable() {
        lockFightersRead();
        try {
            return Collections.unmodifiableList(new ArrayList<>(fighters));
        } finally {
            unlockFightersRead();
        }
    }
    
    // ==================== ACCÈS AUX CURSEURS ====================
    
    /**
     * Lock en lecture pour les curseurs.
     */
    public void lockCursorsRead() {
        cursorsLock.readLock().lock();
    }
    
    /**
     * Unlock en lecture pour les curseurs.
     */
    public void unlockCursorsRead() {
        cursorsLock.readLock().unlock();
    }
    
    /**
     * Lock en écriture pour les curseurs.
     */
    public void lockCursorsWrite() {
        cursorsLock.writeLock().lock();
    }
    
    /**
     * Unlock en écriture pour les curseurs.
     */
    public void unlockCursorsWrite() {
        cursorsLock.writeLock().unlock();
    }
    
    /**
     * Obtient le tableau de curseurs (NON THREAD-SAFE).
     * Le caller DOIT avoir le lock approprié.
     */
    public Cursor[] getCursors() {
        return cursors;
    }
    
    /**
     * Obtient la position d'un curseur (thread-safe).
     */
    public Point getCursorPosition(int team) {
        lockCursorsRead();
        try {
            if (cursors[team] == null) {
                return new Point(0, 0);
            }
            return new Point(cursors[team].x, cursors[team].y);
        } finally {
            unlockCursorsRead();
        }
    }
    
    /**
     * Définit la position d'un curseur (thread-safe).
     */
    public void setCursorPosition(int team, int x, int y) {
        lockCursorsWrite();
        try {
            if (cursors[team] != null) {
                cursors[team].x = x;
                cursors[team].y = y;
            }
        } finally {
            unlockCursorsWrite();
        }
    }
    
    // ==================== ACCÈS AUX COMPTEURS ====================
    
    /**
     * Obtient le nombre de fighters d'une équipe (thread-safe).
     */
    public int getFighterCount(int team) {
        countersLock.readLock().lock();
        try {
            return teamFighterCount[team];
        } finally {
            countersLock.readLock().unlock();
        }
    }
    
    /**
     * Définit le nombre de fighters d'une équipe (thread-safe).
     */
    public void setFighterCount(int team, int count) {
        countersLock.writeLock().lock();
        try {
            teamFighterCount[team] = count;
        } finally {
            countersLock.writeLock().unlock();
        }
    }
    
    /**
     * Incrémente le compteur d'une équipe (thread-safe, atomique).
     */
    public void incrementFighterCount(int team) {
        countersLock.writeLock().lock();
        try {
            teamFighterCount[team]++;
        } finally {
            countersLock.writeLock().unlock();
        }
    }
    
    /**
     * Décrémente le compteur d'une équipe (thread-safe, atomique).
     */
    public void decrementFighterCount(int team) {
        countersLock.writeLock().lock();
        try {
            teamFighterCount[team]--;
        } finally {
            countersLock.writeLock().unlock();
        }
    }
    
    /**
     * Obtient une copie des compteurs (thread-safe).
     */
    public int[] getFighterCountsCopy() {
        countersLock.readLock().lock();
        try {
            return teamFighterCount.clone();
        } finally {
            countersLock.readLock().unlock();
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Obtient le nombre d'équipes actives.
     */
    public int getActiveTeams() {
        return activeTeams;
    }
    
    /**
     * Obtient la largeur de la carte.
     */
    public int getMapWidth() {
        return GameConfig.MAP_WIDTH;
    }
    
    /**
     * Obtient la hauteur de la carte.
     */
    public int getMapHeight() {
        return GameConfig.MAP_HEIGHT;
    }
    
    /**
     * Affiche des statistiques sur l'état (debug).
     */
    @Override
    public String toString() {
        countersLock.readLock().lock();
        try {
            return String.format("GameState[teams=%d, fighters=[%d, %d], total=%d]",
                activeTeams,
                teamFighterCount[0],
                activeTeams > 1 ? teamFighterCount[1] : 0,
                teamFighterCount[0] + (activeTeams > 1 ? teamFighterCount[1] : 0)
            );
        } finally {
            countersLock.readLock().unlock();
        }
    }
}

