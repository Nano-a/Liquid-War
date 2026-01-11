package fr.uparis.informatique.cpoo5.liquidwar.controller.command;

import java.util.Stack;

/**
 * Gestionnaire d'historique des commandes (undo/redo).
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 */
public class CommandHistory {
    private final Stack<Command> history = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    
    public void execute(Command cmd) {
        cmd.execute();
        history.push(cmd);
        redoStack.clear();
    }
    
    public void undo() {
        if (!history.isEmpty()) {
            Command cmd = history.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            history.push(cmd);
        }
    }
    
    public boolean canUndo() {
        return !history.isEmpty();
    }
    
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}

