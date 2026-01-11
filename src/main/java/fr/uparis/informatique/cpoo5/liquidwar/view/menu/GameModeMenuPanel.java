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

/**
 * Panneau de sélection du mode de jeu.
 * 
 * Modes disponibles : SOLO, DUO LOCAL, RÉSEAU (à venir)
 */
public class GameModeMenuPanel extends JPanel {

    private int selectedIndex = 0;

    private static final String[] GAME_MODES = {
            "SOLO OU DUO",
            "TRIO LOCAL",
            "MULTIJOUEUR RÉSEAU"
    };

    private static final String[] MODE_DESCRIPTIONS = {
            "Jouez seul contre l'IA ou à deux",
            "Jouez à trois sur le même ordinateur",
            "Jouez en ligne avec d'autres joueurs"
    };

    private MenuSelectionListener selectionListener;
    private MenuNavigationListener navigationListener;

    public interface MenuSelectionListener {
        void onModeSelected(String mode);
    }

    public interface MenuNavigationListener {
        void onBack();
    }

    public GameModeMenuPanel() {
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
                selectPrevious();
                break;
            case KeyEvent.VK_DOWN:
                selectNext();
                break;
            case KeyEvent.VK_ENTER:
                if (selectionListener != null) {
                    selectionListener.onModeSelected(GAME_MODES[selectedIndex]);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                if (navigationListener != null) {
                    navigationListener.onBack();
                }
                break;
        }
        repaint();
    }

    public void selectNext() {
        selectedIndex = (selectedIndex + 1) % GAME_MODES.length;
        repaint();
    }

    public void selectPrevious() {
        selectedIndex = (selectedIndex - 1 + GAME_MODES.length) % GAME_MODES.length;
        repaint();
    }

    public String getSelectedMode() {
        return GAME_MODES[selectedIndex];
    }

    public void setSelectionListener(MenuSelectionListener listener) {
        this.selectionListener = listener;
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
        String title = "SÉLECTION DU MODE DE JEU";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 5);

        // Modes de jeu - taille agrandie
        int startY = height / 2 - 50;
        int spacing = 150;

        for (int i = 0; i < GAME_MODES.length; i++) {
            int y = startY + i * spacing;

            // Mode
            if (i == selectedIndex) {
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 52));
                g2d.drawString(">", width / 2 - 300, y);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 44));
            }

            fm = g2d.getFontMetrics();
            int modeWidth = fm.stringWidth(GAME_MODES[i]);
            g2d.drawString(GAME_MODES[i], (width - modeWidth) / 2, y);

            // Description - taille agrandie
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.ITALIC, 28));
            fm = g2d.getFontMetrics();
            int descWidth = fm.stringWidth(MODE_DESCRIPTIONS[i]);
            g2d.drawString(MODE_DESCRIPTIONS[i], (width - descWidth) / 2, y + 45);
        }

        // Instructions - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instructions = "↑↓ pour naviguer, ENTRÉE pour sélectionner, ÉCHAP pour retour";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
    }
}
