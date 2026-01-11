package fr.uparis.informatique.cpoo5.liquidwar.controller;

import javax.swing.SwingUtilities;

/**
 * Point d'entrée principal avec système de menus complet.
 * 
 * Ce point d'entrée affiche :
 * 1. Menu principal (JOUER, OPTIONS, AIDE, QUITTER)
 * 2. Sélection du mode de jeu (SOLO, DUO LOCAL, RÉSEAU)
 * 3. Configuration des équipes (type, niveau IA, nombre de combattants)
 * 4. → Puis lance LiquidWarGame avec notre code organisé
 * 
 * Pour lancer avec les menus :
 * gradle run -PmainClass=fr.uparis.informatique.cpoo5.liquidwar.controller.MainWithMenu
 * 
 * Pour lancer directement le jeu (sans menus) :
 * gradle run
 */
public class MainWithMenu {
    
    /**
     * Constructeur qui lance le menu principal.
     * Utilisé quand on revient au menu depuis le jeu.
     */
    public MainWithMenu() {
        System.out.println("🔄 Retour au menu principal...");
        new MenuManager();
    }
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("     🎮 LIQUID WAR - VERSION AVEC MENUS 🎮");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("");
        System.out.println("📋 Fonctionnalités:");
        System.out.println("  ✅ Menu principal complet");
        System.out.println("  ✅ Sélection du mode de jeu");
        System.out.println("  ✅ Configuration des équipes");
        System.out.println("  ✅ Options et aide");
        System.out.println("  ✅ Intégration avec le code organisé");
        System.out.println("");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("");
        
        // Lancer le gestionnaire de menus dans l'EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            new MenuManager();
        });
    }
}

