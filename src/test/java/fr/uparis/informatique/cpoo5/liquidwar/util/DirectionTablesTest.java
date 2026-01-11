package fr.uparis.informatique.cpoo5.liquidwar.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour DirectionTables.
 * 
 * Teste :
 * - Tables DIR_MOVE_X et DIR_MOVE_Y
 * - Validité des directions
 */
class DirectionTablesTest {

    @Test
    @DisplayName("DIR_MOVE_X contient 2 tables de 12 directions")
    void testDirMoveX() {
        assertNotNull(DirectionTables.DIR_MOVE_X, "DIR_MOVE_X ne doit pas être null");
        assertEquals(2, DirectionTables.DIR_MOVE_X.length, "Il doit y avoir 2 tables");
        assertEquals(12, DirectionTables.DIR_MOVE_X[0].length, "Chaque table doit avoir 12 directions");
        assertEquals(12, DirectionTables.DIR_MOVE_X[1].length, "Chaque table doit avoir 12 directions");
    }

    @Test
    @DisplayName("DIR_MOVE_Y contient 2 tables de 12 directions")
    void testDirMoveY() {
        assertNotNull(DirectionTables.DIR_MOVE_Y, "DIR_MOVE_Y ne doit pas être null");
        assertEquals(2, DirectionTables.DIR_MOVE_Y.length, "Il doit y avoir 2 tables");
        assertEquals(12, DirectionTables.DIR_MOVE_Y[0].length, "Chaque table doit avoir 12 directions");
        assertEquals(12, DirectionTables.DIR_MOVE_Y[1].length, "Chaque table doit avoir 12 directions");
    }

    @Test
    @DisplayName("Les valeurs de déplacement sont dans une plage raisonnable")
    void testMoveValuesRange() {
        for (int table = 0; table < 2; table++) {
            for (int dir = 0; dir < 12; dir++) {
                int x = DirectionTables.DIR_MOVE_X[table][dir];
                int y = DirectionTables.DIR_MOVE_Y[table][dir];

                assertTrue(x >= -1 && x <= 1, "X doit être entre -1 et 1");
                assertTrue(y >= -1 && y <= 1, "Y doit être entre -1 et 1");
            }
        }
    }

    @Test
    @DisplayName("Les deux tables ont les mêmes valeurs")
    void testTablesConsistency() {
        for (int dir = 0; dir < 12; dir++) {
            assertEquals(DirectionTables.DIR_MOVE_X[0][dir], DirectionTables.DIR_MOVE_X[1][dir],
                    "Les deux tables X doivent être identiques");
            assertEquals(DirectionTables.DIR_MOVE_Y[0][dir], DirectionTables.DIR_MOVE_Y[1][dir],
                    "Les deux tables Y doivent être identiques");
        }
    }
}
