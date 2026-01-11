package fr.uparis.informatique.cpoo5.liquidwar.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour NetworkClient.
 * 
 * Teste :
 * - Création du client
 * - Gestion du listener
 * - Envoi de messages
 */
class NetworkClientTest {

    private NetworkClient client;
    private TestListener listener;

    @BeforeEach
    void setUp() {
        client = new NetworkClient("localhost", 14000);
        listener = new TestListener();
        client.setListener(listener);
    }

    @Test
    @DisplayName("Création de client avec host et port")
    void testClientCreation() {
        NetworkClient newClient = new NetworkClient("localhost", 14000);
        assertNotNull(newClient, "Le client doit être créé");
    }

    @Test
    @DisplayName("setListener définit le listener")
    void testSetListener() {
        TestListener newListener = new TestListener();
        client.setListener(newListener);

        // Le listener doit être défini (pas de getter, on teste indirectement)
        assertDoesNotThrow(() -> client.setListener(newListener),
                "setListener ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("sendCursorMove ajoute un message à la queue")
    void testSendCursorMove() {
        // Ne peut pas tester sans connexion réelle, mais on vérifie que ça ne crash pas
        assertDoesNotThrow(() -> client.sendCursorMove(10, 20),
                "sendCursorMove ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("Le client peut être créé et configuré")
    void testClientConfiguration() {
        assertNotNull(client, "Le client doit être créé");
        assertDoesNotThrow(() -> client.setListener(listener),
                "setListener ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("disconnect ne crash pas si pas connecté")
    void testDisconnectWhenNotConnected() {
        assertDoesNotThrow(() -> client.disconnect(),
                "disconnect ne doit pas lever d'exception si pas connecté");
    }

    // Classe de test pour le listener
    private static class TestListener implements NetworkClient.NetworkClientListener {
        @Override
        public void onConnected(int playerId, String message) {
        }

        @Override
        public void onGameStart() {
        }

        @Override
        public void onGameStateReceived(NetworkMessage.GameStateData state) {
        }

        @Override
        public void onCursorMoved(int playerId, int x, int y) {
        }

        @Override
        public void onGameOver(int winnerId) {
        }

        @Override
        public void onError(String error) {
        }

        @Override
        public void onDisconnected() {
        }

        @Override
        public void onLobbyUpdate(int connectedPlayers, int minPlayers) {
        }
    }
}
