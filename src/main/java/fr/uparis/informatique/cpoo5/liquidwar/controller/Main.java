package fr.uparis.informatique.cpoo5.liquidwar.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Classe principale pour lancer Liquid War
 * Architecture MVC - Controller
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  Liquid War Java - Version MVC");
        System.out.println("==========================================");
        System.out.println("");
        
        // Vérifier l'environnement graphique
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("ERREUR: Pas d'environnement graphique disponible");
            System.exit(1);
        }
        
        System.out.println("✓ Environnement graphique OK");
        System.out.println("✓ Architecture MVC chargée");
        System.out.println("Création de la fenêtre...");
        
        // Créer la fenêtre dans le thread EDT
        SwingUtilities.invokeLater(() -> {
            try {
                createMainMenu();
            } catch (Exception e) {
                System.err.println("ERREUR: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Garder le programme actif
        try {
            Thread.sleep(2000);
            System.out.println("Programme en cours d'exécution...");
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }
    
    private static void createMainMenu() {
        JFrame frame = new JFrame("Liquid War - Menu Principal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        // Panel personnalisé avec fond dégradé
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fond dégradé
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(20, 30, 60), 
                    0, getHeight(), new Color(0, 0, 30)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Titre
                g2d.setColor(new Color(100, 200, 255));
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                FontMetrics fm = g2d.getFontMetrics();
                String title = "LIQUID WAR";
                int titleWidth = fm.stringWidth(title);
                g2d.drawString(title, (getWidth() - titleWidth) / 2, 120);
                
                // Sous-titre
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                String subtitle = "Architecture MVC - Java Edition";
                int subtitleWidth = g2d.getFontMetrics().stringWidth(subtitle);
                g2d.drawString(subtitle, (getWidth() - subtitleWidth) / 2, 155);
                
                // Info structure
                g2d.setColor(new Color(150, 150, 150));
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                String info = "Model • View • Controller • Utils";
                int infoWidth = g2d.getFontMetrics().stringWidth(info);
                g2d.drawString(info, (getWidth() - infoWidth) / 2, 180);
            }
        };
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(200, 50, 50, 50));
        
        // Bouton Jouer
        JButton playButton = createStyledButton("▶ Jouer", new Color(50, 150, 50));
        playButton.addActionListener(e -> {
            frame.dispose();
            startGame();
        });
        
        // Bouton Options
        JButton optionsButton = createStyledButton("⚙ Options", new Color(50, 100, 150));
        optionsButton.addActionListener(e -> {
            showOptionsDialog(frame);
        });
        
        // Bouton À propos
        JButton aboutButton = createStyledButton("ℹ À propos", new Color(100, 50, 150));
        aboutButton.addActionListener(e -> {
            showAboutDialog(frame);
        });
        
        // Bouton Quitter
        JButton quitButton = createStyledButton("✖ Quitter", new Color(150, 50, 50));
        quitButton.addActionListener(e -> System.exit(0));
        
        panel.add(Box.createVerticalGlue());
        panel.add(centerComponent(playButton));
        panel.add(Box.createVerticalStrut(15));
        panel.add(centerComponent(optionsButton));
        panel.add(Box.createVerticalStrut(15));
        panel.add(centerComponent(aboutButton));
        panel.add(Box.createVerticalStrut(15));
        panel.add(centerComponent(quitButton));
        panel.add(Box.createVerticalGlue());
        
        frame.add(panel);
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
        
        System.out.println("✓ Fenêtre créée et affichée !");
        System.out.println("  Taille: " + frame.getWidth() + "x" + frame.getHeight());
    }
    
    private static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 60));
        button.setMaximumSize(new Dimension(300, 60));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Effet hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private static JPanel centerComponent(JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.add(Box.createHorizontalGlue());
        panel.add(component);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }
    
    private static void showOptionsDialog(JFrame parent) {
        JOptionPane.showMessageDialog(parent,
            "Options de jeu à venir...\n\n" +
            "• Difficulté\n" +
            "• Nombre de joueurs\n" +
            "• Taille de la carte\n" +
            "• Vitesse du jeu",
            "Options",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static void showAboutDialog(JFrame parent) {
        JOptionPane.showMessageDialog(parent,
            "LIQUID WAR - Architecture MVC\n\n" +
            "Version: 1.0 (Java Edition)\n" +
            "Architecture: Model-View-Controller\n\n" +
            "Structure:\n" +
            "  • Model: 37 fichiers (Entités, logique)\n" +
            "  • View: 23 fichiers (Interface graphique)\n" +
            "  • Controller: 33 fichiers (Contrôle)\n" +
            "  • Utils: 51 fichiers (Utilitaires)\n\n" +
            "Total: 144 fichiers Java organisés\n\n" +
            "© 1998-2025 Christian Mauduit (Original C)\n" +
            "Porté en Java avec architecture MVC",
            "À propos",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static void startGame() {
        SwingUtilities.invokeLater(() -> {
            JFrame gameFrame = new JFrame("Liquid War - En jeu");
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setSize(1024, 768);
            gameFrame.setLocationRelativeTo(null);
            
            fr.uparis.informatique.cpoo5.liquidwar.view.GamePanel gamePanel = 
                new fr.uparis.informatique.cpoo5.liquidwar.view.GamePanel();
            gameFrame.add(gamePanel);
            gameFrame.setVisible(true);
            gameFrame.toFront();
            gameFrame.requestFocus();
            
            // Boucle de jeu (60 FPS)
            Timer timer = new Timer(16, e -> gamePanel.update());
            timer.start();
            
            System.out.println("✓ Jeu lancé !");
        });
    }
}
