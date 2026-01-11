package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import fr.uparis.informatique.cpoo5.liquidwar.config.RenderConfig;

/**
 * Panneau de configuration des équipes.
 * 
 * Permet de configurer :
 * - Le type de contrôle (Humain/IA)
 * - Le niveau de l'IA (Facile/Moyen/Difficile)
 * - Le type de contrôle pour les humains (Flèches/Souris/ZQSD)
 * - Le nombre de combattants
 */
public class TeamConfigMenuPanel extends JPanel {

    private int selectedTeam = 0; // 0 ou 1
    private int selectedField = 0; // 0 = type, 1 = niveau IA ou contrôle, 2 = couleur, 3 = nombre

    // Configuration des équipes
    private String[] teamTypes = { "Humain", "IA" };
    private String[] aiLevels = { "Facile", "Moyen", "Difficile" }; // 3 niveaux disponibles
    private String[] controlTypes = { "Flèches", "Souris" }; // Types de contrôle pour humains
    private int[] fighterCounts = { 2000, 2000 }; // Nombre de combattants par équipe
    private int[] selectedColorIndices = { 0, 1 }; // Index des couleurs choisies (0-5)

    // Options de contrôle disponibles
    private static final String[] CONTROL_OPTIONS = { "Flèches", "Souris", "ZQSD" };

    // Toutes les couleurs disponibles (6 couleurs)
    private static final Color[] AVAILABLE_COLORS = RenderConfig.TEAM_COLORS;
    private static final String[] COLOR_NAMES = { "Bleue", "Rouge", "Verte", "Jaune", "Magenta", "Cyan" };

    // Couleurs actuelles des équipes (pour l'affichage)
    private Color[] currentTeamColors = {
            RenderConfig.TEAM_COLORS[0], // Bleu par défaut
            RenderConfig.TEAM_COLORS[1] // Rouge par défaut
    };

    // Message d'erreur pour conflit de contrôle
    private String errorMessage = null;
    private long errorDisplayTime = 0;
    private static final long ERROR_DISPLAY_DURATION = 3000; // 3 secondes

    private ConfigCompleteListener completeListener;
    private MenuNavigationListener navigationListener;

    public interface ConfigCompleteListener {
        void onConfigComplete(TeamConfiguration config);
    }

    public interface MenuNavigationListener {
        void onBack();
    }

    /**
     * Configuration des équipes à passer au jeu
     */
    public static class TeamConfiguration {
        public String[] teamTypes;
        public String[] aiLevels;
        public String[] controlTypes; // Types de contrôle
        public int[] fighterCounts;
        public int[] colorIndices; // Index des couleurs choisies

        public TeamConfiguration(String[] types, String[] levels, String[] controls, int[] counts, int[] colors) {
            this.teamTypes = types;
            this.aiLevels = levels;
            this.controlTypes = controls;
            this.fighterCounts = counts;
            this.colorIndices = colors;
        }
    }

