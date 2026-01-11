package fr.uparis.informatique.cpoo5.liquidwar.model;

import java.util.Optional;

/**
 * Record représentant le résultat d'une partie.
 * 
 * <p><b>Pattern Record (Java 14+)</b> : Classe de données immuable.
 * Utilise également Optional pour représenter l'absence de gagnant (match nul).
 * 
 * @param winner Équipe gagnante (Optional vide si match nul)
 * @param duration Durée de la partie en secondes
 * @param finalScore Score final de l'équipe gagnante
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public record GameResult(
    Optional<Integer> winner,
    long duration,
    int finalScore
) {
    
    /**
     * Constructeur compact pour validation.
     */
    public GameResult {
        if (duration < 0) {
            throw new IllegalArgumentException("La durée ne peut pas être négative");
        }
        if (finalScore < 0) {
            throw new IllegalArgumentException("Le score ne peut pas être négatif");
        }
    }
    
    /**
     * Crée un résultat avec un gagnant.
     * 
     * @param winnerTeam Équipe gagnante
     * @param duration Durée en secondes
     * @param score Score final
     * @return Résultat de la partie
     */
    public static GameResult withWinner(int winnerTeam, long duration, int score) {
        return new GameResult(Optional.of(winnerTeam), duration, score);
    }
    
    /**
     * Crée un résultat de match nul.
     * 
     * @param duration Durée en secondes
     * @return Résultat de match nul
     */
    public static GameResult draw(long duration) {
        return new GameResult(Optional.empty(), duration, 0);
    }
    
    /**
     * Vérifie si la partie a un gagnant.
     * 
     * @return true si un gagnant existe
     */
    public boolean hasWinner() {
        return winner.isPresent();
    }
    
    /**
     * Obtient le gagnant ou lance une exception.
     * 
     * @return Équipe gagnante
     * @throws IllegalStateException si pas de gagnant
     */
    public int getWinnerOrThrow() {
        return winner.orElseThrow(() -> 
            new IllegalStateException("Pas de gagnant (match nul)"));
    }
}
