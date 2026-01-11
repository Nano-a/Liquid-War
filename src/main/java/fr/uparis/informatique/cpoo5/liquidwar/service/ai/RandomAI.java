package fr.uparis.informatique.cpoo5.liquidwar.service.ai;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;

/**
 * IA Aléatoire : Se déplace de manière erratique.
 * 
 * <p>Cette IA choisit une direction aléatoire à chaque frame, créant un
 * mouvement imprévisible et chaotique. Utile principalement pour les tests.
 * 
 * <h2>Stratégie</h2>
 * <ol>
 *   <li>Générer un déplacement aléatoire en X</li>
 *   <li>Générer un déplacement aléatoire en Y</li>
 *   <li>Appliquer le déplacement au curseur</li>
 *   <li>Vérifier les limites de la carte</li>
 * </ol>
 * 
 * <h2>Avantages</h2>
 * <ul>
 *   <li>✅ Imprévisible (difficile à anticiper)</li>
 *   <li>✅ Explore toute la carte</li>
 *   <li>✅ Utile pour tester la robustesse du jeu</li>
 *   <li>✅ Amusant à regarder !</li>
 * </ul>
 * 
 * <h2>Inconvénients</h2>
 * <ul>
 *   <li>⚠️ Inefficace (ne cherche pas à gagner)</li>
 *   <li>⚠️ Gaspille des mouvements</li>
 *   <li>⚠️ Perd toujours contre une IA intelligente</li>
 * </ul>
 * 
 * <h2>Usage recommandé</h2>
 * <p>Principalement pour les tests unitaires et le débogage. Permet de vérifier
 * que le jeu gère correctement tous les types de mouvements.
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 * @see AIStrategy
 */
public class RandomAI implements AIStrategy {
    
    private static final int MAX_MOVE = 5; // Déplacement maximum par frame
    
    @Override
    public Point calculateNextMove(GameState state, int team) {
        // Position actuelle
        Point cursor = state.getCursorPosition(team);
        
        // Génération aléatoire (ThreadLocalRandom est thread-safe)
        int dx = ThreadLocalRandom.current().nextInt(-MAX_MOVE, MAX_MOVE + 1);
        int dy = ThreadLocalRandom.current().nextInt(-MAX_MOVE, MAX_MOVE + 1);
        
        // Nouvelle position
        int newX = cursor.x + dx;
        int newY = cursor.y + dy;
        
        // Vérifier les limites
        newX = Math.max(GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE,
               Math.min(state.getMapWidth() - GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE, newX));
        newY = Math.max(GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE,
               Math.min(state.getMapHeight() - GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE, newY));
        
        // Vérifier si la position est valide (pas un mur)
        if (!state.isWall(newX, newY)) {
            return new Point(newX, newY);
        }
        
        // Si c'est un mur, rester sur place
        return cursor;
    }
    
    @Override
    public String getName() {
        return "Aléatoire";
    }
    
    @Override
    public String getDescription() {
        return "Mouvement erratique et imprévisible (utile pour les tests)";
    }
}

