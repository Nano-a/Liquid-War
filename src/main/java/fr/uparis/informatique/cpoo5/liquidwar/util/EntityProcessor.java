package fr.uparis.informatique.cpoo5.liquidwar.util;

import fr.uparis.informatique.cpoo5.liquidwar.model.sealed.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Classe utilitaire démontrant l'utilisation de :
 * - Pattern Matching avec instanceof (Java 16+)
 * - Sealed Types (Java 17+)
 * - Streams API (Java 8+)
 * - Optional (Java 8+)
 * 
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public class EntityProcessor {
    
    /**
     * Traite une entité avec pattern matching.
     * 
     * <p><b>Pattern Matching avec instanceof (Java 16+)</b> :
     * Permet de tester le type ET d'extraire la variable en une seule expression.
     * 
     * @param entity Entité à traiter
     * @return Description de l'entité
     */
    public static String processEntity(GameEntity entity) {
        // Pattern Matching : instanceof + extraction de variable
        if (entity instanceof FighterEntity fighter) {
            return String.format("Fighter équipe %d à (%d,%d) avec %d PV",
                fighter.team(), fighter.x(), fighter.y(), fighter.health());
        } else if (entity instanceof CursorEntity cursor) {
            return String.format("Cursor équipe %d à (%d,%d) %s",
                cursor.team(), cursor.x(), cursor.y(),
                cursor.active() ? "actif" : "inactif");
        } else if (entity instanceof ObstacleEntity obstacle) {
            return String.format("Obstacle à (%d,%d) %s",
                obstacle.x(), obstacle.y(),
                obstacle.permanent() ? "permanent" : "temporaire");
        }
        // Grâce au sealed type, le compilateur sait que tous les cas sont couverts
        throw new IllegalStateException("Type d'entité inconnu");
    }
    
    /**
     * Filtre les entités vivantes avec Streams API.
     * 
     * <p><b>Streams API (Java 8+)</b> : Pipeline de traitement fonctionnel.
     * 
     * @param entities Liste d'entités
     * @return Liste des combattants vivants
     */
    public static List<FighterEntity> getAliveFighters(List<GameEntity> entities) {
        return entities.stream()
            .filter(e -> e instanceof FighterEntity)  // Opération intermédiaire
            .map(e -> (FighterEntity) e)              // Opération intermédiaire
            .filter(FighterEntity::isAlive)           // Référence de méthode
            .collect(Collectors.toList());            // Opération terminale
    }
    
    /**
     * Trouve un combattant par position avec Optional.
     * 
     * <p><b>Optional (Java 8+)</b> : Gestion de l'absence de valeur.
     * 
     * @param entities Liste d'entités
     * @param x Position X
     * @param y Position Y
     * @return Optional contenant le combattant ou vide
     */
    public static Optional<FighterEntity> findFighterAt(
            List<GameEntity> entities, int x, int y) {
        return entities.stream()
            .filter(e -> e instanceof FighterEntity)
            .map(e -> (FighterEntity) e)
            .filter(f -> f.x() == x && f.y() == y)
            .findFirst();  // Retourne Optional<FighterEntity>
    }
    
    /**
     * Compte les entités par type avec Streams API.
     * 
     * @param entities Liste d'entités
     * @return Nombre de combattants
     */
    public static long countFighters(List<GameEntity> entities) {
        return entities.stream()
            .filter(e -> e.getType() == GameEntity.EntityType.FIGHTER)
            .count();  // Opération terminale
    }
    
    /**
     * Calcule la santé moyenne des combattants avec Streams API.
     * 
     * @param entities Liste d'entités
     * @return Santé moyenne ou 0 si aucun combattant
     */
    public static double averageFighterHealth(List<GameEntity> entities) {
        return entities.stream()
            .filter(e -> e instanceof FighterEntity)
            .map(e -> (FighterEntity) e)
            .mapToInt(FighterEntity::health)  // Référence de méthode
            .average()
            .orElse(0.0);  // Utilisation d'Optional
    }
    
    /**
     * Trouve l'entité la plus proche d'une position avec Streams API.
     * 
     * @param entities Liste d'entités
     * @param x Position X cible
     * @param y Position Y cible
     * @return Optional de l'entité la plus proche
     */
    public static Optional<GameEntity> findClosest(
            List<GameEntity> entities, int x, int y) {
        return entities.stream()
            .min((e1, e2) -> {
                int dist1 = Math.abs(e1.getX() - x) + Math.abs(e1.getY() - y);
                int dist2 = Math.abs(e2.getX() - x) + Math.abs(e2.getY() - y);
                return Integer.compare(dist1, dist2);
            });
    }
}
