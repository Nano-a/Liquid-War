package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

/**
 * Panneau de sélection du mode de temps (Chrono ou Minuterie).
 * 
 * Permet de choisir entre :
 * - CHRONO : Pas de limite de temps, jeu normal
 * - MINUTERIE : Jeu avec durée limitée, arrêt automatique
 */
public class TimeModeMenuPanel extends JPanel {

    private int selectedIndex = 0;
    private int selectedDuration = 5; // Durée par défaut en minutes (5 minutes)
    private boolean selectingDuration = false; // Si on est en train de choisir la durée

    private static final String[] TIME_MODES = {
            "CHRONO",
            "MINUTERIE"
    };

    private static final String[] MODE_DESCRIPTIONS = {
            "Jeu sans limite de temps",
            "Jeu avec durée limitée"
    };

    // Durées possibles en minutes
    private static final int[] DURATIONS = { 1, 2, 3, 5, 10, 15, 20, 30 };

    private TimeModeSelectionListener selectionListener;
    private MenuNavigationListener navigationListener;

    public interface TimeModeSelectionListener {
        void onTimeModeSelected(String mode, Integer durationMinutes);
    }

    public interface MenuNavigationListener {
        void onBack();
    }

    public TimeModeMenuPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        setRequestFocusEnabled(true);

        // Ajouter un FocusListener pour s'assurer que le panneau reçoit le focus
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("TimeModeMenuPanel: Focus obtenu");
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("TimeModeMenuPanel: Focus perdu");
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("TimeModeMenuPanel: Touche pressée: " + e.getKeyCode());
                handleKeyPress(e);
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        if (selectingDuration) {
            // Mode sélection de durée
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    selectPreviousDuration();
                    break;
                case KeyEvent.VK_DOWN:
                    selectNextDuration();
                    break;
                case KeyEvent.VK_ENTER:
                    // Valider la durée et continuer
                    if (selectionListener != null) {
                        selectionListener.onTimeModeSelected("MINUTERIE", selectedDuration);
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    // Retour au choix du mode
                    selectingDuration = false;
                    break;
            }
        } else {
            // Mode sélection du type de temps
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    selectPrevious();
                    break;
                case KeyEvent.VK_DOWN:
                    selectNext();
                    break;
                case KeyEvent.VK_ENTER:
                    if (selectedIndex == 0) {
                        // CHRONO : pas de durée, continuer directement
                        if (selectionListener != null) {
                            selectionListener.onTimeModeSelected("CHRONO", null);
                        }
                    } else {
                        // MINUTERIE : entrer en mode sélection de durée
                        selectingDuration = true;
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    if (navigationListener != null) {
                        navigationListener.onBack();
                    }
                    break;
            }
        }
        repaint();
    }

    public void selectNext() {
        selectedIndex = (selectedIndex + 1) % TIME_MODES.length;
        repaint();
    }

    public void selectPrevious() {
        selectedIndex = (selectedIndex - 1 + TIME_MODES.length) % TIME_MODES.length;
        repaint();
    }

    private void selectNextDuration() {
        for (int i = 0; i < DURATIONS.length; i++) {
            if (DURATIONS[i] == selectedDuration) {
                selectedDuration = DURATIONS[(i + 1) % DURATIONS.length];
                break;
            }
        }
        repaint();
    }

    private void selectPreviousDuration() {
        for (int i = 0; i < DURATIONS.length; i++) {
            if (DURATIONS[i] == selectedDuration) {
                int prevIndex = (i - 1 + DURATIONS.length) % DURATIONS.length;
                selectedDuration = DURATIONS[prevIndex];
                break;
            }
        }
        repaint();
    }

    public void setSelectionListener(TimeModeSelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setNavigationListener(MenuNavigationListener listener) {
        this.navigationListener = listener;
    }

    public void resetSelection() {
        selectedIndex = 0;
        selectedDuration = 5;
        selectingDuration = false;
        repaint();
    }

    /**
     * S'assure que le panneau peut recevoir le focus
     */
    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (selectingDuration) {
            // Afficher le menu de sélection de durée
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 64));
            FontMetrics fm = g2d.getFontMetrics();
            String title = "DURÉE DE LA PARTIE";
            int titleWidth = fm.stringWidth(title);
            g2d.drawString(title, (width - titleWidth) / 2, height / 4);

            // Afficher les durées disponibles
            int startY = height / 2 - 100;
            int spacing = 80;

            for (int i = 0; i < DURATIONS.length; i++) {
                int y = startY + i * spacing;
                int duration = DURATIONS[i];
                String durationText = duration + " minute" + (duration > 1 ? "s" : "");

                if (duration == selectedDuration) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Arial", Font.BOLD, 48));
                    g2d.drawString(">", width / 2 - 250, y);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 40));
                }

                fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(durationText);
                g2d.drawString(durationText, (width - textWidth) / 2, y);
            }

            // Instructions
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 28));
            String instructions = "↑↓ pour naviguer, ENTRÉE pour valider, ÉCHAP pour retour";
            fm = g2d.getFontMetrics();
            int instrWidth = fm.stringWidth(instructions);
            g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
        } else {
            // Afficher le menu de sélection du mode de temps
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 64));
            FontMetrics fm = g2d.getFontMetrics();
            String title = "MODE DE TEMPS";
            int titleWidth = fm.stringWidth(title);
            g2d.drawString(title, (width - titleWidth) / 2, height / 5);

            // Modes de temps
            int startY = height / 2 - 50;
            int spacing = 150;

            for (int i = 0; i < TIME_MODES.length; i++) {
                int y = startY + i * spacing;

                if (i == selectedIndex) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Arial", Font.BOLD, 52));
                    g2d.drawString(">", width / 2 - 300, y);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 44));
                }

                fm = g2d.getFontMetrics();
                int modeWidth = fm.stringWidth(TIME_MODES[i]);
                g2d.drawString(TIME_MODES[i], (width - modeWidth) / 2, y);

                // Description
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Arial", Font.ITALIC, 28));
                fm = g2d.getFontMetrics();
                int descWidth = fm.stringWidth(MODE_DESCRIPTIONS[i]);
                g2d.drawString(MODE_DESCRIPTIONS[i], (width - descWidth) / 2, y + 45);
            }

            // Instructions
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 28));
            String instructions = "↑↓ pour naviguer, ENTRÉE pour sélectionner, ÉCHAP pour retour";
            fm = g2d.getFontMetrics();
            int instrWidth = fm.stringWidth(instructions);
            g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
        }
    }
}