    public TeamConfigMenuPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectPreviousField();
                break;
            case KeyEvent.VK_DOWN:
                selectNextField();
                break;
            case KeyEvent.VK_LEFT:
                changeValue(-1);
                break;
            case KeyEvent.VK_RIGHT:
                changeValue(1);
                break;
            case KeyEvent.VK_ENTER:
                tryStartGame();
                break;
            case KeyEvent.VK_ESCAPE:
                if (navigationListener != null) {
                    navigationListener.onBack();
                }
                break;
        }
        repaint();
    }

    /**
     * Tente de démarrer le jeu en vérifiant les conflits de contrôle et de couleur.
     */
    private void tryStartGame() {
        // Vérifier si les deux équipes sont humaines avec le même contrôle
        if ("Humain".equals(teamTypes[0]) && "Humain".equals(teamTypes[1])) {
            if (controlTypes[0].equals(controlTypes[1])) {
                // Conflit de contrôle !
                showControlConflictError();
                return;
            }
        }

        // Vérifier les conflits de couleur
        if (selectedColorIndices[0] == selectedColorIndices[1]) {
            // Conflit de couleur !
            showColorConflictError();
            return;
        }

        // Pas de conflit, on peut démarrer
        if (completeListener != null) {
            TeamConfiguration config = new TeamConfiguration(
                    teamTypes.clone(), aiLevels.clone(), controlTypes.clone(),
                    fighterCounts.clone(), selectedColorIndices.clone());
            completeListener.onConfigComplete(config);
        }
    }

    /**
     * Affiche un message d'erreur sympa pour le conflit de contrôle.
     */
    private void showControlConflictError() {
        String[] funnyMessages = {
                "🎮 Hé ho ! Vous ne pouvez pas utiliser le même contrôle tous les deux !",
                "⚔️ Partagez les contrôles, pas les conflits !",
                "🤝 Chacun son style ! Changez de contrôle pour l'un des joueurs.",
                "🎯 Même contrôle = même problème ! Variez les plaisirs !",
                "🕹️ Deux joueurs, deux contrôles différents, s'il vous plaît !",
                "⚡ Conflit détecté ! Les vrais rivaux ont des contrôles différents."
        };

        // Choisir un message aléatoire
        int index = (int) (Math.random() * funnyMessages.length);
        errorMessage = funnyMessages[index];
        errorDisplayTime = System.currentTimeMillis();
    }

    /**
     * Affiche un message d'erreur sympa pour le conflit de couleur.
     */
    private void showColorConflictError() {
        String[] funnyMessages = {
                "🎨 Impossible ! Deux équipes ne peuvent pas avoir la même couleur !",
                "🌈 Chaque équipe doit avoir sa propre couleur ! Changez-en une.",
                "🎯 Même couleur = confusion totale ! Distinguez-vous !",
                "🖌️ Variez les couleurs ! Chaque équipe mérite sa propre identité !",
                "💫 Deux équipes, deux couleurs différentes, s'il vous plaît !",
                "⚡ Conflit de couleur ! Les vrais rivaux ont des couleurs distinctes."
        };

        // Choisir un message aléatoire
        int index = (int) (Math.random() * funnyMessages.length);
        errorMessage = funnyMessages[index];
        errorDisplayTime = System.currentTimeMillis();
    }

    /**
     * Calcule le nombre de champs pour une équipe donnée.
     */
    private int getMaxFields(int team) {
        return 4; // type, niveau IA/contrôle, couleur, combattants
    }

    private void selectNextField() {
        selectedField++;
        int maxFields = getMaxFields(selectedTeam);

        if (selectedField >= maxFields) {
            selectedField = 0;
            selectedTeam = (selectedTeam + 1) % 2;
        }
        repaint();
    }

    private void selectPreviousField() {
        selectedField--;

        if (selectedField < 0) {
            selectedTeam = (selectedTeam - 1 + 2) % 2;
            int maxFields = getMaxFields(selectedTeam);
            selectedField = maxFields - 1;
        }
        repaint();
    }

    private void changeValue(int direction) {
        if (selectedField == 0) {
            // Changer le type (Humain <-> IA)
            teamTypes[selectedTeam] = teamTypes[selectedTeam].equals("Humain") ? "IA" : "Humain";
        } else if (selectedField == 1) {
            if ("IA".equals(teamTypes[selectedTeam])) {
                // Changer le niveau d'IA
                String[] levels = { "Facile", "Moyen", "Difficile" };
                int currentIndex = 0;
                for (int i = 0; i < levels.length; i++) {
                    if (levels[i].equals(aiLevels[selectedTeam])) {
                        currentIndex = i;
                        break;
                    }
                }
                int newIndex = (currentIndex + direction + levels.length) % levels.length;
                aiLevels[selectedTeam] = levels[newIndex];
            } else {
                // Changer le type de contrôle pour humain
                int currentIndex = 0;
                for (int i = 0; i < CONTROL_OPTIONS.length; i++) {
                    if (CONTROL_OPTIONS[i].equals(controlTypes[selectedTeam])) {
                        currentIndex = i;
                        break;
                    }
                }
                int newIndex = (currentIndex + direction + CONTROL_OPTIONS.length) % CONTROL_OPTIONS.length;
                controlTypes[selectedTeam] = CONTROL_OPTIONS[newIndex];
            }
        } else if (selectedField == 2) {
            // Changer la couleur
            int newIndex = (selectedColorIndices[selectedTeam] + direction + AVAILABLE_COLORS.length)
                    % AVAILABLE_COLORS.length;
            selectedColorIndices[selectedTeam] = newIndex;
            currentTeamColors[selectedTeam] = AVAILABLE_COLORS[newIndex];
        } else if (selectedField == 3) {
            // Changer le nombre de combattants (500-4000)
            fighterCounts[selectedTeam] += direction * 100;
            fighterCounts[selectedTeam] = Math.max(500, Math.min(4000, fighterCounts[selectedTeam]));
        }
        repaint();
    }

    public void setCompleteListener(ConfigCompleteListener listener) {
        this.completeListener = listener;
    }

    public void setNavigationListener(MenuNavigationListener listener) {
        this.navigationListener = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Titre - taille agrandie
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 64));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "CONFIGURATION DES ÉQUIPES";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 7);

        // Configuration pour chaque équipe - taille agrandie
        int startY = height / 4 + 50;
        int teamSpacing = 320;

        for (int team = 0; team < 2; team++) {
            int y = startY + team * teamSpacing;

            // Nom de l'équipe avec couleur - taille agrandie
            g2d.setColor(currentTeamColors[team]);
            g2d.setFont(new Font("Arial", Font.BOLD, 44));
            String teamTitle = "ÉQUIPE " + (team + 1) + " (" + COLOR_NAMES[selectedColorIndices[team]] + ")";
            fm = g2d.getFontMetrics();
            int teamTitleWidth = fm.stringWidth(teamTitle);
            g2d.drawString(teamTitle, (width - teamTitleWidth) / 2, y);

            // Champ 0 : Type de contrôle
            int fieldY = y + 80;
            boolean isSelected = (selectedTeam == team && selectedField == 0);
            drawField(g2d, "Type:", teamTypes[team], width / 2 - 250, fieldY, isSelected);

            // Champ 1 : Niveau IA ou Type de contrôle
            fieldY += 60;
            isSelected = (selectedTeam == team && selectedField == 1);
            if ("IA".equals(teamTypes[team])) {
                drawField(g2d, "Niveau IA:", aiLevels[team], width / 2 - 250, fieldY, isSelected);
            } else {
                drawField(g2d, "Contrôle:", controlTypes[team], width / 2 - 250, fieldY, isSelected);
            }

            // Champ 2 : Couleur
            fieldY += 60;
            isSelected = (selectedTeam == team && selectedField == 2);
            // Afficher un carré de couleur à côté du nom
            String colorName = COLOR_NAMES[selectedColorIndices[team]];
            drawFieldWithColor(g2d, "Couleur:", colorName, width / 2 - 250, fieldY, isSelected,
                    currentTeamColors[team]);

            // Champ 3 : Nombre de combattants
            fieldY += 60;
            isSelected = (selectedTeam == team && selectedField == 3);
            drawField(g2d, "Combattants:", String.valueOf(fighterCounts[team]), width / 2 - 250, fieldY, isSelected);
        }

        // Afficher le message d'erreur si présent - taille agrandie
        if (errorMessage != null) {
            long elapsed = System.currentTimeMillis() - errorDisplayTime;
            if (elapsed < ERROR_DISPLAY_DURATION) {
                // Calculer l'opacité (fade out)
                float alpha = 1.0f - (float) elapsed / ERROR_DISPLAY_DURATION;
                alpha = Math.max(0.3f, alpha); // Minimum 30% d'opacité

                // Fond semi-transparent pour le message
                g2d.setColor(new Color(80, 0, 0, (int) (200 * alpha)));
                int msgBoxY = height - 250;
                g2d.fillRoundRect(100, msgBoxY - 40, width - 200, 80, 20, 20);

                // Bordure
                g2d.setColor(new Color(255, 100, 100, (int) (255 * alpha)));
                g2d.drawRoundRect(100, msgBoxY - 40, width - 200, 80, 20, 20);

                // Message d'erreur
                g2d.setColor(new Color(255, 255, 255, (int) (255 * alpha)));
                g2d.setFont(new Font("Arial", Font.BOLD, 28));
                fm = g2d.getFontMetrics();
                int msgWidth = fm.stringWidth(errorMessage);
                g2d.drawString(errorMessage, (width - msgWidth) / 2, msgBoxY);
            } else {
                errorMessage = null;
            }
        }

        // Légende des contrôles - taille agrandie
        int legendY = height - 150;
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));

        String[] legendLines = {
                "Flèches : ↑ ↓ ← →  |  Souris : Déplacer la souris  |  ZQSD : Z↑ Q← S↓ D→"
        };

        fm = g2d.getFontMetrics();
        for (String line : legendLines) {
            int lineWidth = fm.stringWidth(line);
            g2d.drawString(line, (width - lineWidth) / 2, legendY);
            legendY += 35;
        }

        // Instructions - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instr = "↑↓ changer champ | ←→ modifier | ENTRÉE commencer | ÉCHAP retour";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instr);
        g2d.drawString(instr, (width - instrWidth) / 2, height - 60);
    }

    private void drawField(Graphics2D g2d, String label, String value, int x, int y, boolean selected) {
        if (selected) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString(">", x - 40, y);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 32));
        }

        g2d.drawString(label + " " + value, x, y);
    }

    private void drawFieldWithColor(Graphics2D g2d, String label, String value, int x, int y, boolean selected,
            Color color) {
        if (selected) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString(">", x - 40, y);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 32));
        }

        g2d.drawString(label + " " + value, x, y);

        // Dessiner un carré de couleur à droite du texte
        int colorBoxX = x + g2d.getFontMetrics().stringWidth(label + " " + value) + 20;
        int colorBoxY = y - 20;
        g2d.setColor(color);
        g2d.fillRect(colorBoxX, colorBoxY, 30, 30);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(colorBoxX, colorBoxY, 30, 30);
    }
}
