package fr.uparis.informatique.cpoo5.liquidwar.config.factory;

/**
 * Classe représentant une configuration complète du jeu.
 * 
 * Immutable pour thread-safety.
 * 
 * @author Liquid War Team
 */
public final class GameConfiguration {
    
    private final String name;
    private final int logicTimerIntervalMs;
    private final int targetFps;
    private final int cursorProximityRadius;
    private final int initialFightersPerTeam;
    private final int cursorMoveSpeed;
    
    /**
     * Constructeur de configuration.
     * 
     * @param name Nom de la configuration
     * @param logicTimerIntervalMs Intervalle timer logique (ms)
     * @param targetFps FPS cible
     * @param cursorProximityRadius Rayon de proximité curseur
     * @param initialFightersPerTeam Nombre initial de fighters par équipe
     * @param cursorMoveSpeed Vitesse de déplacement du curseur
     */
    public GameConfiguration(String name, int logicTimerIntervalMs, int targetFps,
                            int cursorProximityRadius, int initialFightersPerTeam,
                            int cursorMoveSpeed) {
        this.name = name;
        this.logicTimerIntervalMs = logicTimerIntervalMs;
        this.targetFps = targetFps;
        this.cursorProximityRadius = cursorProximityRadius;
        this.initialFightersPerTeam = initialFightersPerTeam;
        this.cursorMoveSpeed = cursorMoveSpeed;
    }
    
    // Getters
    
    public String getName() {
        return name;
    }
    
    public int getLogicTimerIntervalMs() {
        return logicTimerIntervalMs;
    }
    
    public int getTargetFps() {
        return targetFps;
    }
    
    public int getCursorProximityRadius() {
        return cursorProximityRadius;
    }
    
    public int getInitialFightersPerTeam() {
        return initialFightersPerTeam;
    }
    
    public int getCursorMoveSpeed() {
        return cursorMoveSpeed;
    }
    
    @Override
    public String toString() {
        return String.format("GameConfiguration[%s: %dms, %dfps, radius=%d, fighters=%d, speed=%d]",
                           name, logicTimerIntervalMs, targetFps, cursorProximityRadius,
                           initialFightersPerTeam, cursorMoveSpeed);
    }
}

