package fr.uparis.informatique.cpoo5.liquidwar.config;

/**
 * Configuration globale du jeu Liquid War.
 * Centralise toutes les constantes extraites de LiquidWarGame.java et GameArea
 * pour faciliter les modifications futures.
 * 
 * IMPORTANT: Valeurs extraites telles quelles pour ne pas changer le
 * comportement.
 */
public final class GameConfig {

    // Empêcher l'instanciation
    private GameConfig() {
    }

    // ===== CONFIGURATION DE LA CARTE =====
    // Extrait de GameArea (ligne 95-96)
    public static final int MAP_WIDTH = 281;
    public static final int MAP_HEIGHT = 240;

    // ===== CONFIGURATION DES COMBATTANTS =====
    // Extrait de GameArea (lignes 97-101)
    public static final int MAX_FIGHTERS_PER_TEAM = 2000;
    public static final int FIGHTER_SPAWN_RATE = 5;
    public static final int FIGHTER_INITIAL_HEALTH = 100; // Était FIGHTER_HEALTH
    public static final int ATTACK_DAMAGE = 10;

    // Spawn initial (ligne 274-275)
    public static final int INITIAL_FIGHTERS_PER_TEAM = 2000; // Changé de 500 à 2000
    public static final int FIGHTER_SPAWN_RADIUS = 15; // ligne 364
    public static final int FIGHTER_SPAWN_DISTANCE = 8; // ligne 450

    // Total fixe de particules (comme dans le code C)
    public static final int TOTAL_FIGHTERS = 4000; // 2000 par équipe, total constant

    // ===== CONFIGURATION DU PATHFINDING =====
    // Extrait de GameArea (lignes 143-144)
    public static final int AREA_START_GRADIENT = 2000000;
    public static final int CURSOR_START_GRADIENT = 1000000;
    // MOUVEMENT EN VAGUES : Rayon moyen pour équilibre entre direct et gradient
    // Crée l'effet de vagues qui se propagent du curseur vers l'extérieur
    public static final int CURSOR_PROXIMITY_RADIUS = 40; // Équilibre pour vagues naturelles

    // ===== CONFIGURATION DU MOUVEMENT =====
    // Extrait de GameArea (lignes 132-134)
    public static final int NB_TRY_MOVE = 5;
    public static final int NB_LOCAL_DIRS = 16;
    public static final int NB_DIRECTIONS = 12;

    // ===== CONFIGURATION DES CURSEURS =====
    public static final int CURSOR_MOVE_SPEED = 2; // Réduit de 5 à 2 pour clavier plus lent
    public static final int CURSOR_MIN_DISTANCE_FROM_EDGE = 10; // lignes 440, 908

    // ===== CONFIGURATION DES ÉQUIPES =====
    public static final int MAX_TEAMS = 6;
    public static final int DEFAULT_TEAM_COUNT = 2; // ligne 242

    // ===== CONFIGURATION DES PERFORMANCES =====
    // MOUVEMENT MASSIF : Timer plus rapide pour réactivité maximale
    // Toutes les particules bougent ensemble = besoin de vitesse
    public static final int LOGIC_TICKS_PER_TIMER_EVENT = 1; // 1 seul tick par appel (sécurisé)
    public static final int LOGIC_TIMER_INTERVAL_MS = 5; // Appelé tous les 5ms = ~200 fois/sec (rapide)
    public static final int DISPLAY_TIMER_INTERVAL_MS = 16; // ~60 FPS pour fluidité (équilibre performance/qualité)
    public static final int TARGET_FPS = 60;

    // ===== PROBABILITÉS =====
    public static final double SPAWN_PROBABILITY = 0.2; // ligne 408
    public static final double AI_CHASE_PROBABILITY = 0.7; // ligne 430

    // ===== COMPORTEMENT LIQUIDE =====
    // Paramètres pour rendre le mouvement plus organique et fluide
    public static final double MOVEMENT_RANDOMNESS = 0.15; // 15% de chance de dévier
    public static final double DIRECTION_VARIATION = 0.25; // 25% de variation angulaire
    public static final boolean ENABLE_ORGANIC_MOVEMENT = true; // Activer mouvement organique

    // ===== PROPAGATION DU GRADIENT =====
    // Comme dans la v9 : pas de variable GRADIENT_PROPAGATION_PASSES
    // La propagation se fait directement dans le code
}
