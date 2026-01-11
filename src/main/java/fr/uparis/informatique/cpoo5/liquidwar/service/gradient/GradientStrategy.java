package fr.uparis.informatique.cpoo5.liquidwar.service.gradient;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;

/**
 * Strategy Pattern pour les algorithmes de calcul de gradient.
 * 
 * Permet de choisir différents algorithmes de pathfinding :
 * - BFS (Breadth-First Search) : Simple et rapide
 * - Dijkstra : Plus précis avec coûts variables
 * - A* : Optimisé avec heuristique
 * 
 * @author Liquid War Team
 */
public interface GradientStrategy {
    
    /**
     * Calcule le gradient pour une équipe.
     * 
     * @param map Carte du jeu (-1 = obstacle, autre = libre)
     * @param gradient Tableau de gradient à mettre à jour
     * @param cursor Curseur de l'équipe
     * @param cursorVal Valeur initiale au curseur
     * @param globalClock Horloge globale pour rotation
     */
    void calculateGradient(int[][] map, int[] gradient, Cursor cursor, 
                          int cursorVal, int globalClock);
    
    /**
     * Propage le gradient de manière incrémentale (une passe).
     * 
     * @param map Carte du jeu
     * @param gradient Tableau de gradient
     * @param globalClock Horloge globale
     */
    void spreadGradient(int[][] map, int[] gradient, int globalClock);
    
    /**
     * Retourne le nom de la stratégie.
     */
    String getName();
    
    /**
     * Retourne une description de l'algorithme.
     */
    String getDescription();
}

