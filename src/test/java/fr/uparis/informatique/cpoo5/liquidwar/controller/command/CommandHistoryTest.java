package fr.uparis.informatique.cpoo5.liquidwar.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour CommandHistory.
 * 
 * Teste :
 * - Exécution de commandes
 * - Undo/Redo
 * - État canUndo/canRedo
 */
class CommandHistoryTest {

    private CommandHistory history;
    private int testValue;

    @BeforeEach
    void setUp() {
        history = new CommandHistory();
        testValue = 0;
    }

    @Test
    @DisplayName("execute ajoute une commande à l'historique")
    void testExecute() {
        Command cmd = new TestCommand(() -> testValue = 10, () -> testValue = 0);

        assertFalse(history.canUndo(), "Pas d'undo possible avant exécution");
        history.execute(cmd);

        assertEquals(10, testValue, "La commande doit être exécutée");
        assertTrue(history.canUndo(), "Undo doit être possible après exécution");
    }

    @Test
    @DisplayName("undo annule la dernière commande")
    void testUndo() {
        Command cmd = new TestCommand(() -> testValue = 20, () -> testValue = 0);
        history.execute(cmd);

        history.undo();

        assertEquals(0, testValue, "Undo doit restaurer l'état précédent");
        assertTrue(history.canRedo(), "Redo doit être possible après undo");
    }

    @Test
    @DisplayName("redo réexécute la commande annulée")
    void testRedo() {
        Command cmd = new TestCommand(() -> testValue = 30, () -> testValue = 0);
        history.execute(cmd);
        history.undo();

        history.redo();

        assertEquals(30, testValue, "Redo doit réexécuter la commande");
        assertTrue(history.canUndo(), "Undo doit être possible après redo");
    }

    @Test
    @DisplayName("undo vide ne fait rien")
    void testUndoEmpty() {
        assertFalse(history.canUndo(), "Pas d'undo possible si historique vide");
        history.undo(); // Ne doit pas crasher
        assertFalse(history.canUndo(), "Toujours pas d'undo possible");
    }

    @Test
    @DisplayName("redo vide ne fait rien")
    void testRedoEmpty() {
        assertFalse(history.canRedo(), "Pas de redo possible si pile vide");
        history.redo(); // Ne doit pas crasher
        assertFalse(history.canRedo(), "Toujours pas de redo possible");
    }

    @Test
    @DisplayName("execute efface la pile redo")
    void testExecuteClearsRedo() {
        Command cmd1 = new TestCommand(() -> testValue = 10, () -> testValue = 0);
        Command cmd2 = new TestCommand(() -> testValue = 20, () -> testValue = 10);

        history.execute(cmd1);
        history.undo();
        assertTrue(history.canRedo(), "Redo doit être possible");

        history.execute(cmd2);
        assertFalse(history.canRedo(), "Redo doit être effacé après nouvelle commande");
    }

    // Classe de test pour les commandes
    private static class TestCommand implements Command {
        private final Runnable executeAction;
        private final Runnable undoAction;

        public TestCommand(Runnable executeAction, Runnable undoAction) {
            this.executeAction = executeAction;
            this.undoAction = undoAction;
        }

        @Override
        public void execute() {
            executeAction.run();
        }

        @Override
        public void undo() {
            undoAction.run();
        }
    }
}
