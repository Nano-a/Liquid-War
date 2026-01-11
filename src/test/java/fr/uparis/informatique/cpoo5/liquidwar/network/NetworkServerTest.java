package fr.uparis.informatique.cpoo5.liquidwar.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour NetworkServer.
 * 
 * Teste :
 * - Création du serveur
 * - Gestion du listener
 * - Méthodes utilitaires
 */
class NetworkServerTest {

    private NetworkServer server;
    private TestListener listener;

    @BeforeEach
    void setUp() {
        server = new NetworkServer(14000);
        listener = new TestListener();
        server.setListener(listener);
    }

    @Test
    @DisplayName("Création de serveur avec port")
    void testServerCreation() {
        NetworkServer newServer = new NetworkServer(15000);
        assertNotNull(newServer, "Le serveur doit être créé");
    }

    @Test
    @DisplayName("setListener définit le listener")
    void testSetListener() {
        TestListener newListener = new TestListener();
        server.setListener(newListener);

        assertDoesNotThrow(() -> server.setListener(newListener),
                "setListener ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("Le serveur peut être créé et configuré")
    void testServerConfiguration() {
        assertNotNull(server, "Le serveur doit être créé");
        assertDoesNotThrow(() -> server.setListener(listener),
                "setListener ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("stop ne crash pas si pas démarré")
    void testStopWhenNotStarted() {
        assertDoesNotThrow(() -> server.stop(),
                "stop ne doit pas lever d'exception si pas démarré");
    }

    // Classe de test pour le listener
    private static class TestListener implements NetworkServer.NetworkServerListener {
        @Override
        public void onClientConnected(int playerId) {
        }

        @Override
        public void onClientDisconnected(int playerId) {
        }

        @Override
        public void onCursorMoved(int playerId, int x, int y) {
        }

        @Override
        public NetworkMessage.GameStateData getCurrentGameState() {
            return null;
        }
    }
}
