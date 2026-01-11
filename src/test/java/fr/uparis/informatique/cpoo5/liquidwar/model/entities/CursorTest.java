package fr.uparis.informatique.cpoo5.liquidwar.model.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour Cursor.
 * 
 * Teste :
 * - Création de curseur
 * - Modification des valeurs
 * - État actif/inactif
 */
class CursorTest {

    @Test
    @DisplayName("Création de curseur")
    void testCursorCreation() {
        Cursor cursor = new Cursor();

        // Valeurs par défaut
        assertEquals(0, cursor.x, "X doit être 0 par défaut");
        assertEquals(0, cursor.y, "Y doit être 0 par défaut");
        assertEquals(0, cursor.team, "L'équipe doit être 0 par défaut");
        assertEquals(0, cursor.active, "Le curseur doit être inactif par défaut");
    }

    @Test
    @DisplayName("Curseur peut être modifié")
    void testCursorModification() {
        Cursor cursor = new Cursor();

        cursor.x = 100;
        cursor.y = 200;
        cursor.team = 1;
        cursor.active = 1;

        assertEquals(100, cursor.x, "X doit être modifiable");
        assertEquals(200, cursor.y, "Y doit être modifiable");
        assertEquals(1, cursor.team, "L'équipe doit être modifiable");
        assertEquals(1, cursor.active, "L'état actif doit être modifiable");
    }

    @Test
    @DisplayName("Curseur peut être activé et désactivé")
    void testCursorActivation() {
        Cursor cursor = new Cursor();

        cursor.active = 1;
        assertEquals(1, cursor.active, "Le curseur doit pouvoir être activé");

        cursor.active = 0;
        assertEquals(0, cursor.active, "Le curseur doit pouvoir être désactivé");
    }
}
