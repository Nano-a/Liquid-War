package fr.uparis.informatique.cpoo5.liquidwar.model.sealed;

/**
 * Record représentant une particule combattante.
 * 
 * <p><b>Pattern Sealed Type (Java 17+)</b> : Implémentation finale d'une interface scellée.
 * <p><b>Pattern Record (Java 14+)</b> : Classe de données immuable.
 * 
 * @param x Position X
 * @param y Position Y
 * @param team Équipe (0 ou 1)
 * @param health Points de vie
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public final record FighterEntity(int x, int y, int team, int health) 
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
        return EntityType.FIGHTER;
    }
    
    /**
     * Vérifie si le combattant est vivant.
     * 
     * @return true si health > 0
     */
    public boolean isAlive() {
        return health > 0;
    }
}
