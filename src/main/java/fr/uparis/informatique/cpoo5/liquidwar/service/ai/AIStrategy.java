package fr.uparis.informatique.cpoo5.liquidwar.service.ai;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;

/**
 * IA Agressive : Poursuit activement les ennemis.
 * 
 * <p>Cette IA utilise le gradient ennemi pour trouver le chemin le plus court
 * vers les particules adverses. C'est le comportement du code C original.
 * 
 * <h2>Stratégie</h2>
 * <ol>
 *   <li>Évaluer toutes les directions possibles (12 directions)</li>
 *   <li>Pour chaque direction, consulter le gradient de l'équipe ennemie</li>
 *   <li>Choisir la direction avec le gradient le plus faible (= plus proche de l'ennemi)</li>
 *   <li>Se déplacer dans cette direction</li>
 * </ol>
 * 
 * <h2>Avantages</h2>
 * <ul>
 *   <li>✅ Trouve automatiquement le chemin le plus court</li>
 *   <li>✅ Évite automatiquement les murs (gradient infini)</li>
 *   <li>✅ Comportement prédictible et efficace</li>
 *   <li>✅ Conforme au code C original</li>
 * </ul>
 * 
 * <h2>Inconvénients</h2>
 * <ul>
 *   <li>⚠️ Peut se faire piéger dans des coins</li>
 *   <li>⚠️ Ne protège pas les alliés faibles</li>
 * </ul>
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 * @see AIStrategy
 */
public class AggressiveAI implements AIStrategy {
    
    // Tables de direction (12 directions comme dans le code C)
    private static final int[][] DIR_MOVE_X = {
        { 0, 1, 1, 1, 1, 0, 0, -1, -1, -1, -1, 0 }
    };
    private static final int[][] DIR_MOVE_Y = {
        { -1, -1, 0, 0, 1, 1, 1, 1, 0, 0, -1, -1 }
    };
    
    @Override
    public Point calculateNextMove(GameState state, int team) {
        // Position actuelle du curseur
        Point cursor = state.getCursorPosition(team);
        
        // Équipe ennemie (0 → 1, 1 → 0)
        int enemyTeam = (team == 0) ? 1 : 0;
        
        // Chercher la meilleure direction selon le gradient ennemi
        int bestDir = -1;
        int bestGrad = Integer.MAX_VALUE;
        
        for (int dir = 0; dir < 12; dir++) {
            int nx = cursor.x + DIR_MOVE_X[0][dir];
            int ny = cursor.y + DIR_MOVE_Y[0][dir];
            
            // Vérifier si la position est valide
            if (state.isValidPosition(nx, ny) && !state.isWall(nx, ny)) {
                // Consulter le gradient de l'équipe ennemie
                int grad = state.getGradient(enemyTeam, nx, ny);
                
                // Garder la direction avec le gradient le plus faible
                if (grad < bestGrad) {
                    bestGrad = grad;
                    bestDir = dir;
                }
            }
        }
        
        // Si une direction a été trouvée, se déplacer
        if (bestDir >= 0) {
            int newX = cursor.x + DIR_MOVE_X[0][bestDir];
            int newY = cursor.y + DIR_MOVE_Y[0][bestDir];
            
            // Vérifier les limites
            newX = Math.max(GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE,
                   Math.min(state.getMapWidth() - GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE, newX));
            newY = Math.max(GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE,
                   Math.min(state.getMapHeight() - GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE, newY));
            
            return new Point(newX, newY);
        }
        
        // Pas de direction trouvée → rester sur place
        return cursor;
    }
    
    @Override
    public String getName() {
        return "Agressive (Code C)";
    }
    
    @Override
    public String getDescription() {
        return "Poursuit activement les ennemis en suivant le gradient adverse";
    }
}

