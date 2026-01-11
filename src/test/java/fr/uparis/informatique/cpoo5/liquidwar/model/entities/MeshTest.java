package fr.uparis.informatique.cpoo5.liquidwar.model.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour Mesh.
 * 
 * Teste :
 * - Création de Mesh
 * - Gestion des voisins
 * - Informations par équipe
 */
class MeshTest {

    private Mesh mesh;

    @BeforeEach
    void setUp() {
        mesh = new Mesh(10, 20);
    }

    @Test
    @DisplayName("Création de Mesh avec position correcte")
    void testMeshCreation() {
        assertEquals(10, mesh.x, "X doit être correct");
        assertEquals(20, mesh.y, "Y doit être correct");
        assertNotNull(mesh.neighbors, "Les voisins doivent être initialisés");
        assertNotNull(mesh.teamInfo, "Les infos d'équipe doivent être initialisées");
    }

    @Test
    @DisplayName("setNeighbor et getNeighbor fonctionnent")
    void testNeighborManagement() {
        Mesh neighbor = new Mesh(11, 20);

        mesh.setNeighbor(1, neighbor); // Direction NE

        assertEquals(neighbor, mesh.getNeighbor(1), "Le voisin doit être accessible");
        assertTrue(mesh.hasNeighbor(1), "hasNeighbor doit retourner true");
    }

    @Test
    @DisplayName("getNeighbor retourne null pour direction invalide")
    void testGetNeighborInvalidDirection() {
        assertNull(mesh.getNeighbor(-1), "Direction négative doit retourner null");
        assertNull(mesh.getNeighbor(12), "Direction >= 12 doit retourner null");
    }

    @Test
    @DisplayName("hasNeighbor détecte l'absence de voisin")
    void testHasNeighbor() {
        assertFalse(mesh.hasNeighbor(0), "Pas de voisin par défaut");

        Mesh neighbor = new Mesh(11, 20);
        mesh.setNeighbor(1, neighbor);

        assertTrue(mesh.hasNeighbor(1), "Voisin doit être détecté");
        assertFalse(mesh.hasNeighbor(0), "Autre direction ne doit pas avoir de voisin");
    }

    @Test
    @DisplayName("teamInfo est initialisé pour toutes les équipes")
    void testTeamInfoInitialization() {
        assertNotNull(mesh.teamInfo, "teamInfo ne doit pas être null");
        assertTrue(mesh.teamInfo.length >= 2, "Il doit y avoir au moins 2 équipes");

        for (int team = 0; team < 2; team++) {
            assertNotNull(mesh.teamInfo[team], "L'info d'équipe " + team + " doit exister");
        }
    }

    @Test
    @DisplayName("resetGradient réinitialise le gradient")
    void testResetGradient() {
        mesh.teamInfo[0].gradient = 1000;
        mesh.teamInfo[0].resetGradient();

        assertEquals(fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig.AREA_START_GRADIENT,
                mesh.teamInfo[0].gradient,
                "Le gradient doit être réinitialisé à AREA_START_GRADIENT");
    }

    @Test
    @DisplayName("arrayIndex peut être défini")
    void testArrayIndex() {
        mesh.arrayIndex = 42;
        assertEquals(42, mesh.arrayIndex, "arrayIndex doit être modifiable");
    }
}
