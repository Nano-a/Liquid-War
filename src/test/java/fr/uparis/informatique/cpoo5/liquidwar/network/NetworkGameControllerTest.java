package fr.uparis.informatique.cpoo5.liquidwar.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.model.GameState;

/**
 * Tests unitaires pour NetworkGameController.
 * 
 * Teste :
 * - Création du contrôleur
 * - Gestion de l'état du jeu
 * - Configuration
 */
class NetworkGameControllerTest {

    private NetworkGameController controller;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        int[][] map = new int[10][10];
        int[][] gradient = new int[2][100];
        gameState = new GameState(map, gradient, new java.util.ArrayList<>(),
                new fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor[2],
                new int[2], 2);
        controller = new NetworkGameController(gameState);
    }

    @Test
    @DisplayName("Création de contrôleur avec GameState")
    void testControllerCreation() {
        assertNotNull(controller, "Le contrôleur doit être créé");
    }

    @Test
    @DisplayName("Création de contrôleur sans GameState")
    void testControllerCreationWithoutGameState() {
        NetworkGameController newController = new NetworkGameController(null);
        assertNotNull(newController, "Le contrôleur doit être créé même sans GameState");
    }

    @Test
    @DisplayName("setGameState définit l'état du jeu")
    void testSetGameState() {
        controller.setGameState(gameState);

        assertDoesNotThrow(() -> controller.setGameState(gameState),
                "setGameState ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("setMinPlayers définit le nombre minimum de joueurs")
    void testSetMinPlayers() {
        controller.setMinPlayers(3);
        assertEquals(3, controller.getMinPlayers(),
                "Le nombre minimum de joueurs doit être défini");
    }

    @Test
    @DisplayName("getMinPlayers retourne la valeur par défaut")
    void testGetMinPlayers() {
        assertEquals(2, controller.getMinPlayers(),
                "Le nombre minimum par défaut doit être 2");
    }

    @Test
    @DisplayName("setLobbyUpdateListener définit le listener")
    void testSetLobbyUpdateListener() {
        NetworkGameController.LobbyUpdateListener listener = (connected, min) -> {
        };

        assertDoesNotThrow(() -> controller.setLobbyUpdateListener(listener),
                "setLobbyUpdateListener ne doit pas lever d'exception");
    }
}
