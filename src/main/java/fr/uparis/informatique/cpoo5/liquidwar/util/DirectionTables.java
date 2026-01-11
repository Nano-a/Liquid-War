package fr.uparis.informatique.cpoo5.liquidwar.util;

/**
 * Tables de directions pour le système de mouvement "slime-like" de Liquid War.
 * 
 * Ces tables sont extraites telles quelles de LiquidWarGame.java pour réduire
 * la taille du fichier principal sans modifier le comportement.
 * 
 * IMPORTANT : Code extrait sans modification de la logique.
 */
public final class DirectionTables {
    
    // Empêcher l'instanciation
    private DirectionTables() {}
    
    /**
     * Tables de déplacement en X pour 12 directions (N, NE, E, SE, S, SW, W, NW).
     * [table][direction] → offset X
     */
    public static final int[][] DIR_MOVE_X = {
        { 0, 1, 1, 1, 1, 0, 0, -1, -1, -1, -1, 0 }, // Table 0
        { 0, 1, 1, 1, 1, 0, 0, -1, -1, -1, -1, 0 }  // Table 1
    };
    
    /**
     * Tables de déplacement en Y pour 12 directions (N, NE, E, SE, S, SW, W, NW).
     * [table][direction] → offset Y
     */
    public static final int[][] DIR_MOVE_Y = {
        { -1, -1, 0, 0, 1, 1, 1, 1, 0, 0, -1, -1 }, // Table 0
        { -1, -1, 0, 0, 1, 1, 1, 1, 0, 0, -1, -1 }  // Table 1
    };
}

