package fr.uparis.informatique.cpoo5.liquidwar.model.entities;

/**
 * Représente le curseur d'une équipe dans Liquid War.
 * Les combattants se dirigent vers le curseur de leur équipe.
 * 
 * IMPORTANT: Code extrait tel quel de LiquidWarGame.java (lignes 972-976)
 * pour améliorer la structure sans changer le comportement.
 */
public class Cursor {
    public int x, y;      // Gardé public pour compatibilité
    public int team;
    public int active;    // 1 = actif, 0 = inactif
}

