package fr.uparis.informatique.cpoo5.liquidwar.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour MapLoader.
 * 
 * Teste :
 * - Création de carte par défaut
 * - Lecture du nom de carte
 * - Gestion des fichiers inexistants
 */
class MapLoaderTest {

    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 50;

    @Test
    @DisplayName("createDefaultMap crée une carte valide")
    void testCreateDefaultMap() {
        int[][] map = MapLoader.loadMapFromBMP("nonexistent.bmp", MAP_WIDTH, MAP_HEIGHT);

        assertNotNull(map, "La carte ne doit pas être null");
        assertEquals(MAP_HEIGHT, map.length, "La hauteur doit être correcte");
        assertEquals(MAP_WIDTH, map[0].length, "La largeur doit être correcte");
    }

    @Test
    @DisplayName("createDefaultMap crée des murs sur les bords")
    void testDefaultMapHasBorderWalls() {
        int[][] map = MapLoader.loadMapFromBMP("nonexistent.bmp", MAP_WIDTH, MAP_HEIGHT);

        // Vérifier les bords
        for (int x = 0; x < MAP_WIDTH; x++) {
            assertEquals(-1, map[0][x], "Le bord supérieur doit être un mur");
            assertEquals(-1, map[MAP_HEIGHT - 1][x], "Le bord inférieur doit être un mur");
        }

        for (int y = 0; y < MAP_HEIGHT; y++) {
            assertEquals(-1, map[y][0], "Le bord gauche doit être un mur");
            assertEquals(-1, map[y][MAP_WIDTH - 1], "Le bord droit doit être un mur");
        }
    }

    @Test
    @DisplayName("createDefaultMap crée des zones libres au centre")
    void testDefaultMapHasFreeCenter() {
        int[][] map = MapLoader.loadMapFromBMP("nonexistent.bmp", MAP_WIDTH, MAP_HEIGHT);

        // Le centre doit avoir des zones libres
        int centerX = MAP_WIDTH / 2;
        int centerY = MAP_HEIGHT / 2;

        // Vérifier qu'il y a au moins une zone libre près du centre
        boolean hasFreeSpace = false;
        for (int y = centerY - 5; y <= centerY + 5; y++) {
            for (int x = centerX - 5; x <= centerX + 5; x++) {
                if (map[y][x] == 0) {
                    hasFreeSpace = true;
                    break;
                }
            }
            if (hasFreeSpace)
                break;
        }

        assertTrue(hasFreeSpace, "Il doit y avoir des zones libres au centre");
    }

    @Test
    @DisplayName("readMapName retourne un nom par défaut si fichier inexistant")
    void testReadMapNameDefault() {
        String name = MapLoader.readMapName("nonexistent.txt");

        assertNotNull(name, "Le nom ne doit pas être null");
        assertFalse(name.isEmpty(), "Le nom ne doit pas être vide");
    }

    @Test
    @DisplayName("loadMapFromBMP gère les fichiers inexistants")
    void testLoadMapFromBMPNonexistent() {
        int[][] map = MapLoader.loadMapFromBMP("nonexistent_file.bmp", MAP_WIDTH, MAP_HEIGHT);

        assertNotNull(map, "Une carte par défaut doit être créée");
        assertEquals(MAP_HEIGHT, map.length, "La hauteur doit être correcte");
        assertEquals(MAP_WIDTH, map[0].length, "La largeur doit être correcte");
    }

    @Test
    @DisplayName("loadMapFromBMP crée une carte avec dimensions correctes")
    void testMapDimensions() {
        int width = 100;
        int height = 80;
        int[][] map = MapLoader.loadMapFromBMP("nonexistent.bmp", width, height);

        assertEquals(height, map.length, "La hauteur doit correspondre");
        assertEquals(width, map[0].length, "La largeur doit correspondre");
    }

    @Test
    @DisplayName("loadMapFromBMP utilise valeurs -1 pour obstacles et 0 pour libre")
    void testMapValues() {
        int[][] map = MapLoader.loadMapFromBMP("nonexistent.bmp", MAP_WIDTH, MAP_HEIGHT);

        // Vérifier que seules les valeurs -1 et 0 sont utilisées
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                assertTrue(map[y][x] == -1 || map[y][x] == 0,
                        "Les valeurs doivent être -1 (obstacle) ou 0 (libre)");
            }
        }
    }
}
