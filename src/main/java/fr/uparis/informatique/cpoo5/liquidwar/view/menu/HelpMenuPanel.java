package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panneau d'aide du jeu.
 * 
 * Affiche les instructions et informations sur le jeu
 */
public class HelpMenuPanel extends JPanel {
    
    private int currentSection = 0;
    
    private static final String[][] HELP_SECTIONS = {
        {
            "COMMENT JOUER",
            "",
            "• Déplacez votre curseur avec les flèches ou la souris",
            "• Vos combattants (bleus) suivent votre curseur",
            "• Battez l'adversaire en éliminant tous ses combattants",
            "• Les combattants perdent de la vie en combat",
            "",
            "Appuyez sur ← ou → pour voir plus d'informations"
        },
        {
            "STRATÉGIE",
            "",
            "• Divisez vos forces pour attaquer sur plusieurs fronts",
            "• Regroupez-vous pour des attaques massives",
            "• Évitez les obstacles pour ne pas perdre de combattants",
            "• Surveillez le nombre de combattants de chaque équipe",
            "",
            "Le jeu se gagne par la tactique, pas seulement la force !"
        },
        {
            "À PROPOS",
            "",
            "Liquid War - Version Java UPC",
            "",
            "Inspiré du jeu original Liquid War (1998)",
            "Développé dans le cadre du cours CPOO5",
            "",
            "Architecture MVC + Services",
            "Code organisé et professionnel",
            "",
            "Appuyez sur ÉCHAP pour retourner au menu"
        }
    };
    
    private MenuNavigationListener navigationListener;
    
    public interface MenuNavigationListener {
        void onBack();
    }
    
    public HelpMenuPanel() {
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
            case KeyEvent.VK_LEFT:
                currentSection = (currentSection - 1 + HELP_SECTIONS.length) % HELP_SECTIONS.length;
                repaint();
                break;
            case KeyEvent.VK_RIGHT:
                currentSection = (currentSection + 1) % HELP_SECTIONS.length;
                repaint();
                break;
            case KeyEvent.VK_ESCAPE:
                if (navigationListener != null) {
                    navigationListener.onBack();
                }
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
        
        // Titre principal - taille agrandie
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        FontMetrics fm = g2d.getFontMetrics();
        String mainTitle = "AIDE";
        int titleWidth = fm.stringWidth(mainTitle);
        g2d.drawString(mainTitle, (width - titleWidth) / 2, height / 6);
        
        // Indicateur de section - taille agrandie
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String sectionIndicator = "Section " + (currentSection + 1) + "/" + HELP_SECTIONS.length;
        fm = g2d.getFontMetrics();
        int indicatorWidth = fm.stringWidth(sectionIndicator);
        g2d.drawString(sectionIndicator, (width - indicatorWidth) / 2, height / 6 + 50);
        
        // Contenu de la section - taille agrandie
        String[] lines = HELP_SECTIONS[currentSection];
        int startY = height / 3;
        int lineSpacing = 65;
        
        for (int i = 0; i < lines.length; i++) {
            if (i == 0) {
                // Titre de la section
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 36));
            }
            
            fm = g2d.getFontMetrics();
            int lineWidth = fm.stringWidth(lines[i]);
            g2d.drawString(lines[i], (width - lineWidth) / 2, startY + i * lineSpacing);
        }
        
        // Navigation - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String nav = "← Section précédente | Section suivante → | ÉCHAP retour";
        fm = g2d.getFontMetrics();
        int navWidth = fm.stringWidth(nav);
        g2d.drawString(nav, (width - navWidth) / 2, height - 80);
    }
}
