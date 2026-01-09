package fr.uparis.informatique.cpoo5.liquidwar.service.ai;

import java.awt.Point;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

/**
 * IA Défensive : Patrouille autour du centre de la carte.
 * 
 * <p>
 * Cette IA privilégie la protection du territoire plutôt que l'attaque.
 * Elle se positionne au centre de la carte puis patrouille autour de cette
 * position, créant une zone de défense solide.
 * 
 * <h2>Stratégie</h2>
 * <ol>
 * <li>Se déplacer vers le centre de la carte</li>
 * <li>Une fois proche du centre, patrouiller autour (mouvement circulaire)</li>
 * <li>Éviter les mouvements brusques</li>
 * </ol>
 * 
 * <h2>Avantages</h2>
 * <ul>
 * <li>✅ Protège efficacement un territoire</li>
 * <li>✅ Difficile à percer (concentration de particules)</li>
 * <li>✅ Mouvement dynamique (ne reste pas immobile)</li>
 * </ul>
 * 
 * <h2>Inconvénients</h2>
 * <ul>
 * <li>⚠️ Ne prend pas l'initiative</li>
 * <li>⚠️ Perd du terrain face à une IA agressive</li>
 * <li>⚠️ Peut être contournée</li>
 * </ul>
 * 
 * <h2>Usage recommandé</h2>
 * <p>
 * Utile pour protéger une base ou gagner du temps pendant que les particules
 * se regroupent.
 * 
 * @author Votre Nom
 * @version 1.1
 * @since 2025-12-01
 * @see AIStrategy
 */
public class DefensiveAI implements AIStrategy {

    private static final int MOVEMENT_SPEED = 2; // Vitesse de mouvement
    private static final int PATROL_RADIUS = 30; // Rayon de patrouille autour du centre
    private static final int CENTER_PROXIMITY = 10; // Distance pour considérer qu'on est "proche" du centre

    // Compteur interne pour le mouvement circulaire (s'incrémente à chaque appel)
    private int patrolAngle = 0;

    @Override
    public Point calculateNextMove(GameState state, int team) {
        // Position actuelle du curseur
        Point cursor = state.getCursorPosition(team);

        // Calculer le centre de la carte
        int centerX = state.getMapWidth() / 2;
        int centerY = state.getMapHeight() / 2;

        // Distance du curseur au centre
        double distanceToCenter = Math.sqrt(
                Math.pow(cursor.x - centerX, 2) + Math.pow(cursor.y - centerY, 2));

        int newX, newY;

        // Si on est loin du centre, se rapprocher
        if (distanceToCenter > CENTER_PROXIMITY) {
            // Se déplacer vers le centre
            int dx = Integer.compare(centerX, cursor.x) * MOVEMENT_SPEED;
            int dy = Integer.compare(centerY, cursor.y) * MOVEMENT_SPEED;

            newX = cursor.x + dx;
            newY = cursor.y + dy;
        } else {
            // On est proche du centre → PATROUILLER autour
            // Mouvement circulaire basé sur l'angle de patrouille
            double angle = Math.toRadians(patrolAngle);
            newX = centerX + (int) (PATROL_RADIUS * Math.cos(angle));
            newY = centerY + (int) (PATROL_RADIUS * Math.sin(angle));

            // Incrémenter l'angle pour le prochain mouvement (vitesse de rotation)
            patrolAngle = (patrolAngle + 3) % 360; // Rotation lente (3 degrés par frame)
        }

        // Vérifier les limites
        newX = Math.max(GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE,
                Math.min(state.getMapWidth() - GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE, newX));
        newY = Math.max(GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE,
                Math.min(state.getMapHeight() - GameConfig.CURSOR_MIN_DISTANCE_FROM_EDGE, newY));

        // Vérifier que la position n'est pas un mur et trouver une alternative si
        // nécessaire
        if (state.isWall(newX, newY)) {
            // Si c'est un mur, chercher une case libre proche dans plusieurs directions
            // Essayer d'abord les directions cardinales
            int[][] directions = {
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Cardinales
                    { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 }, // Diagonales
                    { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 } // Plus loin
            };

            boolean found = false;
            for (int[] dir : directions) {
                int testX = cursor.x + dir[0];
                int testY = cursor.y + dir[1];

                if (state.isValidPosition(testX, testY) && !state.isWall(testX, testY)) {
                    newX = testX;
                    newY = testY;
                    found = true;
                    break;
                }
            }

            // Si aucune direction n'a fonctionné, essayer de reculer
            if (!found) {
                // Reculer dans la direction opposée au centre
                int backDx = Integer.compare(cursor.x, centerX);
                int backDy = Integer.compare(cursor.y, centerY);
                if (backDx == 0)
                    backDx = (cursor.x < centerX) ? 1 : -1;
                if (backDy == 0)
                    backDy = (cursor.y < centerY) ? 1 : -1;

                newX = cursor.x + backDx * MOVEMENT_SPEED;
                newY = cursor.y + backDy * MOVEMENT_SPEED;

                // Vérifier que cette position est valide
                if (!state.isValidPosition(newX, newY) || state.isWall(newX, newY)) {
                    // Dernière tentative : mouvement aléatoire vers une case libre
                    for (int angle = 0; angle < 360; angle += 45) {
                        double rad = Math.toRadians(angle);
                        int testX = cursor.x + (int) (Math.cos(rad) * MOVEMENT_SPEED);
                        int testY = cursor.y + (int) (Math.sin(rad) * MOVEMENT_SPEED);

                        if (state.isValidPosition(testX, testY) && !state.isWall(testX, testY)) {
                            newX = testX;
                            newY = testY;
                            found = true;
                            break;
                        }
                    }

                    // Si vraiment rien ne fonctionne, rester sur place (mieux que de se bloquer)
                    if (!found) {
                        newX = cursor.x;
                        newY = cursor.y;
                    }
                }
            }
        }

        return new Point(newX, newY);
    }

    @Override
    public String getName() {
        return "Défensive";
    }

    @Override
    public String getDescription() {
        return "Patrouille autour du centre de la carte pour protéger le territoire";
    }
}
