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
import javax.swing.SwingUtilities;

/**
 * Panneau du menu principal.
 * 
 * Affiche les options principales : JOUER, OPTIONS, AIDE, QUITTER
 */
public class MainMenuPanel extends JPanel {

    private int selectedIndex = 0;

    private static final String[] MENU_ITEMS = {
            "JOUER",
            "OPTIONS",
            "AIDE",
            "QUITTER"
    };

    private static final String TITLE = "LIQUID WAR";

    // Listener pour les sélections
    private MenuSelectionListener selectionListener;

    public interface MenuSelectionListener {
        void onMenuItemSelected(String item);
    }

    public MainMenuPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        setRequestFocusEnabled(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        // Ajouter un listener pour s'assurer que le panel reçoit le focus quand il
        // devient visible
        addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                    // Le panel vient d'être affiché, demander le focus
                    SwingUtilities.invokeLater(() -> {
                        requestFocusInWindow();
                        if (!hasFocus()) {
                            requestFocus();
                        }
                    });
                }
            }
        });
    }

    /**
     * Gère les touches clavier
     */
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
                    selectionListener.onMenuItemSelected(MENU_ITEMS[selectedIndex]);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                // Échap sur le menu principal = Quitter
                if (selectionListener != null) {
                    selectionListener.onMenuItemSelected("QUITTER");
                }
                break;
        }
        repaint();
    }

    /**
     * Sélectionne l'option suivante
     */
    public void selectNext() {
        selectedIndex = (selectedIndex + 1) % MENU_ITEMS.length;
        repaint();
    }

    /**
     * Sélectionne l'option précédente
     */
    public void selectPrevious() {
        selectedIndex = (selectedIndex - 1 + MENU_ITEMS.length) % MENU_ITEMS.length;
        repaint();
    }

    /**
     * Obtient l'élément sélectionné
     */
    public String getSelectedItem() {
        return MENU_ITEMS[selectedIndex];
    }

    /**
     * Définit le listener de sélection
     */
    public void setSelectionListener(MenuSelectionListener listener) {
        this.selectionListener = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing pour un meilleur rendu du texte
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Titre - taille agrandie
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 96));
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(TITLE);
        g2d.drawString(TITLE, (width - titleWidth) / 2, height / 4);

        // Sous-titre
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.ITALIC, 32));
        fm = g2d.getFontMetrics();
        String subtitle = "Le jeu de stratégie liquide";
        int subtitleWidth = fm.stringWidth(subtitle);
        g2d.drawString(subtitle, (width - subtitleWidth) / 2, height / 4 + 60);

        // Options du menu - taille agrandie
        int startY = height / 2 + 50;
        int spacing = 100;

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            int y = startY + i * spacing;

            // Couleur et police selon sélection
            if (i == selectedIndex) {
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 56));

                // Indicateur de sélection
                g2d.drawString(">", width / 2 - 200, y);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 48));
            }

            fm = g2d.getFontMetrics();
            int itemWidth = fm.stringWidth(MENU_ITEMS[i]);
            g2d.drawString(MENU_ITEMS[i], (width - itemWidth) / 2, y);
        }

        // Instructions - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instructions = "Utilisez les flèches ↑↓ pour naviguer, ENTRÉE pour sélectionner";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
    }
}
