package fr.uparis.informatique.cpoo5.liquidwar.model.sealed;

/**
 * Record représentant un curseur de joueur.
 * 
 * <p><b>Pattern Sealed Type (Java 17+)</b> : Implémentation finale d'une interface scellée.
 * <p><b>Pattern Record (Java 14+)</b> : Classe de données immuable.
 * 
 * @param x Position X
 * @param y Position Y
 * @param team Équipe propriétaire
 * @param active État actif
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public final record CursorEntity(int x, int y, int team, boolean active) 
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
        return EntityType.CURSOR;
    }
}
