package fr.uparis.informatique.cpoo5.liquidwar.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;

/**
 * Panneau des statistiques du jeu affiché à droite.
 * 
 * Affiche :
 * - Bouton Menu (pour le menu pause)
 * - Durée de la partie
 * - Nombre de combattants par équipe (jusqu'à 4 équipes)
 * - Pourcentage par équipe
 */
public class GameStatsPanel extends JPanel {

    private JButton menuButton;
    private JLabel timeLabel;
    private JLabel timeTitle; // Titre du temps (Chrono ou Minuterie)

    // Support jusqu'à 4 équipes
    private static final int MAX_TEAMS = 4;
    private JLabel[] teamCountLabels = new JLabel[MAX_TEAMS];
    private JLabel[] teamPercentLabels = new JLabel[MAX_TEAMS];
    private JProgressBar[] teamProgressBars = new JProgressBar[MAX_TEAMS];
    private JPanel[] teamPanels = new JPanel[MAX_TEAMS];

    private long gameStartTime;
    private int[] teamCounts = new int[MAX_TEAMS];
    private int activeTeams = 2;
    private String timeMode = "CHRONO"; // Mode de temps : "CHRONO" ou "MINUTERIE"
    private Integer timerDurationMinutes = null; // Durée en minutes pour la minuterie (null = chrono)

    // Noms et émojis des équipes
    private static final String[] TEAM_NAMES = { "BLEUE", "ROUGE", "VERTE", "JAUNE" };
    private static final String[] TEAM_EMOJIS = { "🔵", "🔴", "🟢", "🟡" };

    public GameStatsPanel() {
        this(2); // 2 équipes par défaut
    }

    public GameStatsPanel(int activeTeams) {
        this.activeTeams = Math.max(2, Math.min(MAX_TEAMS, activeTeams));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(20, 20, 30));
        setPreferredSize(new Dimension(250, 600));
        setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        gameStartTime = System.currentTimeMillis();

