package fr.uparis.informatique.cpoo5.liquidwar.model.sealed;

/**
 * Record représentant un obstacle sur la carte.
 * 
 * <p><b>Pattern Sealed Type (Java 17+)</b> : Implémentation finale d'une interface scellée.
 * <p><b>Pattern Record (Java 14+)</b> : Classe de données immuable.
 * 
 * @param x Position X
 * @param y Position Y
 * @param permanent Obstacle permanent ou temporaire
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public final record ObstacleEntity(int x, int y, boolean permanent) 
    implements GameEntity {
    
    @Override
    public int getX() {
        return x;
    }
    
    @Override
    public int getY() {
        return y;
    }
    
    @Override
    public EntityType getType() {
        return EntityType.OBSTACLE;
    }
}
