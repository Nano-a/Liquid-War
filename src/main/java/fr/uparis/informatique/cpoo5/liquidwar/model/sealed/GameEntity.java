package fr.uparis.informatique.cpoo5.liquidwar.model.sealed;

/**
 * Interface scellée représentant une entité du jeu.
 * 
 * <p><b>Pattern Sealed Type (Java 17+)</b> : Type scellé avec liste fixe de sous-types.
 * Garantit l'exhaustivité lors des tests de type (instanceof) ou switch.
 * 
 * <p>Les seuls sous-types autorisés sont :
 * <ul>
 *   <li>{@link FighterEntity} - Particule combattante</li>
 *   <li>{@link CursorEntity} - Curseur de joueur</li>
 *   <li>{@link ObstacleEntity} - Obstacle sur la carte</li>
 * </ul>
 * 
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public sealed interface GameEntity 
    permits FighterEntity, CursorEntity, ObstacleEntity {
    
    /**
     * Obtient la position de l'entité.
     * 
     * @return Position de l'entité
     */
    int getX();
    
    /**
     * Obtient la position Y de l'entité.
     * 
     * @return Position Y
     */
    int getY();
    
    /**
     * Obtient le type d'entité.
     * 
     * @return Type d'entité
     */
    EntityType getType();
    
    /**
     * Énumération des types d'entités.
     */
    enum EntityType {
        FIGHTER,
        CURSOR,
        OBSTACLE
    }
}