        initComponents();
    }

    /**
     * Définit le nombre d'équipes actives et reconstruit l'UI.
     */
    public void setActiveTeams(int teams) {
        this.activeTeams = Math.max(2, Math.min(MAX_TEAMS, teams));
        removeAll();
        initComponents();
        revalidate();
        repaint();
    }

    private void initComponents() {
        // Bouton Menu
        menuButton = new JButton("⏸ MENU");
        menuButton.setFont(new Font("Arial", Font.BOLD, 16));
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.setMaximumSize(new Dimension(220, 50));
        menuButton.setBackground(new Color(70, 70, 90));
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusPainted(false);
        add(menuButton);

        add(Box.createRigidArea(new Dimension(0, 20)));

        // Titre Statistiques
        JLabel statsTitle = createLabel("📊 STATISTIQUES", 18, true);
        add(statsTitle);
        add(Box.createRigidArea(new Dimension(0, 15)));

        // Temps écoulé
        timeTitle = createLabel("⏱ Chrono:", 14, true);
        add(timeTitle);
        timeLabel = createLabel("00:00", 16, false);
        add(timeLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));

        // Séparateur
        add(createSeparator());
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Créer les panneaux pour chaque équipe active
        for (int i = 0; i < activeTeams; i++) {
            createTeamPanel(i);
            add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(Box.createVerticalGlue());
    }

    private void createTeamPanel(int teamIndex) {
        // Titre de l'équipe
        JLabel teamTitle = createLabel(TEAM_EMOJIS[teamIndex] + " ÉQUIPE " + TEAM_NAMES[teamIndex], 13, true);
        teamTitle.setForeground(
                GameRenderer.getCurrentTeamColors()[teamIndex % GameRenderer.getCurrentTeamColors().length]);
        add(teamTitle);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // Nombre de combattants
        teamCountLabels[teamIndex] = createLabel("0 combattants", 11, false);
        add(teamCountLabels[teamIndex]);
        add(Box.createRigidArea(new Dimension(0, 3)));

        // Pourcentage
        teamPercentLabels[teamIndex] = createLabel("0%", 12, true);
        teamPercentLabels[teamIndex].setForeground(
                GameRenderer.getCurrentTeamColors()[teamIndex % GameRenderer.getCurrentTeamColors().length]);
        add(teamPercentLabels[teamIndex]);
        add(Box.createRigidArea(new Dimension(0, 3)));

        // Barre de progression
        teamProgressBars[teamIndex] = new JProgressBar(0, 100);
        teamProgressBars[teamIndex].setValue(0);
        teamProgressBars[teamIndex].setStringPainted(true);
        teamProgressBars[teamIndex].setForeground(
                GameRenderer.getCurrentTeamColors()[teamIndex % GameRenderer.getCurrentTeamColors().length]);
        teamProgressBars[teamIndex].setMaximumSize(new Dimension(220, 20));
        add(teamProgressBars[teamIndex]);
    }

    private JLabel createLabel(String text, int fontSize, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, fontSize));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(220, 2));
        separator.setForeground(Color.GRAY);
        return separator;
    }

    /**
     * Met à jour les statistiques (version 2 équipes pour compatibilité)
     */
    public void updateStats(int team1Count, int team2Count) {
        updateStats(new int[] { team1Count, team2Count, 0, 0 });
    }

    /**
     * Met à jour les statistiques pour toutes les équipes
     */
    public void updateStats(int[] counts) {
        int total = 0;
        for (int i = 0; i < activeTeams && i < counts.length; i++) {
            teamCounts[i] = counts[i];
            total += counts[i];
        }

        // Mettre à jour chaque équipe
        for (int i = 0; i < activeTeams; i++) {
            int percent = total > 0 ? (teamCounts[i] * 100) / total : 0;

            if (teamCountLabels[i] != null) {
                teamCountLabels[i].setText(teamCounts[i] + " combattants");
            }
            if (teamPercentLabels[i] != null) {
                teamPercentLabels[i].setText(percent + "%");
            }
            if (teamProgressBars[i] != null) {
                teamProgressBars[i].setValue(percent);
            }
        }

        // Mettre à jour le temps
        updateTime();
    }

    /**
     * Met à jour le temps écoulé (chrono) ou le compte à rebours (minuterie)
     */
    private void updateTime() {
        if ("MINUTERIE".equals(timeMode) && timerDurationMinutes != null && gameStartTime > 0) {
            // Mode minuterie : compte à rebours
            long currentTime = System.currentTimeMillis();
            long elapsedMillis = currentTime - gameStartTime;
            long elapsedSeconds = elapsedMillis / 1000;
            long totalSeconds = timerDurationMinutes * 60L;
            long remainingSeconds = totalSeconds - elapsedSeconds;

            // Si le temps est écoulé, afficher 00:00
            if (remainingSeconds <= 0) {
                timeLabel.setText("00:00");
            } else {
                int minutes = (int) (remainingSeconds / 60);
                int seconds = (int) (remainingSeconds % 60);
                timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
            }
        } else {
            // Mode chrono : temps écoulé normal
            if (gameStartTime > 0) {
                long elapsed = System.currentTimeMillis() - gameStartTime;
                int seconds = (int) (elapsed / 1000) % 60;
                int minutes = (int) (elapsed / 60000);
                timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
            } else {
                timeLabel.setText("00:00");
            }
        }
    }

    /**
     * Définit le listener pour le bouton menu
     */
    public void setMenuButtonListener(ActionListener listener) {
        menuButton.addActionListener(listener);
    }

    /**
     * Obtient le pourcentage de l'équipe 1
     */
    public int getTeam1Percent() {
        int total = 0;
        for (int i = 0; i < activeTeams; i++) {
            total += teamCounts[i];
        }
        return total > 0 ? (teamCounts[0] * 100) / total : 0;
    }

    /**
     * Obtient le pourcentage de l'équipe 2
     */
    public int getTeam2Percent() {
        int total = 0;
        for (int i = 0; i < activeTeams; i++) {
            total += teamCounts[i];
        }
        return total > 0 ? (teamCounts[1] * 100) / total : 0;
    }

    /**
     * Obtient le pourcentage d'une équipe spécifique
     */
    public int getTeamPercent(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= activeTeams)
            return 0;
        int total = 0;
        for (int i = 0; i < activeTeams; i++) {
            total += teamCounts[i];
        }
        return total > 0 ? (teamCounts[teamIndex] * 100) / total : 0;
    }

    /**
     * Vérifie si une équipe a été éliminée
     */
    public boolean isTeamEliminated(int teamIndex) {
        return teamIndex >= 0 && teamIndex < activeTeams && teamCounts[teamIndex] == 0;
    }

    /**
     * Obtient l'équipe gagnante (si une seule reste)
     * 
     * @return Index de l'équipe gagnante, ou -1 si pas encore de gagnant
     */
    public int getWinningTeam() {
        int aliveTeams = 0;
        int lastAlive = -1;

        for (int i = 0; i < activeTeams; i++) {
            if (teamCounts[i] > 0) {
                aliveTeams++;
                lastAlive = i;
            }
        }

        return aliveTeams == 1 ? lastAlive : -1;
    }

    /**
     * Réinitialise le chronomètre
     */
    public void resetTimer() {
        gameStartTime = System.currentTimeMillis();
    }

    /**
     * Obtient le temps écoulé depuis le début du jeu
     */
    public String getElapsedTime() {
        long elapsed = System.currentTimeMillis() - gameStartTime;
        int seconds = (int) (elapsed / 1000) % 60;
        int minutes = (int) (elapsed / 60000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Obtient le temps écoulé en secondes
     */
    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }

    /**
     * Définit le mode de temps (Chrono ou Minuterie)
     * 
     * @param mode            "CHRONO" ou "MINUTERIE"
     * @param durationMinutes Durée en minutes pour la minuterie (null si chrono)
     */
    public void setTimeMode(String mode, Integer durationMinutes) {
        this.timeMode = mode;
        this.timerDurationMinutes = durationMinutes;
        if (timeTitle != null) {
            if ("MINUTERIE".equals(mode)) {
                timeTitle.setText("⏱ Minuterie:");
            } else {
                timeTitle.setText("⏱ Chrono:");
            }
        }
        // Initialiser l'affichage du temps
        if (timeLabel != null) {
            updateTime();

        }
    }

    /**
     * Définit le temps de début de partie (pour synchroniser avec le jeu)
     * 
     * @param startTime Temps de début en millisecondes
     */
    public void setGameStartTime(long startTime) {
        this.gameStartTime = startTime;
        if (timeLabel != null) {
            updateTime();
        }
    }
}