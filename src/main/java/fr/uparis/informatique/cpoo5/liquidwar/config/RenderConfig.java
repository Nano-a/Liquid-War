package fr.uparis.informatique.cpoo5.liquidwar.config;

import java.awt.Color;

/**
 * Configuration du rendu graphique pour Liquid War.
 * Centralise les constantes de rendu extraites de LiquidWarGame.java et GameArea.
 * 
 * IMPORTANT: Valeurs extraites telles quelles pour ne pas changer le comportement.
 */
public final class RenderConfig {
    
    // Empêcher l'instanciation
    private RenderConfig() {}
    
    // ===== ZOOM ET DIMENSIONS =====
    // Extrait de LiquidWarGame (lignes 28-30)
    public static final int SCALE = 6; // Zoom x6
    public static final int GAME_WIDTH = 281 * SCALE;   // 1686
    public static final int GAME_HEIGHT = 240 * SCALE;  // 1440
    
    // ===== COULEURS DES ÉQUIPES =====
    // Extrait de GameArea (lignes 113-120)
    public static final Color[] TEAM_COLORS = {
        new Color(100, 150, 255), // Bleu - Joueur 1
        new Color(255, 100, 100), // Rouge - Joueur 2
        new Color(100, 255, 100), // Vert - Joueur 3
        new Color(255, 255, 100), // Jaune - Joueur 4
        new Color(255, 100, 255), // Magenta - Joueur 5
        new Color(100, 255, 255)  // Cyan - Joueur 6
    };
    
    // ===== COULEURS DE LA CARTE =====
    // Extrait de drawStaticBuffer (lignes 787-789)
    public static final Color BACKGROUND_COLOR_1 = new Color(0, 30, 60);
    public static final Color BACKGROUND_COLOR_2 = new Color(0, 0, 30);
    public static final Color OBSTACLE_COLOR = new Color(30, 30, 30);
    
    // Couleur de fond de GameArea (ligne 167)
    public static final Color GAME_AREA_BACKGROUND = new Color(0, 0, 30);
    
    // ===== COULEURS DE L'INTERFACE =====
    public static final Color HUD_TEXT_COLOR = Color.WHITE;
    
    // ===== CURSEUR =====
    // Extrait de paintComponent (lignes 845-849)
    public static final int CURSOR_SIZE = 3;
    public static final int CURSOR_CROSS_SIZE = 2;  // size * 2
    
    // ===== POLICE =====
    public static final String HUD_FONT_NAME = "Arial";
    public static final int HUD_FONT_SIZE_LARGE = 16;  // ligne 876
    public static final int HUD_FONT_SIZE_SMALL = 14;  // ligne 887
    
    // ===== OPTIMISATIONS =====
    // Le code désactive explicitement l'antialiasing (lignes 284-287, 853-856)
    public static final boolean ENABLE_ANTIALIASING = false;
}

