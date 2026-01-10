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

import fr.uparis.informatique.cpoo5.liquidwar.config.GameOptions;

/**
 * Panneau des options du jeu.
 * 
 * Permet de modifier :
 * - Volume sonore
 * - Qualité graphique
 * - Vitesse du jeu
 * 
 * ENTRÉE = Valider et quitter
 * ÉCHAP = Annuler les modifications et quitter
 */
public class OptionsMenuPanel extends JPanel {

    private int selectedIndex = 0;

    // Valeurs sauvegardées pour pouvoir annuler
    private int savedVolume;
    private String savedGraphicsQuality;
    private int savedGameSpeed;

    private static final String[] OPTIONS = {
            "Volume",
            "Qualité graphique",
            "Vitesse du jeu"
    };

    private static final String[] GRAPHICS_QUALITIES = { "Basse", "Moyenne", "Élevée" };

    private MenuNavigationListener navigationListener;

    public interface MenuNavigationListener {
        void onBack();
    }

    public OptionsMenuPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);

        // Sauvegarder les valeurs initiales
        saveCurrentValues();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    /**
     * Sauvegarde les valeurs actuelles pour pouvoir les restaurer si annulation.
     * Appelé à chaque fois que le panneau est affiché.
     */
    public void saveCurrentValues() {
        GameOptions options = GameOptions.getInstance();
        savedVolume = options.getVolumeLevel();
        savedGraphicsQuality = options.getGraphicsQuality();
        savedGameSpeed = options.getGameSpeed();
        System.out.println("💾 Options sauvegardées : Volume=" + savedVolume + "%, Qualité=" + savedGraphicsQuality
                + ", Vitesse=" + savedGameSpeed + "%");
    }

    /**
     * Restaure les valeurs sauvegardées (annulation).
     */
    private void restoreSavedValues() {
        GameOptions options = GameOptions.getInstance();
        options.setVolumeLevel(savedVolume);
        options.setGraphicsQuality(savedGraphicsQuality);
        options.setGameSpeed(savedGameSpeed);
        System.out.println("↩️ Options restaurées : Volume=" + savedVolume + "%, Qualité=" + savedGraphicsQuality
                + ", Vitesse=" + savedGameSpeed + "%");
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectedIndex = (selectedIndex - 1 + OPTIONS.length) % OPTIONS.length;
                break;
            case KeyEvent.VK_DOWN:
                selectedIndex = (selectedIndex + 1) % OPTIONS.length;
                break;
            case KeyEvent.VK_LEFT:
                changeValue(-1);
                break;
            case KeyEvent.VK_RIGHT:
                changeValue(1);
                break;
            case KeyEvent.VK_ENTER:
                // ENTRÉE = Valider les modifications et quitter
                System.out.println("✅ Options validées !");
                if (navigationListener != null) {
                    navigationListener.onBack();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                // ÉCHAP = Annuler les modifications et quitter
                restoreSavedValues();
                System.out.println("❌ Modifications annulées");
                if (navigationListener != null) {
                    navigationListener.onBack();
                }
                break;
        }
        repaint();
    }

    private void changeValue(int direction) {
        GameOptions options = GameOptions.getInstance();

        switch (selectedIndex) {
            case 0: // Volume
                int newVolume = options.getVolumeLevel() + direction * 10;
                options.setVolumeLevel(newVolume);
                System.out.println("🔊 Volume ajusté : " + options.getVolumeLevel() + "%");
                break;
            case 1: // Qualité graphique
                String currentQualityStr = options.getGraphicsQuality();
                int currentQuality = 0;
                for (int i = 0; i < GRAPHICS_QUALITIES.length; i++) {
                    if (GRAPHICS_QUALITIES[i].equals(currentQualityStr)) {
                        currentQuality = i;
                        break;
                    }
                }
                // Inverser la direction pour corriger le problème d'inversion
                int newQualityIdx = (currentQuality - direction + GRAPHICS_QUALITIES.length)
                        % GRAPHICS_QUALITIES.length;
                options.setGraphicsQuality(GRAPHICS_QUALITIES[newQualityIdx]);
                System.out.println("🖥️ Qualité graphique : " + options.getGraphicsQuality());
                break;
            case 2: // Vitesse du jeu
                // 🆕 Incrément de 25% pour des changements TRÈS visibles
                int newSpeed = options.getGameSpeed() + direction * 25;
                options.setGameSpeed(newSpeed);
                System.out.println("⏩ Vitesse du jeu : " + options.getGameSpeed() + "% " +
                        (options.getGameSpeed() < 50 ? "🐌 ULTRA LENT"
                                : options.getGameSpeed() < 100 ? "🚶 Lent"
                                        : options.getGameSpeed() == 100 ? "🏃 Normal"
                                                : options.getGameSpeed() < 150 ? "⚡ Rapide" : "🚀 ULTRA RAPIDE"));
                break;
        }
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
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "OPTIONS";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 5);

        // Options - taille agrandie
        int startY = height / 2 - 50;
        int spacing = 130;

        for (int i = 0; i < OPTIONS.length; i++) {
            int y = startY + i * spacing;
            boolean isSelected = (i == selectedIndex);

            if (isSelected) {
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 44));
                g2d.drawString(">", width / 2 - 400, y);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 38));
            }

            // Nom de l'option
            g2d.drawString(OPTIONS[i] + ":", width / 2 - 350, y);

            // Valeur depuis GameOptions
            GameOptions options = GameOptions.getInstance();
            String value = "";
            String speedEmoji = "";
            switch (i) {
                case 0:
                    value = options.getVolumeLevel() + "%";
                    break;
                case 1:
                    value = options.getGraphicsQuality();
                    break;
                case 2:
                    int speed = options.getGameSpeed();
                    value = speed + "%";
                    // 🆕 Emoji visuel pour la vitesse
                    if (speed <= 25)
                        speedEmoji = " 🐌";
                    else if (speed <= 50)
                        speedEmoji = " 🚶";
                    else if (speed <= 75)
                        speedEmoji = " 🏃";
                    else if (speed <= 125)
                        speedEmoji = " ⚡";
                    else if (speed <= 175)
                        speedEmoji = " 🚀";
                    else
                        speedEmoji = " 💨";
                    value += speedEmoji;
                    break;
            }

            // Barre de progression pour le volume et la vitesse
            if (i == 0 || i == 2) {
                int barX = width / 2 + 50;
                int barY = y - 25;
                int barWidth = 300;
                int barHeight = 35;
                // 🆕 Nouvelle plage : 25% à 200% (175 points de variation)
                int fillValue = (i == 0) ? options.getVolumeLevel() : options.getGameSpeed() - 25;
                int maxValue = (i == 0) ? 100 : 175;

                // Fond de la barre
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

                // Remplissage
                g2d.setColor(isSelected ? Color.YELLOW : new Color(100, 150, 255));
                int fillWidth = (int) ((fillValue / (float) maxValue) * barWidth);
                g2d.fillRoundRect(barX, barY, fillWidth, barHeight, 10, 10);

                // Texte
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 28));
                fm = g2d.getFontMetrics();
                int valueWidth = fm.stringWidth(value);
                g2d.drawString(value, barX + (barWidth - valueWidth) / 2, y);
            } else {
                g2d.drawString(value, width / 2 + 100, y);
            }
        }

        // Instructions - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instructions = "↑↓ naviguer | ←→ modifier | ENTRÉE valider | ÉCHAP annuler";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
    }
}
