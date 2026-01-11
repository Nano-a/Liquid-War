package fr.uparis.informatique.cpoo5.liquidwar.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Panel de jeu pour Liquid War
 * Architecture MVC - View
 */
public class GamePanel extends JPanel implements KeyListener, MouseMotionListener {
    private int playerX = 400;
    private int playerY = 300;
    private int playerSize = 40;
    private int targetX = 400;
    private int targetY = 300;
    
    public GamePanel() {
        setFocusable(true);
        addKeyListener(this);
        addMouseMotionListener(this);
        setBackground(new Color(0, 50, 100));
        requestFocusInWindow();
    }
    
    public void update() {
        // Déplacement fluide vers la cible
        int dx = targetX - playerX;
        int dy = targetY - playerY;
        double distance = Math.sqrt(dx*dx + dy*dy);
        
        if (distance > 5) {
            playerX += (int)(dx * 0.1);
            playerY += (int)(dy * 0.1);
        }
        
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fond dégradé
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(0, 30, 60), 
            0, getHeight(), new Color(0, 0, 30)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Grille
        g2d.setColor(new Color(50, 50, 80, 100));
        for (int i = 0; i < getWidth(); i += 50) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 50) {
            g2d.drawLine(0, i, getWidth(), i);
        }
        
        // Ligne vers la cible
        g2d.setColor(new Color(100, 150, 255, 100));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                                      0, new float[]{10, 5}, 0));
        g2d.drawLine(playerX, playerY, targetX, targetY);
        
        // Cible
        g2d.setColor(new Color(255, 255, 100, 150));
        g2d.fillOval(targetX - 8, targetY - 8, 16, 16);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawOval(targetX - 8, targetY - 8, 16, 16);
        
        // Joueur (pixel liquide)
        int[] xPoints = new int[8];
        int[] yPoints = new int[8];
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8;
            xPoints[i] = playerX + (int)(Math.cos(angle) * playerSize/2);
            yPoints[i] = playerY + (int)(Math.sin(angle) * playerSize/2);
        }
        
        // Ombre
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillPolygon(xPoints, yPoints, 8);
        
        // Corps
        GradientPaint playerGradient = new GradientPaint(
            playerX - playerSize/2, playerY - playerSize/2, new Color(150, 200, 255),
            playerX + playerSize/2, playerY + playerSize/2, new Color(50, 100, 200)
        );
        g2d.setPaint(playerGradient);
        g2d.fillPolygon(xPoints, yPoints, 8);
        
        // Contour
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawPolygon(xPoints, yPoints, 8);
        
        // HUD
        drawHUD(g2d);
    }
    
    private void drawHUD(Graphics2D g2d) {
        // Fond semi-transparent
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), 80);
        g2d.fillRect(0, getHeight() - 60, getWidth(), 60);
        
        // Titre
        g2d.setColor(new Color(100, 200, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("LIQUID WAR", 20, 35);
        
        // Info
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("Architecture MVC - Demo", 20, 55);
        
        // Instructions
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("🖱 Déplacez la souris pour contrôler votre armée liquide", 20, getHeight() - 35);
        g2d.drawString("⌨ ESC pour retourner au menu  |  ↑↓←→ pour déplacer", 20, getHeight() - 15);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int speed = 20;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) targetX -= speed;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) targetX += speed;
        if (e.getKeyCode() == KeyEvent.VK_UP) targetY -= speed;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) targetY += speed;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            SwingUtilities.getWindowAncestor(this).dispose();
            fr.uparis.informatique.cpoo5.liquidwar.controller.Main.main(new String[0]);
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        targetX = e.getX();
        targetY = e.getY();
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
}

