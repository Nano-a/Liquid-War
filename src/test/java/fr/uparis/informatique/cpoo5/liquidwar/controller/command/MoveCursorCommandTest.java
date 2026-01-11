package fr.uparis.informatique.cpoo5.liquidwar.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;

/**
 * Tests unitaires pour MoveCursorCommand.
 * 
 * Teste :
 * - Exécution de la commande
 * - Annulation (undo)
 * - Sauvegarde de l'ancienne position
 */
class MoveCursorCommandTest {

    private Cursor cursor;

    @BeforeEach
    void setUp() {
        cursor = new Cursor();
        cursor.x = 10;
        cursor.y = 20;
    }

    @Test
    @DisplayName("execute déplace le curseur")
    void testExecute() {
        Point newPos = new Point(30, 40);
        MoveCursorCommand cmd = new MoveCursorCommand(cursor, newPos);

        cmd.execute();

        assertEquals(30, cursor.x, "X doit être mis à jour");
        assertEquals(40, cursor.y, "Y doit être mis à jour");
    }

    @Test
    @DisplayName("undo restaure l'ancienne position")
    void testUndo() {
        Point oldPos = new Point(cursor.x, cursor.y);
        Point newPos = new Point(50, 60);
        MoveCursorCommand cmd = new MoveCursorCommand(cursor, newPos);

        cmd.execute();
        cmd.undo();

        assertEquals(oldPos.x, cursor.x, "X doit être restauré");
        assertEquals(oldPos.y, cursor.y, "Y doit être restauré");
    }

    @Test
    @DisplayName("Plusieurs commandes peuvent être exécutées séquentiellement")
    void testMultipleCommands() {
        int initialX = cursor.x;
        int initialY = cursor.y;

        // Premier mouvement
        MoveCursorCommand cmd1 = new MoveCursorCommand(cursor, new Point(100, 100));
        cmd1.execute();
        assertEquals(100, cursor.x, "Premier mouvement doit fonctionner");
        assertEquals(100, cursor.y);

        // Deuxième mouvement (depuis la position 100,100)
        MoveCursorCommand cmd2 = new MoveCursorCommand(cursor, new Point(200, 200));
        cmd2.execute();
        assertEquals(200, cursor.x, "Deuxième mouvement doit fonctionner");
        assertEquals(200, cursor.y);

        // Undo du deuxième mouvement (retour à 100,100 - position au moment de la
        // création de cmd2)
        cmd2.undo();
        assertEquals(100, cursor.x, "Undo du deuxième mouvement doit restaurer la position au moment de sa création");
        assertEquals(100, cursor.y);

        // Undo du premier mouvement (retour à position initiale)
        cmd1.undo();
        assertEquals(initialX, cursor.x, "Undo du premier mouvement doit restaurer la position initiale");
        assertEquals(initialY, cursor.y, "Y doit aussi être restauré");
    }
}
