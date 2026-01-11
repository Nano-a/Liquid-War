package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panneau d'attente des joueurs avant le début de la partie réseau.
 * 
 * <p>Affiche le nombre de joueurs connectés et attend que le minimum soit atteint.
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class WaitingRoomPanel extends JPanel {
    
    private int connectedPlayers = 1; // L'hôte compte comme 1
    private int minPlayers = 2;
    private boolean isServer;
    private String serverAddress;
    private int serverPort;
    private boolean gameStarted = false;
    
    private WaitingRoomListener listener;
    
    // Animation
    private int dotCount = 0;
    private javax.swing.Timer animationTimer;
    
    /**
     * Interface pour les événements du lobby.
     */
    public interface WaitingRoomListener {
        void onStartGame();
        void onCancel();
    }
    
    /**
     * Crée un nouveau panneau d'attente.
     * 
     * @param isServer true si c'est le serveur
     * @param serverAddress Adresse du serveur
     * @param serverPort Port du serveur
     * @param minPlayers Nombre minimum de joueurs
     */
    public WaitingRoomPanel(boolean isServer, String serverAddress, int serverPort, int minPlayers) {
        this.isServer = isServer;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.minPlayers = minPlayers;
        
        setBackground(Color.BLACK);
        setFocusable(true);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });
        
        // Animation des points
        animationTimer = new javax.swing.Timer(500, e -> {
            dotCount = (dotCount + 1) % 4;
            repaint();
        });
        animationTimer.start();
    }
    
    /**
     * Définit le listener.
     */
    public void setListener(WaitingRoomListener listener) {
        this.listener = listener;
    }
    
    /**
     * Met à jour le nombre de joueurs connectés.
     */
    public void setConnectedPlayers(int count) {
        this.connectedPlayers = count;
        repaint();
        checkStartCondition();
    }
    
    /**
     * Met à jour les informations du lobby (reçu du serveur).
     * 
     * @param connectedPlayers Nombre de joueurs connectés
     * @param minPlayers Nombre minimum de joueurs requis
     */
    public void updateLobbyInfo(int connectedPlayers, int minPlayers) {
        this.connectedPlayers = connectedPlayers;
        this.minPlayers = minPlayers;
        repaint();
        checkStartCondition();
    }
    
    /**
     * Vérifie si les conditions de démarrage sont remplies.
     */
    private void checkStartCondition() {
        // Vérifier si on peut démarrer
        if (connectedPlayers >= minPlayers && !gameStarted) {
            // En mode serveur, on peut démarrer manuellement ou automatiquement
            // Pour l'instant, on démarre automatiquement
            if (isServer) {
                gameStarted = true;
                if (listener != null) {
                    // Petit délai pour que l'affichage se mette à jour
                    javax.swing.Timer startTimer = new javax.swing.Timer(1000, evt -> {
                        listener.onStartGame();
                    });
                    startTimer.setRepeats(false);
                    startTimer.start();
                }
            }
        }
    }
    
    /**
     * Gère les touches du clavier.
     */
    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_ESCAPE:
                if (listener != null) {
                    listener.onCancel();
                }
                break;
                
            case KeyEvent.VK_ENTER:
                // Le serveur peut forcer le démarrage si au moins 2 joueurs
                if (isServer && connectedPlayers >= 2 && !gameStarted) {
                    gameStarted = true;
                    if (listener != null) {
                        listener.onStartGame();
                    }
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
        
        // Titre
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        FontMetrics fm = g2d.getFontMetrics();
        String title = isServer ? "SALLE D'ATTENTE - SERVEUR" : "CONNEXION AU SERVEUR";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 100);
        
        // Adresse du serveur
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String address = serverAddress + ":" + serverPort;
        fm = g2d.getFontMetrics();
        int addressWidth = fm.stringWidth(address);
        g2d.drawString(address, (width - addressWidth) / 2, 140);
        
        // Statut de connexion
        int centerY = height / 2 - 50;
        
        // Nombre de joueurs
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String playerCount = connectedPlayers + " / " + minPlayers;
        fm = g2d.getFontMetrics();
        int countWidth = fm.stringWidth(playerCount);
        g2d.drawString(playerCount, (width - countWidth) / 2, centerY);
        
        // Label
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String label = "Joueurs connectés";
        fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        g2d.drawString(label, (width - labelWidth) / 2, centerY + 40);
        
        // Message d'attente avec animation
        String dots = ".".repeat(dotCount);
        String waitMessage;
        
        if (connectedPlayers >= minPlayers) {
            g2d.setColor(Color.GREEN);
            waitMessage = gameStarted ? "Démarrage de la partie..." : "Prêt à jouer !";
        } else {
            g2d.setColor(Color.YELLOW);
            int remaining = minPlayers - connectedPlayers;
            waitMessage = "En attente de " + remaining + " joueur(s)" + dots;
        }
        
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g2d.getFontMetrics();
        int msgWidth = fm.stringWidth(waitMessage);
        g2d.drawString(waitMessage, (width - msgWidth) / 2, centerY + 100);
        
        // Liste des joueurs avec couleurs d'équipe
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        int listY = centerY + 160;
        
        // Couleurs des équipes : Bleu, Rouge, Vert, Jaune
        Color[] teamColors = {
            new Color(100, 150, 255), // Bleu
            new Color(255, 100, 100), // Rouge
            new Color(100, 255, 100), // Vert
            new Color(255, 255, 100)  // Jaune
        };
        String[] teamNames = {"Bleu", "Rouge", "Vert", "Jaune"};
        
        for (int i = 0; i < connectedPlayers; i++) {
            String playerName = "Joueur " + (i + 1) + " - Équipe " + teamNames[i];
            if (i == 0 && isServer) {
                playerName += " (Vous - Hôte)";
            } else if (!isServer && i == connectedPlayers - 1) {
                playerName += " (Vous)";
            }
            
            // Couleur de l'équipe (Bleu, Rouge, Vert, Jaune)
            Color teamColor = teamColors[i % teamColors.length];
            g2d.setColor(teamColor);
            g2d.fillOval(width / 2 - 180, listY + i * 30 - 15, 20, 20);
            
            g2d.setColor(Color.WHITE);
            g2d.drawString(playerName, width / 2 - 150, listY + i * 30);
        }
        
        // Instructions en bas
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
        String instructions;
        if (isServer && connectedPlayers >= 2) {
            instructions = "ENTRÉE pour démarrer maintenant, ÉCHAP pour annuler";
        } else {
            instructions = "ÉCHAP pour annuler";
        }
        
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 50);
    }
    
    /**
     * Arrête l'animation.
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}

