package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import fr.uparis.informatique.cpoo5.liquidwar.network.NetworkProtocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panneau pour se connecter à un serveur de jeu.
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class ClientConnectPanel extends JPanel {
    
    private String hostname = "localhost";
    private int port = NetworkProtocol.DEFAULT_PORT;
    private int selectedField = 0; // 0=hostname, 1=port, 2=connect, 3=back
    private ClientConnectListener listener;
    private boolean editingHostname = false;
    private StringBuilder hostnameBuilder;
    
    /**
     * Interface pour les événements du panneau.
     */
    public interface ClientConnectListener {
        void onConnect(String hostname, int port);
        void onBack();
    }
    
    /**
     * Crée un nouveau panneau de connexion client.
     */
    public ClientConnectPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        hostnameBuilder = new StringBuilder(hostname);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode(), e.getKeyChar());
            }
        });
    }
    
    /**
     * Définit le listener.
     * 
     * @param listener Listener à notifier
     */
    public void setListener(ClientConnectListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gère les touches du clavier.
     */
    private void handleKeyPress(int keyCode, char keyChar) {
        // Mode édition du hostname
        if (editingHostname) {
            if (keyCode == KeyEvent.VK_ENTER) {
                hostname = hostnameBuilder.toString();
                editingHostname = false;
                repaint();
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                hostnameBuilder = new StringBuilder(hostname);
                editingHostname = false;
                repaint();
            } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                if (hostnameBuilder.length() > 0) {
                    hostnameBuilder.deleteCharAt(hostnameBuilder.length() - 1);
                    repaint();
                }
            } else if (Character.isLetterOrDigit(keyChar) || keyChar == '.' || keyChar == '-') {
                if (hostnameBuilder.length() < 50) {
                    hostnameBuilder.append(keyChar);
                    repaint();
                }
            }
            return;
        }
        
        // Mode navigation
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
                if (selectedField == 1) {
                    port = Math.max(1024, port - 1);
                    repaint();
                }
                break;
                
            case KeyEvent.VK_RIGHT:
                if (selectedField == 1) {
                    port = Math.min(65535, port + 1);
                    repaint();
                }
                break;
                
            case KeyEvent.VK_ENTER:
                if (selectedField == 0) {
                    editingHostname = true;
                    hostnameBuilder = new StringBuilder(hostname);
                    repaint();
                } else if (selectedField == 2 && listener != null) {
                    listener.onConnect(hostname, port);
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
        String title = "REJOINDRE UN SERVEUR";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 5);
        
        // Champs de configuration - taille agrandie
        int startY = height / 2 - 100;
        int spacing = 130;
        
        // Hostname
        String hostValue = editingHostname ? hostnameBuilder.toString() + "_" : hostname;
        drawHostnameField(g2d, "Serveur:", hostValue, 0, startY, width);
        
        // Port
        drawConfigField(g2d, "Port:", String.valueOf(port), 1, startY + spacing, width);
        
        // Bouton Connexion
        drawButton(g2d, "CONNEXION", 2, startY + spacing * 2 + 50, width);
        
        // Bouton Retour
        drawButton(g2d, "RETOUR", 3, startY + spacing * 2 + 150, width);
        
        // Instructions - taille agrandie
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instructions = editingHostname ? 
                "Tapez l'adresse, ENTRÉE pour valider, ÉCHAP pour annuler" :
                "↑↓ pour naviguer, ←→ pour modifier port, ENTRÉE pour sélectionner";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 80);
        
        // Info - taille agrandie
        if (!editingHostname) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.ITALIC, 24));
            String info = "Connexion à " + hostname + ":" + port;
            fm = g2d.getFontMetrics();
            int infoWidth = fm.stringWidth(info);
            g2d.drawString(info, (width - infoWidth) / 2, height - 130);
        }
    }
    
    /**
     * Dessine le champ hostname (avec mode édition).
     */
    private void drawHostnameField(Graphics2D g2d, String label, String value, int index, int y, int width) {
        boolean selected = (selectedField == index);
        
        // Indicateur de sélection
        if (selected) {
            g2d.setColor(editingHostname ? Color.GREEN : Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 44));
            g2d.drawString(editingHostname ? "✎" : ">", width / 2 - 350, y);
        }
        
        // Label et valeur - taille agrandie
        g2d.setColor(editingHostname ? Color.GREEN : (selected ? Color.YELLOW : Color.WHITE));
        g2d.setFont(new Font("Arial", selected ? Font.BOLD : Font.PLAIN, 44));
        FontMetrics fm = g2d.getFontMetrics();
        
        String text = label + " " + value;
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, y);
        
        // Indication pour éditer - taille agrandie
        if (selected && !editingHostname) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.ITALIC, 24));
            String hint = "(ENTRÉE pour modifier)";
            fm = g2d.getFontMetrics();
            int hintWidth = fm.stringWidth(hint);
            g2d.drawString(hint, (width - hintWidth) / 2, y + 40);
        }
    }
    
    /**
     * Dessine un champ de configuration numérique.
     */
    private void drawConfigField(Graphics2D g2d, String label, String value, int index, int y, int width) {
        boolean selected = (selectedField == index);
        
        // Indicateur de sélection
        if (selected) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 44));
            g2d.drawString(">", width / 2 - 350, y);
        }
        
        // Label et valeur - taille agrandie
        g2d.setColor(selected ? Color.YELLOW : Color.WHITE);
        g2d.setFont(new Font("Arial", selected ? Font.BOLD : Font.PLAIN, 44));
        FontMetrics fm = g2d.getFontMetrics();
        
        String text = label + " ";
        if (selected) {
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
