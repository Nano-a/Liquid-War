package fr.uparis.informatique.cpoo5.liquidwar.view.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Panneau de sélection de la carte (map).
 * 
 * Affiche une grille 3x2 avec :
 * - Map aléatoire (première case)
 * - 5 maps spécifiques : 2d, puckman, bubbles, tiles, tag, village
 */
public class MapSelectionMenuPanel extends JPanel {

    private int selectedIndex = 0; // 0-5 (6 cases)
    private static final int GRID_COLS = 3;
    private static final int GRID_ROWS = 2;
    private static final int TOTAL_MAPS = 6;

    // Noms des maps (la première est "aléatoire")
    private static final String[] MAP_NAMES = {
            "ALÉATOIRE",
            "2D",
            "PUCKMAN",
            "BUBBLES",
            "TILES",
            "VILLAGE"
    };

    // Noms de fichiers pour les maps (null pour aléatoire)
    private static final String[] MAP_FILES = {
            null, // Aléatoire
            "2d",
            "puckman",
            "bubbles",
            "tiles",
            "village"
    };

    private MapSelectionListener selectionListener;
    private MenuNavigationListener navigationListener;

    // Images des maps (chargées depuis "Screen de Map")
    private BufferedImage[] mapImages = new BufferedImage[TOTAL_MAPS];
    // Images pré-redimensionnées pour éviter le délai lors du survol
    private Image[] scaledMapImages = new Image[TOTAL_MAPS];
    private boolean imagesLoaded = false;
    // Dimensions des images mises en cache (pour éviter de recalculer si les
    // dimensions n'ont pas changé)
    private int cachedImgWidth = 0;
    private int cachedImgHeight = 0;

    // Positions des cases pour détecter les clics
    private int gridX, gridY, cellWidth, cellHeight, cellPadding;

    public interface MapSelectionListener {
        void onMapSelected(String mapName);
    }

    public interface MenuNavigationListener {
        void onBack();
    }

