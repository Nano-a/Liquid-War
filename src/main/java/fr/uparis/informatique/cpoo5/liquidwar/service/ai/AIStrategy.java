package fr.uparis.informatique.cpoo5.liquidwar.service.ai;

import java.awt.Point;

/**
 * Stratégie d'Intelligence Artificielle pour contrôler un curseur.
 * 
 * <p><b>Pattern Strategy</b> : Permet de changer dynamiquement le comportement de l'IA
 * sans modifier le code du jeu. Chaque implémentation définit une stratégie différente.
 * 
 * <h2>Exemples d'implémentations</h2>
 * <ul>
 *   <li>{@link AggressiveAI} : Poursuit activement les ennemis (comportement du code C)</li>
 *   <li>{@link DefensiveAI} : Reste près du centre de masse des alliés</li>
 *   <li>{@link RandomAI} : Mouvement aléatoire (utile pour les tests)</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // Dans GameCanvas
 * private AIStrategy aiStrategy = new AggressiveAI(); // Par défaut
 * 
 * // Changer de stratégie dynamiquement
 * gameCanvas.setAIStrategy(new DefensiveAI());
 * 
 * // Dans updateAICursor()
 * Point nextMove = aiStrategy.calculateNextMove(gameState, team);
 * cursors[team].x = nextMove.x;
 * cursors[team].y = nextMove.y;
 * }</pre>
 * 
 * <h2>Avantages du Pattern</h2>
 * <ul>
 *   <li>✅ Facile d'ajouter de nouvelles IA sans modifier le code existant (Open/Closed)</li>
 *   <li>✅ Testable isolément (chaque stratégie peut être testée séparément)</li>
 *   <li>✅ Changeable à runtime (via menu Options)</li>
 *   <li>✅ Respect du principe de substitution de Liskov</li>
 * </ul>
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 * @see AggressiveAI
 * @see DefensiveAI
 * @see RandomAI
 */
public interface AIStrategy {
    
    /**
     * Calcule la prochaine position du curseur pour une équipe donnée.
     * 
     * <p>Cette méthode est appelée à chaque frame pour déterminer où l'IA
     * doit déplacer son curseur.
     * 
     * <p><b>Contraintes</b> :
     * <ul>
     *   <li>La position retournée DOIT être valide (dans les limites de la carte)</li>
     *   <li>La méthode NE DOIT PAS modifier le GameState (lecture seule)</li>
     *   <li>La méthode DOIT être rapide (appelée 120 fois/seconde)</li>
     * </ul>
     * 
     * @param gameState État actuel du jeu (immuable, thread-safe)
     * @param team Numéro de l'équipe contrôlée par l'IA (0 ou 1)
     * @return Point représentant la nouvelle position du curseur (x, y)
     * 
     * @throws IllegalArgumentException si team est invalide
     * @throws NullPointerException si gameState est null
     */
    Point calculateNextMove(GameState gameState, int team);
    
    /**
     * Retourne le nom de la stratégie (pour affichage dans le menu).
     * 
     * <p>Utilisé dans le menu Options pour permettre à l'utilisateur
     * de sélectionner le type d'IA.
     * 
     * @return Nom descriptif de la stratégie (ex: "Agressive", "Défensive", etc.)
     */
    String getName();
    
    /**
     * Retourne une description de la stratégie (optionnel).
     * 
     * <p>Utilisé pour afficher une aide dans le menu Options.
     * 
     * @return Description de la stratégie (par défaut : nom)
     */
    default String getDescription() {
        return getName();
    }
    
    /**
     * Initialise la stratégie (optionnel).
     * 
     * <p>Appelé une fois au démarrage du jeu. Permet d'initialiser
     * des variables ou de précalculer des données.
     * 
     * @param gameState État initial du jeu
     */
    default void initialize(GameState gameState) {
        // Par défaut : rien à faire
    }
    
    /**
     * Classe imbriquée pour encapsuler l'état du jeu.
     * 
     * <p>Fournit une vue immuable des données du jeu aux stratégies IA.
     * Cela empêche les stratégies de modifier accidentellement l'état du jeu.
     */
    interface GameState {
        
        /**
         * Obtient la position actuelle d'un curseur.
         */
        Point getCursorPosition(int team);
        
        /**
         * Obtient le nombre de fighters d'une équipe.
         */
        int getFighterCount(int team);
        
        /**
         * Vérifie si une position est valide (dans les limites).
         */
        boolean isValidPosition(int x, int y);
        
        /**
         * Vérifie si une position est un mur.
         */
        boolean isWall(int x, int y);
        
        /**
         * Obtient la valeur du gradient à une position.
         */
        int getGradient(int team, int x, int y);
        
        /**
         * Obtient la largeur de la carte.
         */
        int getMapWidth();
        
        /**
         * Obtient la hauteur de la carte.
         */
        int getMapHeight();
    }
}

