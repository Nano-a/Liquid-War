package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panneau de menu pour le multijoueur en réseau.
 * 
 * <p>Permet de choisir entre créer une partie (serveur) ou rejoindre
 * une partie existante (client).
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class NetworkMenuPanel extends JPanel {
    
    private static final String[] MENU_ITEMS = {
        "CRÉER UNE PARTIE",
        "REJOINDRE UNE PARTIE",
        "RETOUR"
    };
    
    private static final String[] DESCRIPTIONS = {
        "Hébergez une partie et invitez des joueurs",
        "Rejoignez une partie existante",
        "Retourner au menu précédent"
    };
    
    private int selectedIndex = 0;
    private SelectionListener listener;
    
    /**
     * Interface pour écouter les sélections de menu.
     */
    public interface SelectionListener {
        void onMenuItemSelected(String item);
    }
    
    /**
     * Crée un nouveau panneau de menu réseau.
     */
    public NetworkMenuPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });
    }
    
    /**
     * Définit le listener pour les sélections.
     * 
     * @param listener Listener à notifier
     */
    public void setSelectionListener(SelectionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gère les touches du clavier.
     */
    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                selectedIndex = (selectedIndex - 1 + MENU_ITEMS.length) % MENU_ITEMS.length;
                repaint();
                break;
                
            case KeyEvent.VK_DOWN:
                selectedIndex = (selectedIndex + 1) % MENU_ITEMS.length;
                repaint();
                break;
                
            case KeyEvent.VK_ENTER:
                if (listener != null) {
                    listener.onMenuItemSelected(MENU_ITEMS[selectedIndex]);
                }
                break;
                
            case KeyEvent.VK_ESCAPE:
                if (listener != null) {
                    listener.onMenuItemSelected("RETOUR");
                }
                break;
        }
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
        String title = "MULTIJOUEUR RÉSEAU";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 5);
        
        // Sous-titre - taille agrandie
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String subtitle = "Port par défaut: 14000";
        fm = g2d.getFontMetrics();
        int subtitleWidth = fm.stringWidth(subtitle);
        g2d.drawString(subtitle, (width - subtitleWidth) / 2, height / 5 + 50);
        
        // Menu - taille agrandie
        int startY = height / 2 - 50;
        int spacing = 150;
        
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            int y = startY + i * spacing;
            
            if (i == selectedIndex) {
                // Item sélectionné
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 52));
                g2d.drawString(">", width / 2 - 350, y);
            } else {
                // Item normal
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 44));
            }
            
            fm = g2d.getFontMetrics();
            int itemWidth = fm.stringWidth(MENU_ITEMS[i]);
            g2d.drawString(MENU_ITEMS[i], (width - itemWidth) / 2, y);
            
            // Description - taille agrandie
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.ITALIC, 28));
            fm = g2d.getFontMetrics();
            int descWidth = fm.stringWidth(DESCRIPTIONS[i]);
            g2d.drawString(DESCRIPTIONS[i], (width - descWidth) / 2, y + 45);
        }
        
        // Instructions en bas - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instructions = "↑↓ pour naviguer, ENTRÉE pour sélectionner, ÉCHAP pour retour";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
    }
    
    /**
     * Réinitialise la sélection.
     */
    public void resetSelection() {
        selectedIndex = 0;
        repaint();
    }
}