    public MapSelectionMenuPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        // Ajouter les listeners pour les clics et le survol de souris
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getPoint());
            }
        });

        // Charger les images
        loadMapImages();
    }

    /**
     * Charge les images des maps depuis les ressources
     * (src/main/resources/map-screens)
     */
    private void loadMapImages() {
        // Noms des fichiers d'images (null pour aléatoire)
        String[] imageFiles = {
                null, // Aléatoire - pas d'image
                "2d.png",
                "puckman.png",
                "bubbles.png",
                "tiles.png",
                "village.png"
        };

        try {
            for (int i = 0; i < TOTAL_MAPS; i++) {
                if (imageFiles[i] != null) {
                    // Charger depuis les ressources
                    String resourcePath = "/map-screens/" + imageFiles[i];
                    InputStream imageStream = getClass().getResourceAsStream(resourcePath);

                    if (imageStream != null) {
                        mapImages[i] = ImageIO.read(imageStream);
                        imageStream.close();
                        System.out.println("✓ Image chargée depuis les ressources: " + imageFiles[i]);
                    } else {
                        // Fallback : essayer depuis le système de fichiers (pour compatibilité)
                        String basePath = System.getProperty("user.dir");
                        String[] possiblePaths = {
                                basePath + "/Screen de Map",
                                basePath + "/../Screen de Map",
                                "/home/ajinou/Bureau/Projet CPOO/Screen de Map"
                        };

                        boolean loaded = false;
                        for (String path : possiblePaths) {
                            File imageFile = new File(path, imageFiles[i]);
                            if (imageFile.exists()) {
                                mapImages[i] = ImageIO.read(imageFile);
                                System.out.println("✓ Image chargée depuis le système de fichiers: " + imageFiles[i]);
                                loaded = true;
                                break;
                            }
                        }

                        if (!loaded) {
                            System.err.println("⚠️ Image non trouvée: " + imageFiles[i]);
                        }
                    }
                }
                // Pour "aléatoire" (index 0), ne pas charger d'image - on garde null
            }
            imagesLoaded = true;
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images de maps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère le mouvement de la souris pour mettre à jour la sélection
     */
    private void handleMouseMove(Point point) {
        // Calculer quelle case est survolée
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = row * GRID_COLS + col;
                if (index >= TOTAL_MAPS)
                    break;

                int x = gridX + col * cellWidth;
                int y = gridY + row * cellHeight;

                // Vérifier si la souris est dans cette case
                if (point.x >= x + cellPadding && point.x < x + cellWidth - cellPadding &&
                        point.y >= y + cellPadding && point.y < y + cellHeight - cellPadding) {
                    if (selectedIndex != index) {
                        selectedIndex = index;
                        repaint();
                    }
                    setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    return;
                }
            }
        }
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Gère les clics de souris sur les cases
     */
    private void handleMouseClick(Point point) {
        // Calculer quelle case a été cliquée
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = row * GRID_COLS + col;
                if (index >= TOTAL_MAPS)
                    break;

                int x = gridX + col * cellWidth;
                int y = gridY + row * cellHeight;

                // Vérifier si le clic est dans cette case
                if (point.x >= x + cellPadding && point.x < x + cellWidth - cellPadding &&
                        point.y >= y + cellPadding && point.y < y + cellHeight - cellPadding) {
                    // Sélectionner cette map
                    selectedIndex = index;
                    repaint();

                    // Sélectionner la map
                    if (selectionListener != null) {
                        String mapName = MAP_FILES[selectedIndex];
                        if (mapName == null) {
                            // Map aléatoire : choisir une des 5 autres
                            Random rand = new Random();
                            mapName = MAP_FILES[1 + rand.nextInt(5)];
                        }
                        selectionListener.onMapSelected(mapName);
                    }
                    return;
                }
            }
        }
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                moveSelection(0, -1);
                break;
            case KeyEvent.VK_DOWN:
                moveSelection(0, 1);
                break;
            case KeyEvent.VK_LEFT:
                moveSelection(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
                moveSelection(1, 0);
                break;
            case KeyEvent.VK_ENTER:
                if (selectionListener != null) {
                    String mapName = MAP_FILES[selectedIndex];
                    if (mapName == null) {
                        // Map aléatoire : choisir une des 5 autres
                        Random rand = new Random();
                        mapName = MAP_FILES[1 + rand.nextInt(5)];
                    }
                    selectionListener.onMapSelected(mapName);
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

    private void moveSelection(int dx, int dy) {
        int currentRow = selectedIndex / GRID_COLS;
        int currentCol = selectedIndex % GRID_COLS;

        int newRow = currentRow + dy;
        int newCol = currentCol + dx;

        // Gérer les bordures
        if (newRow < 0)
            newRow = GRID_ROWS - 1;
        if (newRow >= GRID_ROWS)
            newRow = 0;
        if (newCol < 0)
            newCol = GRID_COLS - 1;
        if (newCol >= GRID_COLS)
            newCol = 0;

        selectedIndex = newRow * GRID_COLS + newCol;
    }

    public void setSelectionListener(MapSelectionListener listener) {
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

        // Titre
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 64));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "SÉLECTION DE LA CARTE";
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 8);

        // Calculer la taille et position de la grille
        int gridWidth = width * 3 / 4;
        int gridHeight = height * 3 / 5;
        gridX = (width - gridWidth) / 2;
        gridY = height / 4 + 50;

        cellWidth = gridWidth / GRID_COLS;
        cellHeight = gridHeight / GRID_ROWS;
        cellPadding = 20;

        // Dessiner la grille
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = row * GRID_COLS + col;
                if (index >= TOTAL_MAPS)
                    break;

                int x = gridX + col * cellWidth;
                int y = gridY + row * cellHeight;

                boolean isSelected = (selectedIndex == index);

                // Couleur de la case
                Color boxColor = isSelected ? Color.YELLOW : Color.WHITE;
                Color fillColor = isSelected ? new Color(255, 255, 100, 30) : new Color(255, 255, 255, 10);

                int boxX = x + cellPadding;
                int boxY = y + cellPadding;
                int boxWidth = cellWidth - 2 * cellPadding;
                int boxHeight = cellHeight - 2 * cellPadding;

                // Dessiner la case avec bordures (style demandé)
                drawMapBox(g2d, boxX, boxY, boxWidth, boxHeight, boxColor, fillColor, isSelected);

                // Dessiner l'image de la map si disponible (sauf pour "aléatoire")
                if (imagesLoaded && index > 0 && mapImages[index] != null) {
                    int imgWidth = boxWidth - 10;
                    int imgHeight = boxHeight - 50; // Laisser de la place pour le nom
                    int imgX = boxX + (boxWidth - imgWidth) / 2;
                    int imgY = boxY + 5;

                    // Utiliser l'image pré-redimensionnée si disponible et de la bonne taille
                    Image scaledImage = scaledMapImages[index];
                    // Si les dimensions ont changé, recalculer toutes les images
                    if (cachedImgWidth != imgWidth || cachedImgHeight != imgHeight) {
                        // Vider le cache pour forcer le recalcul
                        for (int i = 0; i < TOTAL_MAPS; i++) {
                            scaledMapImages[i] = null;
                        }
                        cachedImgWidth = imgWidth;
                        cachedImgHeight = imgHeight;
                    }
                    // Si l'image n'est pas encore en cache, la redimensionner
                    if (scaledImage == null) {
                        scaledImage = mapImages[index].getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
                        scaledMapImages[index] = scaledImage;
                    }
                    g2d.drawImage(scaledImage, imgX, imgY, null);
                }

                // Nom de la map (en bas de la case, sous le cadre)
                g2d.setColor(isSelected ? Color.YELLOW : Color.WHITE);
                g2d.setFont(new Font("Arial", isSelected ? Font.BOLD : Font.PLAIN, 24));
                fm = g2d.getFontMetrics();
                String mapName = MAP_NAMES[index];
                int nameWidth = fm.stringWidth(mapName);
                // Positionner le texte en dessous du cadre avec un espacement
                int textY = y + cellHeight - cellPadding + 25; // Descendre le texte sous le cadre
                g2d.drawString(mapName, x + (cellWidth - nameWidth) / 2, textY);
            }
        }

        // Instructions
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instructions = "↑↓←→ naviguer | Clic ou ENTRÉE sélectionner | ÉCHAP retour";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, height - 60);
    }

    /**
     * Dessine une case de map avec bordures dans le style demandé.
     * Style :
     * _ _ _
     * |_||_||_|
     * |_||_||_|
     */
    private void drawMapBox(Graphics2D g2d, int x, int y, int width, int height, Color borderColor,
            Color fillColor, boolean selected) {
        int borderWidth = selected ? 4 : 2;

        // Fond
        g2d.setColor(fillColor);
        g2d.fillRect(x, y, width, height);

        // Bordures (style demandé)
        g2d.setColor(borderColor);
        g2d.setStroke(new java.awt.BasicStroke(borderWidth));

        // Bordure supérieure
        g2d.drawLine(x, y, x + width, y);

        // Bordures latérales
        g2d.drawLine(x, y, x, y + height);
        g2d.drawLine(x + width, y, x + width, y + height);

        // Bordure inférieure
        g2d.drawLine(x, y + height, x + width, y + height);

        // Si sélectionné, ajouter un indicateur
        if (selected) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString(">", x - 30, y + height / 2 + 15);
        }
    }
}
