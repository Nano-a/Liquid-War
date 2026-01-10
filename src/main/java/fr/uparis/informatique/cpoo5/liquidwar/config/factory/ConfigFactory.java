package fr.uparis.informatique.cpoo5.liquidwar.config.factory;

/**
 * Factory Pattern pour créer différentes configurations de jeu.
 * 
 * Permet de créer facilement des configurations prédéfinies
 * (facile, normal, difficile, custom) et de les sauvegarder/charger.
 * 
 * @author Liquid War Team
 */
public class ConfigFactory {
    
    /**
     * Crée une configuration par défaut.
     */
    public static GameConfiguration createDefault() {
        return new GameConfiguration(
            "Default",
            8,    // LOGIC_TIMER_INTERVAL_MS
            200,  // TARGET_FPS
            10,   // CURSOR_PROXIMITY_RADIUS
            2000, // INITIAL_FIGHTERS_PER_TEAM
            2     // CURSOR_MOVE_SPEED
        );
    }
    
    /**
     * Crée une configuration "Facile" pour débutants.
     */
    public static GameConfiguration createEasy() {
        return new GameConfiguration(
            "Easy",
            10,   // Plus lent
            120,  // FPS réduit
            50,   // Plus de particules proches
            1500, // Moins de particules
            3     // Curseur plus rapide
        );
    }
    
    /**
     * Crée une configuration "Normal" équilibrée.
     */
    public static GameConfiguration createNormal() {
        return new GameConfiguration(
            "Normal",
            8,
            166,
            30,
            2000,
            2
        );
    }
    
    /**
     * Crée une configuration "Difficile" pour experts.
     */
    public static GameConfiguration createHard() {
        return new GameConfiguration(
            "Hard",
            5,    // Très rapide
            200,  // FPS élevé
            20,   // Moins de particules proches
            2500, // Plus de particules
            1     // Curseur lent
        );
    }
    
    /**
     * Crée une configuration "Performance" optimisée pour FPS.
     */
    public static GameConfiguration createPerformance() {
        return new GameConfiguration(
            "Performance",
            16,   // Moins de calculs
            60,   // FPS standard
            40,
            1000, // Moins de particules
            2
        );
    }
    
    /**
     * Crée une configuration "Qualité" avec maximum de détails.
     */
    public static GameConfiguration createQuality() {
        return new GameConfiguration(
            "Quality",
            4,    // Calculs fréquents
            200,  // FPS maximum
            15,
            3000, // Maximum de particules
            2
        );
    }
    
    /**
     * Crée une configuration custom.
     * 
     * @param name Nom de la configuration
     * @param logicInterval Intervalle logique en ms
     * @param targetFps FPS cible
     * @param proximityRadius Rayon de proximité curseur
     * @param fightersPerTeam Nombre de fighters par équipe
     * @param cursorSpeed Vitesse du curseur
     */
    public static GameConfiguration createCustom(String name, int logicInterval, 
                                                 int targetFps, int proximityRadius,
                                                 int fightersPerTeam, int cursorSpeed) {
        return new GameConfiguration(name, logicInterval, targetFps, 
                                    proximityRadius, fightersPerTeam, cursorSpeed);
    }
    
    /**
     * Charge une configuration depuis un nom prédéfini.
     * 
     * @param configName Nom de la configuration
     * @return Configuration correspondante, ou Default si inconnu
     */
    public static GameConfiguration loadConfiguration(String configName) {
        return switch (configName.toLowerCase()) {
            case "easy", "facile" -> createEasy();
            case "normal" -> createNormal();
            case "hard", "difficile" -> createHard();
            case "performance" -> createPerformance();
            case "quality", "qualité" -> createQuality();
            default -> createDefault();
        };
    }
}