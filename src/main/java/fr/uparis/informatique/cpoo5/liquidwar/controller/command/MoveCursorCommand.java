package fr.uparis.informatique.cpoo5.liquidwar.controller.command;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import java.awt.Point;

/**
 * Commande pour déplacer un curseur.
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 */
public class MoveCursorCommand implements Command {
    private final Cursor cursor;
    private final Point oldPos;
    private final Point newPos;
    
    public MoveCursorCommand(Cursor cursor, Point newPos) {
        this.cursor = cursor;
        this.oldPos = new Point(cursor.x, cursor.y);
        this.newPos = newPos;
    }
    
    @Override
    public void execute() {
        cursor.x = newPos.x;
        cursor.y = newPos.y;
    }
    
    @Override
    public void undo() {
        cursor.x = oldPos.x;
        cursor.y = oldPos.y;
    }
}
