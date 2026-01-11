package fr.uparis.informatique.cpoo5.liquidwar.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Mesh;

/**
 * Tests unitaires pour MeshBuilder.
 * 
 * Teste :
 * - Construction du tableau MESH
 * - Création des liens entre voisins
 * - Gestion des obstacles
 */
class MeshBuilderTest {

    private int[][] map;
    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 50;

    @BeforeEach
    void setUp() {
        map = new int[MAP_HEIGHT][MAP_WIDTH];
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }
    }

    @Test
    @DisplayName("buildMeshArray crée des cellules MESH")
    void testBuildMeshArray() {
        Mesh[] meshArray = MeshBuilder.buildMeshArray(map);

        assertNotNull(meshArray, "Le tableau MESH ne doit pas être null");
        assertTrue(meshArray.length > 0, "Il doit y avoir des cellules MESH");

        // Vérifier que toutes les cellules sont valides
        for (Mesh mesh : meshArray) {
            assertNotNull(mesh, "Chaque cellule doit être non null");
            assertTrue(mesh.x >= 0 && mesh.x < MAP_WIDTH, "X doit être dans les limites");
            assertTrue(mesh.y >= 0 && mesh.y < MAP_HEIGHT, "Y doit être dans les limites");
        }
    }

    @Test
    @DisplayName("buildMeshArray exclut les obstacles")
    void testBuildMeshArrayExcludesObstacles() {
        // Créer des obstacles
        for (int y = 20; y < 30; y++) {
            for (int x = 20; x < 30; x++) {
                map[y][x] = -1;
            }
        }

        Mesh[] meshArray = MeshBuilder.buildMeshArray(map);

        // Vérifier qu'aucune cellule MESH n'est sur un obstacle
        for (Mesh mesh : meshArray) {
            assertNotEquals(-1, map[mesh.y][mesh.x],
                    "Aucune cellule MESH ne doit être sur un obstacle");
        }
    }

    @Test
    @DisplayName("buildMeshArray crée des liens entre voisins")
    void testBuildMeshArrayCreatesLinks() {
        Mesh[] meshArray = MeshBuilder.buildMeshArray(map);

        // Trouver une cellule au centre
        Mesh centerMesh = null;
        for (Mesh mesh : meshArray) {
            if (mesh.x == MAP_WIDTH / 2 && mesh.y == MAP_HEIGHT / 2) {
                centerMesh = mesh;
                break;
            }
        }

        if (centerMesh != null) {
            // Vérifier qu'au moins un voisin est lié
            boolean hasNeighbor = false;
            for (int dir = 0; dir < 12; dir++) {
                if (centerMesh.hasNeighbor(dir)) {
                    hasNeighbor = true;
                    break;
                }
            }
            assertTrue(hasNeighbor, "La cellule doit avoir au moins un voisin");
        }
    }

    @Test
    @DisplayName("buildMeshArray gère carte vide")
    void testBuildMeshArrayEmptyMap() {
        int[][] emptyMap = new int[0][0];
        Mesh[] meshArray = MeshBuilder.buildMeshArray(emptyMap);

        assertNotNull(meshArray, "Le tableau ne doit pas être null");
        assertEquals(0, meshArray.length, "Le tableau doit être vide");
    }

    @Test
    @DisplayName("buildMeshArray gère carte avec seulement obstacles")
    void testBuildMeshArrayOnlyObstacles() {
        // Remplir toute la carte d'obstacles
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = -1;
            }
        }

        Mesh[] meshArray = MeshBuilder.buildMeshArray(map);

        assertEquals(0, meshArray.length, "Il ne doit y avoir aucune cellule MESH");
    }

    @Test
    @DisplayName("buildMeshArray crée le bon nombre de cellules")
    void testBuildMeshArrayCorrectCount() {
        // Compter les cellules libres
        int freeCells = 0;
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                if (map[y][x] != -1) {
                    freeCells++;
                }
            }
        }

        Mesh[] meshArray = MeshBuilder.buildMeshArray(map);

        assertEquals(freeCells, meshArray.length,
                "Le nombre de cellules MESH doit correspondre aux cellules libres");
    }
}
