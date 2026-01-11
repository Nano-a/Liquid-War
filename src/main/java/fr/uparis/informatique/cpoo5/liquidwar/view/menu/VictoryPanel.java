package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.JPanel;

/**
 * Panneau d'annonce de victoire
 * Affiche "VICTORY" avec la couleur de l'équipe gagnante
 * Style similaire au menu pause (fond sombre, overlay)
 */
public class VictoryPanel extends JPanel {
    private String[] menuItems = { "RECOMMENCER", "MENU PRINCIPAL", "QUITTER" };
    private int selectedIndex = 0;
    private Consumer<String> selectionListener;

    private Color winnerColor;
    private String winnerName;
    private String gameDuration;
    private boolean isTie = false;
    private int[] tieScores = null; // Scores pour l'égalité
    private String timeMode = "CHRONO"; // Mode de temps pour l'affichage

    public VictoryPanel() {
        setOpaque(false); // Transparent pour voir le jeu derrière
        setLayout(new GridBagLayout()); // Centrer le contenu
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    selectedIndex = (selectedIndex - 1 + menuItems.length) % menuItems.length;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedIndex = (selectedIndex + 1) % menuItems.length;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (selectionListener != null) {
                        selectionListener.accept(menuItems[selectedIndex]);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // Échap = Retour au menu principal
                    if (selectionListener != null) {
                        selectionListener.accept("MENU PRINCIPAL");
                    }
                }
            }
        });
    }

    /**
     * Définir les informations de victoire
     * 
     * @param winnerColor  Couleur de l'équipe gagnante (Bleu ou Rouge)
     * @param winnerName   Nom de l'équipe gagnante
     * @param gameDuration Durée de la partie (format "MM:SS")
     */
    public void setVictoryInfo(Color winnerColor, String winnerName, String gameDuration) {
        this.winnerColor = winnerColor;
        this.winnerName = winnerName;
        this.gameDuration = gameDuration;
        this.isTie = false;
        this.tieScores = null;
        repaint();
    }

    /**
     * Définir les informations d'égalité (mode minuterie).
     * 
     * @param teamFighterCounts Nombre de particules par équipe
     * @param gameDuration      Durée de la partie (format "MM:SS")
     * @param timeMode          Mode de temps ("CHRONO" ou "MINUTERIE")
     */
    public void setTieInfo(int[] teamFighterCounts, String gameDuration, String timeMode) {
        this.isTie = true;
        this.tieScores = teamFighterCounts != null ? teamFighterCounts.clone() : null;
        this.gameDuration = gameDuration;
        this.timeMode = timeMode != null ? timeMode : "CHRONO";
        this.winnerColor = null;
        this.winnerName = null;
        repaint();
    }

    public void setSelectionListener(Consumer<String> listener) {
        this.selectionListener = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Fond semi-transparent sombre (comme le menu pause)
        g.setColor(new Color(0, 0, 0, 180)); // Noir avec alpha 180 (plus opaque que pause)
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int titleY = 0; // Variable pour stocker titleY (accessible partout)

        if (isTie) {
            // ===== AFFICHAGE ÉGALITÉ =====
            g2d.setFont(new Font("Arial", Font.BOLD, 80));
            FontMetrics fmTitle = g2d.getFontMetrics();
            String title = "ÉGALITÉ";
            int titleWidth = fmTitle.stringWidth(title);
            titleY = centerY - 300; // Remonté de 100 pixels pour plus d'espace

            // Ombre du titre
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(title, centerX - titleWidth / 2 + 4, titleY + 4);

            // Titre en blanc (couleur neutre)
            g2d.setColor(Color.WHITE);
            g2d.drawString(title, centerX - titleWidth / 2, titleY);

            // ===== SCORES DES ÉQUIPES =====
            if (tieScores != null) {
                String[] teamNames = { "ÉQUIPE BLEUE", "ÉQUIPE ROUGE", "ÉQUIPE VERTE", "ÉQUIPE JAUNE" };
                Color[] teamColors = fr.uparis.informatique.cpoo5.liquidwar.view.GameRenderer.getCurrentTeamColors();

                // Compter les équipes actives (celles qui ont des particules)
                int activeCount = 0;
                for (int i = 0; i < tieScores.length; i++) {
                    if (tieScores[i] > 0) { // Équipe active avec des particules
                        activeCount++;
                    }
                }

                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                int startY = titleY + 120; // Légèrement plus bas pour l'espacement
                int spacing = 60;
                int displayedIndex = 0;

                // Afficher uniquement les équipes actives
                for (int i = 0; i < tieScores.length && i < teamNames.length; i++) {
                    if (tieScores[i] > 0) { // Équipe active avec des particules
                        int y = startY + displayedIndex * spacing;
                        String scoreText = teamNames[i] + ": " + tieScores[i] + " particules";

                        // Ombre
                        g2d.setColor(new Color(0, 0, 0, 150));
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(scoreText);
                        g2d.drawString(scoreText, centerX - textWidth / 2 + 3, y + 3);

                        // Texte avec couleur de l'équipe
                        g2d.setColor(teamColors[i % teamColors.length]);
                        g2d.drawString(scoreText, centerX - textWidth / 2, y);

                        displayedIndex++;
                    }
                }
            }
        } else {
            // ===== AFFICHAGE VICTOIRE NORMALE =====
            // ===== TITRE "VICTORY" =====
            g2d.setFont(new Font("Arial", Font.BOLD, 80));
            FontMetrics fmTitle = g2d.getFontMetrics();
            String title = "VICTORY";
            int titleWidth = fmTitle.stringWidth(title);
            titleY = centerY - 200; // Utiliser la variable déjà déclarée

            // Ombre du titre
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(title, centerX - titleWidth / 2 + 4, titleY + 4);

            // Titre avec couleur du gagnant
            g2d.setColor(winnerColor != null ? winnerColor : Color.WHITE);
            g2d.drawString(title, centerX - titleWidth / 2, titleY);

            // ===== NOM DU GAGNANT =====
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics fmWinner = g2d.getFontMetrics();
            String winnerText = winnerName != null ? winnerName : "Équipe Gagnante";
            int winnerWidth = fmWinner.stringWidth(winnerText);
            int winnerY = titleY + 80;

            // Ombre
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(winnerText, centerX - winnerWidth / 2 + 3, winnerY + 3);

            // Texte avec couleur du gagnant (comme le titre VICTORY)
            g2d.setColor(winnerColor != null ? winnerColor : Color.WHITE);
            g2d.drawString(winnerText, centerX - winnerWidth / 2, winnerY);

            // ===== DURÉE DE LA PARTIE =====
            if (gameDuration != null) {
                g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                FontMetrics fmDuration = g2d.getFontMetrics();
                String durationText = "Durée: " + gameDuration;
                int durationWidth = fmDuration.stringWidth(durationText);
                int durationY = winnerY + 50;

                g2d.setColor(new Color(200, 200, 200));
                g2d.drawString(durationText, centerX - durationWidth / 2, durationY);
            }
        }

        // ===== DURÉE DE LA PARTIE (pour égalité) =====
        if (isTie && gameDuration != null) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            FontMetrics fmDuration = g2d.getFontMetrics();
            // Afficher "Chrono" ou "Minuterie" selon le mode
            String timeLabel = "MINUTERIE".equals(timeMode) ? "Minuterie" : "Chrono";
            String durationText = timeLabel + ": " + gameDuration;
            int durationWidth = fmDuration.stringWidth(durationText);
            // Calculer la position Y en fonction du nombre d'équipes actives affichées
            int activeTeamsCount = 0;
            if (tieScores != null) {
                for (int score : tieScores) {
                    if (score > 0)
                        activeTeamsCount++;
                }
            }
            // Calculer depuis titleY pour être cohérent
            int lastScoreY = titleY + 120 + (activeTeamsCount * 60);
            int durationY = lastScoreY + 40; // Plus d'espace après les scores

            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString(durationText, centerX - durationWidth / 2, durationY);
        }

        // ===== SÉPARATEUR =====
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(centerX - 200, centerY - 50, centerX + 200, centerY - 50);

        // ===== MENU OPTIONS =====
        // Positionner le menu plus bas si c'est une égalité pour plus d'espace
        int startY = isTie ? centerY + 100 : centerY + 20;
        int itemSpacing = 60;

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            int y = startY + i * itemSpacing;

            if (i == selectedIndex) {
                // Item sélectionné
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 32));

                // Indicateur de sélection
                g2d.drawString(">", centerX - 200, y);

            } else {
                // Item non sélectionné
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 28));
            }

            FontMetrics fm = g2d.getFontMetrics();
            int itemWidth = fm.stringWidth(item);

            // Ombre
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(item, centerX - itemWidth / 2 + 2, y + 2);

            // Texte
            if (i == selectedIndex) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.drawString(item, centerX - itemWidth / 2, y);
        }

        // ===== INSTRUCTIONS =====
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String instructions = "↑↓ naviguer | ENTRÉE sélectionner";
        FontMetrics fmInstr = g2d.getFontMetrics();
        int instrWidth = fmInstr.stringWidth(instructions);
        g2d.drawString(instructions, centerX - instrWidth / 2, getHeight() - 50);

        // ===== DÉCORATION - ÉTOILES =====
        if (!isTie) {
            drawStars(g2d, centerX, (centerY - 200) - 40, winnerColor);
        }
    }

    /**
     * Dessiner des étoiles décoratives autour du titre
     */
    private void drawStars(Graphics2D g2d, int centerX, int centerY, Color color) {
        if (color == null)
            color = Color.YELLOW;

        // Étoiles à gauche et à droite du titre
        int[] xOffsets = { -250, -200, 200, 250 };
        int[] ySizes = { 20, 30, 30, 20 };

        for (int i = 0; i < xOffsets.length; i++) {
            drawStar(g2d, centerX + xOffsets[i], centerY, ySizes[i], color);
        }
    }

    /**
     * Dessiner une étoile à 5 branches
     */
    private void drawStar(Graphics2D g2d, int centerX, int centerY, int size, Color color) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        double angle = -Math.PI / 2; // Commencer en haut
        double angleIncrement = Math.PI / 5;

        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? size : size / 2.5;
            xPoints[i] = centerX + (int) (Math.cos(angle) * radius);
            yPoints[i] = centerY + (int) (Math.sin(angle) * radius);
            angle += angleIncrement;
        }

        // Ombre de l'étoile
        g2d.setColor(new Color(0, 0, 0, 100));
        Polygon shadowStar = new Polygon(xPoints, yPoints, 10);
        g2d.translate(3, 3);
        g2d.fillPolygon(shadowStar);
        g2d.translate(-3, -3);

        // Étoile
        g2d.setColor(color);
        Polygon star = new Polygon(xPoints, yPoints, 10);
        g2d.fillPolygon(star);

        // Contour
        g2d.setColor(color.brighter());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(star);
    }

    /**
     * Réinitialiser la sélection
     */
    public void resetSelection() {
        selectedIndex = 0;
        repaint();
    }
}
