package fr.uparis.informatique.cpoo5.liquidwar.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;

/**
 * Chargeur de cartes simplifié pour Liquid War
 */
public class MapLoader {

    public static int[][] loadMapFromBMP(String path, int width, int height) {
        GameLogger logger = GameLogger.getInstance();
        logger.debug("==========================================");
        logger.debug("DEBUG MapLoader - Tentative de chargement");
        logger.debug("==========================================");
        logger.debug("Chemin demandé: %s", path);
        logger.debug("Dimensions: %dx%d", width, height);

        int[][] map = new int[height][width];

        try {
            File file = new File(path);
            logger.debug("Chemin absolu: %s", file.getAbsolutePath());
            logger.debug("Fichier existe? %s", file.exists());
            logger.debug("Fichier est lisible? %s", file.canRead());

            if (file.exists()) {
                logger.info("✓ Fichier trouvé ! Chargement en cours...");
                BufferedImage image = ImageIO.read(file);

                if (image == null) {
                    logger.error("✗ Erreur: ImageIO.read() a retourné null");
                    return createDefaultMap(width, height);
                }

                logger.info("✓ Image chargée: %dx%d", image.getWidth(), image.getHeight());

                int wallCount = 0;
                int freeCount = 0;

                for (int y = 0; y < height && y < image.getHeight(); y++) {
                    for (int x = 0; x < width && x < image.getWidth(); x++) {
                        int rgb = image.getRGB(x, y);
                        Color color = new Color(rgb);

                        // Convertir en niveau de gris pour déterminer si c'est un mur ou non
                        int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                        // IMPORTANT : GameArea attend -1 pour les obstacles !
                        map[y][x] = (gray < 128) ? -1 : 0; // -1 = mur (obstacle), 0 = libre

                        if (map[y][x] == -1)
                            wallCount++;
                        else
                            freeCount++;
                    }
                }

                logger.info("✓ Carte chargée avec succès!");
                logger.info("  - Murs (pixels noirs): %d", wallCount);
                logger.info("  - Zones libres (pixels blancs): %d", freeCount);
                logger.info("  - Total pixels: %d", wallCount + freeCount);
                logger.debug("==========================================");
                return map;
            } else {
                logger.error("✗ Fichier NON TROUVÉ: %s", path);
            }
        } catch (Exception e) {
            GameLogger.getInstance().error("✗ ERREUR lors du chargement de la carte:", e);
            GameLogger.getInstance().error("  Type: %s", e.getClass().getName());
            GameLogger.getInstance().error("  Message: %s", e.getMessage());
        }

        // Carte par défaut si échec du chargement
        GameLogger.getInstance().warn("→ Création d'une carte par défaut...");
        return createDefaultMap(width, height);
    }

    public static String readMapName(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(file);
                if (scanner.hasNextLine()) {
                    String name = scanner.nextLine();
                    scanner.close();
                    return name;
                }
                scanner.close();
            }
        } catch (Exception e) {
            // Ignorer
        }
        return "Liquid War Map";
    }

    private static int[][] createDefaultMap(int width, int height) {
        GameLogger logger = GameLogger.getInstance();
        logger.debug("==========================================");
        logger.debug("DEBUG - Création carte par défaut");
        logger.debug("==========================================");

        int[][] map = new int[height][width];
        int wallCount = 0;

        // Créer une carte simple avec des murs sur les bords
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Murs sur les bords
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    map[y][x] = -1; // -1 = obstacle (attendu par GameArea)
                    wallCount++;
                }
                // Quelques obstacles au centre
                else if ((x == width / 2 && y > height / 4 && y < 3 * height / 4) ||
                        (y == height / 2 && x > width / 4 && x < 3 * width / 4)) {
                    map[y][x] = -1; // -1 = obstacle
                    wallCount++;
                } else {
                    map[y][x] = 0; // 0 = zone libre
                }
            }
        }

        logger.info("✓ Carte par défaut créée");
        logger.info("  - Dimensions: %dx%d", width, height);
        logger.info("  - Murs: %d", wallCount);
        logger.info("  - Zones libres: %d", width * height - wallCount);
        logger.debug("==========================================");
        return map;
    }
}
