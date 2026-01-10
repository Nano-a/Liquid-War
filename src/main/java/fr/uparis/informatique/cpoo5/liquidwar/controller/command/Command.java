package fr.uparis.informatique.cpoo5.liquidwar.controller.command;

/**
 * Interface pour le Pattern Command.
 * 
 * <p>Encapsule une action dans un objet, permettant de :
 * <ul>
 *   <li>Paramétrer des objets avec des actions</li>
 *   <li>Mettre des requêtes en file d'attente</li>
 *   <li>Annuler des opérations (undo/redo)</li>
 * </ul>
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 */
public interface Command {
    /**
     * Exécute la commande.
     */
    void execute();
    
    /**
     * Annule la commande.
     */
    void undo();
}

