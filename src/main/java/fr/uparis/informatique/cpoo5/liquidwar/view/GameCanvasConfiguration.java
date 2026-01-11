package fr.uparis.informatique.cpoo5.liquidwar.view;

import fr.uparis.informatique.cpoo5.liquidwar.service.ai.AIStrategy;
import fr.uparis.informatique.cpoo5.liquidwar.service.ai.AggressiveAI;
import fr.uparis.informatique.cpoo5.liquidwar.service.ai.DefensiveAI;
import fr.uparis.informatique.cpoo5.liquidwar.service.ai.RandomAI;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;

/**
 * Gestionnaire de configuration pour GameCanvas.
 * 
 * Responsabilités :
 * - Gestion des types d'équipes (Humain/IA)
 * - Gestion des types de contrôle (Flèches/Souris/ZQSD)
 * - Gestion de la difficulté de l'IA
 * - Gestion du nombre de fighters par équipe
 */
public class GameCanvasConfiguration {

    // Types de contrôle par équipe : "Flèches", "Souris", "ZQSD" (max 4 équipes)
    private String[] teamControlTypes = { "Flèches", "Souris", "ZQSD", "Flèches" };

    // Types d'équipe : "Humain" ou "IA" - pour savoir quelles équipes sont
    // contrôlées par des humains
    private String[] teamTypes = { "Humain", "IA", "IA", "IA" };

    // Nombre de combattants par équipe (max 4 équipes)
    private int[] customFighterCounts = null; // null = utiliser la valeur par défaut

    // Configuration IA
    private String aiDifficulty = "Moyen"; // Par défaut
    private AIStrategy aiStrategy; // Instance de l'IA selon la difficulté

    // Nombre d'équipes pour le mode réseau (2-4)
    private int requestedTeams = 2;

    /**
     * Définit la difficulté de l'IA.
     * 
     * @param difficulty "Facile", "Moyen" ou "Difficile"
     */
    public void setAIDifficulty(String difficulty) {
        this.aiDifficulty = difficulty;

        // Créer l'instance de l'IA selon la difficulté
        switch (difficulty) {
            case "Facile":
                aiStrategy = new RandomAI();
                GameLogger.getInstance().info("IA configurée: Facile - Stratégie: RandomAI");
                break;
            case "Moyen":
                aiStrategy = new DefensiveAI();
                GameLogger.getInstance().info("IA configurée: Moyen - Stratégie: DefensiveAI");
                break;
            case "Difficile":
                aiStrategy = new AggressiveAI();
                GameLogger.getInstance().info("IA configurée: Difficile - Stratégie: AggressiveAI");
                break;
            default:
                aiStrategy = new AggressiveAI(); // Par défaut
                GameLogger.getInstance().warn("Difficulté inconnue: %s - Utilisation: AggressiveAI (par défaut)",
                        difficulty);
        }
    }

    /**
     * Définit le nombre d'équipes pour la partie (2-4).
     * 
     * @param teams Nombre d'équipes (2, 3 ou 4)
     */
    public void setRequestedTeams(int teams) {
        this.requestedTeams = Math.max(2, Math.min(4, teams));
    }

    /**
     * Définit les types de contrôle pour chaque équipe.
     * 
     * @param controlTypes Tableau de types : "Flèches", "Souris", ou "ZQSD"
     */
    public void setTeamControlTypes(String[] controlTypes) {
        if (controlTypes != null && controlTypes.length >= 2) {
            // Copier les valeurs dans le tableau existant (max 4 équipes)
            for (int i = 0; i < Math.min(controlTypes.length, 4); i++) {
                this.teamControlTypes[i] = controlTypes[i];
            }
            StringBuilder sb = new StringBuilder("🎮 Contrôles configurés - ");
            for (int i = 0; i < Math.min(controlTypes.length, 4); i++) {
                sb.append("Équipe ").append(i).append(": ").append(teamControlTypes[i]);
                if (i < Math.min(controlTypes.length, 4) - 1)
                    sb.append(", ");
            }
            GameLogger.getInstance().info(sb.toString());
        }
    }

    /**
     * Définit les types d'équipe (Humain ou IA) pour chaque équipe.
     * 
     * @param types Tableau de types : "Humain" ou "IA"
     */
    public void setTeamTypes(String[] types) {
        if (types != null && types.length >= 2) {
            // Copier les valeurs dans le tableau existant (max 4 équipes)
            for (int i = 0; i < Math.min(types.length, 4); i++) {
                this.teamTypes[i] = types[i];
            }
            StringBuilder sb = new StringBuilder("👥 Types d'équipe configurés - ");
            for (int i = 0; i < Math.min(types.length, 4); i++) {
                sb.append("Équipe ").append(i).append(": ").append(teamTypes[i]);
                if (i < Math.min(types.length, 4) - 1)
                    sb.append(", ");
            }
            GameLogger.getInstance().info(sb.toString());
        }
    }

    /**
     * Définit le nombre de combattants par équipe.
     * 
     * @param counts Tableau du nombre de combattants par équipe
     */
    public void setFighterCounts(int[] counts) {
        if (counts != null && counts.length >= 2) {
            this.customFighterCounts = counts.clone();
            GameLogger.getInstance().info("Combattants personnalisés configurés");
        }
    }

    /**
     * Vérifie si une équipe est contrôlée par un humain.
     * 
     * @param team Index de l'équipe
     * @return true si l'équipe est humaine
     */
    public boolean isTeamHuman(int team) {
        if (team < 0 || team >= teamTypes.length) {
            return false;
        }
        return "Humain".equals(teamTypes[team]);
    }

    // Getters
    public String[] getTeamControlTypes() {
        return teamControlTypes.clone();
    }

    public String[] getTeamTypes() {
        return teamTypes.clone();
    }

    public int[] getCustomFighterCounts() {
        return customFighterCounts != null ? customFighterCounts.clone() : null;
    }

    public String getAIDifficulty() {
        return aiDifficulty;
    }

    public AIStrategy getAIStrategy() {
        return aiStrategy;
    }

    public int getRequestedTeams() {
        return requestedTeams;
    }
}
