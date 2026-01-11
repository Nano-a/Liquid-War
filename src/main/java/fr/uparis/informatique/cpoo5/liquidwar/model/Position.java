package fr.uparis.informatique.cpoo5.liquidwar.model;

/**
 * Record représentant une position 2D immuable.
 * 
 * <p><b>Pattern Record (Java 14+)</b> : Classe de données immuable.
 * Le compilateur génère automatiquement :
 * <ul>
 *   <li>Les champs private final</li>
 *   <li>Le constructeur</li>
 *   <li>Les accesseurs x() et y()</li>
 *   <li>equals(), hashCode() et toString()</li>
 * </ul>
 * 
 * @param x Position X
 * @param y Position Y
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public record Position(int x, int y) {
    
    /**
     * Constructeur compact pour validation.
     * Vérifie que les coordonnées sont positives.
     */
    public Position {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Les coordonnées doivent être positives");
        }
    }
    
    /**
     * Calcule la distance de Manhattan vers une autre position.
     * 
     * @param other L'autre position
     * @return Distance de Manhattan
     */
    public int manhattanDistance(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    /**
     * Calcule la distance euclidienne vers une autre position.
     * 
     * @param other L'autre position
     * @return Distance euclidienne
     */
    public double euclideanDistance(Position other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Crée une nouvelle position décalée.
     * 
     * @param dx Décalage en X
     * @param dy Décalage en Y
     * @return Nouvelle position
     */
    public Position offset(int dx, int dy) {
        return new Position(this.x + dx, this.y + dy);
    }
}
