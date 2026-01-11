package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import fr.uparis.informatique.cpoo5.liquidwar.network.NetworkProtocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panneau pour configurer et démarrer un serveur de jeu.
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class ServerSetupPanel extends JPanel {
    
    private int port = NetworkProtocol.DEFAULT_PORT;
    private int minPlayers = 2;
    private int selectedField = 0; // 0=port, 1=minPlayers, 2=start, 3=back
    private ServerSetupListener listener;
    
    private static final String[] FIELD_LABELS = {
        "Port",
        "Joueurs minimum",
        "DÉMARRER",
        "RETOUR"
    };
    
    /**
     * Interface pour les événements du panneau.
     */
    public interface ServerSetupListener {
        void onStartServer(int port, int minPlayers);
        void onBack();
    }
    
    /**
     * Crée un nouveau panneau de configuration serveur.
     */
    public ServerSetupPanel() {
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
     * Définit le listener.
     * 
     * @param listener Listener à notifier
     */
    public void setListener(ServerSetupListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gère les touches du clavier.
     */
    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                selectedField = (selectedField - 1 + 4) % 4;
                repaint();
                break;
                
            case KeyEvent.VK_DOWN:
                selectedField = (selectedField + 1) % 4;
                repaint();
                break;
                
            case KeyEvent.VK_LEFT:
                if (selectedField == 0) {
                    port = Math.max(1024, port - 1);
                } else if (selectedField == 1) {
                    minPlayers = Math.max(2, minPlayers - 1);
                }
                repaint();
                break;
                
            case KeyEvent.VK_RIGHT:
                if (selectedField == 0) {
                    port = Math.min(65535, port + 1);
                } else if (selectedField == 1) {
                    minPlayers = Math.min(NetworkProtocol.MAX_PLAYERS, minPlayers + 1);
                }
                repaint();
                break;
                
            case KeyEvent.VK_ENTER:
                if (selectedField == 2 && listener != null) {
                    listener.onStartServer(port, minPlayers);
                } else if (selectedField == 3 && listener != null) {
                    listener.onBack();
                }
                break;
                
            case KeyEvent.VK_ESCAPE:
                if (listener != null) {
                    listener.onBack();
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
        String title = "CRÉER UN SERVEUR";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 5);
        
        // Champs de configuration - taille agrandie
        int startY = height / 2 - 100;
        int spacing = 130;
        
        // Port
        drawConfigField(g2d, "Port:", String.valueOf(port), 0, startY, width, true);
        
        // Joueurs minimum
        drawConfigField(g2d, "Joueurs minimum:", String.valueOf(minPlayers), 1, startY + spacing, width, true);
        
        // Bouton Démarrer
        drawButton(g2d, "DÉMARRER", 2, startY + spacing * 2 + 50, width);
        
        // Bouton Retour
        drawButton(g2d, "RETOUR", 3, startY + spacing * 2 + 150, width);
        
        // Instructions - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instructions = "↑↓ pour naviguer, ←→ pour modifier, ENTRÉE pour valider, ÉCHAP pour retour";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
        
        // Info - taille agrandie
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.ITALIC, 24));
        String info = "Le serveur attendra " + minPlayers + " joueur(s) avant de commencer";
        fm = g2d.getFontMetrics();
        int infoWidth = fm.stringWidth(info);
        g2d.drawString(info, (width - infoWidth) / 2, height - 130);
    }
    
    /**
     * Dessine un champ de configuration.
     */
    private void drawConfigField(Graphics2D g2d, String label, String value, int index, int y, 
                                 int width, boolean editable) {
        boolean selected = (selectedField == index);
        
        // Indicateur de sélection
        if (selected) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 44));
            g2d.drawString(">", width / 2 - 350, y);
        }
        
        // Label - taille agrandie
        g2d.setColor(selected ? Color.YELLOW : Color.WHITE);
        g2d.setFont(new Font("Arial", selected ? Font.BOLD : Font.PLAIN, 44));
        FontMetrics fm = g2d.getFontMetrics();
        
        String text = label + " ";
        if (editable && selected) {
            text += "< " + value + " >";
        } else {
            text += value;
        }
        
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, y);
    }
    
    /**
     * Dessine un bouton.
     */
    private void drawButton(Graphics2D g2d, String text, int index, int y, int width) {
        boolean selected = (selectedField == index);
        
        if (selected) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 52));
            g2d.drawString(">", width / 2 - 250, y);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 44));
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, y);
    }
}
