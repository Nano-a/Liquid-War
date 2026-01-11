package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panneau du menu pause.
 * 
 * Affiché pendant le jeu quand on appuie sur le bouton Menu ou ÉCHAP.
 * Options : CONTINUER, RECOMMENCER, OPTIONS, QUITTER
 */
public class PauseMenuPanel extends JPanel {
    
    private int selectedIndex = 0;
    
    private static final String[] MENU_ITEMS = {
        "CONTINUER",
        "RECOMMENCER",
        "OPTIONS",
        "MENU PRINCIPAL",
        "QUITTER"
    };
    
    private MenuSelectionListener selectionListener;
    
    public interface MenuSelectionListener {
        void onMenuItemSelected(String item);
    }
    
    public PauseMenuPanel() {
        setOpaque(false); // Transparent pour voir le jeu en arrière-plan
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
                    selectionListener.onMenuItemSelected(MENU_ITEMS[selectedIndex]);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                if (selectionListener != null) {
                    selectionListener.onMenuItemSelected("CONTINUER");
                }
                break;
        }
        repaint();
    }
    
    public void selectNext() {
        selectedIndex = (selectedIndex + 1) % MENU_ITEMS.length;
        repaint();
    }
    
    public void selectPrevious() {
        selectedIndex = (selectedIndex - 1 + MENU_ITEMS.length) % MENU_ITEMS.length;
        repaint();
    }
    
    public void setSelectionListener(MenuSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Fond semi-transparent
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, width, height);
        
        // Titre
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "PAUSE";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 150);
        
        // Options du menu
        int startY = height / 2 - 50;
        int spacing = 70;
        
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            int y = startY + i * spacing;
            
            if (i == selectedIndex) {
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 32));
                g2d.drawString(">", width / 2 - 180, y);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 28));
            }
            
            fm = g2d.getFontMetrics();
            int itemWidth = fm.stringWidth(MENU_ITEMS[i]);
            g2d.drawString(MENU_ITEMS[i], (width - itemWidth) / 2, y);
        }
        
        // Instructions
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String instructions = "↑↓ pour naviguer, ENTRÉE pour sélectionner, ÉCHAP pour continuer";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 50);
    }
}

