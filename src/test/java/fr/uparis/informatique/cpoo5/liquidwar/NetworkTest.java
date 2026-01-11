package fr.uparis.informatique.cpoo5.liquidwar;

import fr.uparis.informatique.cpoo5.liquidwar.network.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le système réseau de Liquid War.
 */
public class NetworkTest {
    
    @Test
    @Timeout(10)
    public void testNetworkProtocol() {
        // Test des constantes
        assertTrue(NetworkProtocol.isValidPort(14000));
        assertFalse(NetworkProtocol.isValidPort(0));
        assertFalse(NetworkProtocol.isValidPort(70000));
        
        assertTrue(NetworkProtocol.isValidPlayerId(0));
        assertTrue(NetworkProtocol.isValidPlayerId(3));
        assertFalse(NetworkProtocol.isValidPlayerId(-1));
        assertFalse(NetworkProtocol.isValidPlayerId(4));
        
        System.out.println("✅ NetworkProtocol: Tests réussis");
    }
    
    @Test
    @Timeout(10)
    public void testNetworkMessage() {
        // Test de création de message
        NetworkMessage msg = new NetworkMessage(
            NetworkMessage.MessageType.CONNECT, 0, "test"
        );
        
        assertEquals(NetworkMessage.MessageType.CONNECT, msg.getType());
        assertEquals(0, msg.getPlayerId());
        assertEquals("test", msg.getDataAsString());
        assertNotNull(msg.getTimestamp());
        
        // Test CursorPosition
        NetworkMessage.CursorPosition pos = new NetworkMessage.CursorPosition(100, 200);
        NetworkMessage cursorMsg = new NetworkMessage(
            NetworkMessage.MessageType.CURSOR_MOVE, 1, pos
        );
        
        NetworkMessage.CursorPosition receivedPos = cursorMsg.getDataAsCursorPosition();
        assertNotNull(receivedPos);
        assertEquals(100, receivedPos.x);
        assertEquals(200, receivedPos.y);
        
        // Test GameStateData
        int[][] cursors = {{10, 20}, {30, 40}};
        int[] fighters = {100, 150};
        NetworkMessage.GameStateData state = new NetworkMessage.GameStateData(
            cursors, fighters, 42
        );
        
        NetworkMessage stateMsg = new NetworkMessage(
            NetworkMessage.MessageType.GAME_STATE, -1, state
        );
        
        NetworkMessage.GameStateData receivedState = stateMsg.getDataAsGameState();
        assertNotNull(receivedState);
        assertEquals(42, receivedState.gameTick);
        assertEquals(2, receivedState.fighterCounts.length);
        
        System.out.println("✅ NetworkMessage: Tests réussis");
    }
    
    @Test
    @Timeout(10)
    public void testServerClientConnection() throws Exception {
        final int testPort = 15000; // Port différent pour éviter les conflits
        final CountDownLatch serverStarted = new CountDownLatch(1);
        final CountDownLatch clientConnected = new CountDownLatch(1);
        final CountDownLatch messageReceived = new CountDownLatch(1);
        
        final AtomicInteger connectedPlayerId = new AtomicInteger(-1);
        
        // Démarrer le serveur dans un thread
        Thread serverThread = new Thread(() -> {
            try {
                NetworkServer server = new NetworkServer(testPort);
                server.setListener(new NetworkServer.NetworkServerListener() {
                    @Override
                    public void onClientConnected(int playerId) {
                        System.out.println("Serveur: Client connecté - ID " + playerId);
                        clientConnected.countDown();
                    }
                    
                    @Override
                    public void onClientDisconnected(int playerId) {
                        System.out.println("Serveur: Client déconnecté - ID " + playerId);
                    }
                    
                    @Override
                    public void onCursorMoved(int playerId, int x, int y) {
                        System.out.println("Serveur: Curseur déplacé - Joueur " + playerId + 
                                         " à (" + x + ", " + y + ")");
                        messageReceived.countDown();
                    }
                    
                    @Override
                    public NetworkMessage.GameStateData getCurrentGameState() {
                        return null; // Pas nécessaire pour ce test
                    }
                });
                
                server.start();
                serverStarted.countDown();
                System.out.println("Serveur démarré sur le port " + testPort);
                
                // Attendre que le test se termine
                Thread.sleep(5000);
                server.stop();
                
            } catch (Exception e) {
                e.printStackTrace();
                fail("Erreur serveur: " + e.getMessage());
            }
        });
        serverThread.start();
        
        // Attendre que le serveur démarre
        assertTrue(serverStarted.await(3, TimeUnit.SECONDS), 
                  "Le serveur n'a pas démarré à temps");
        
        // Attendre un peu pour que le serveur soit prêt
        Thread.sleep(500);
        
        // Connecter un client
        NetworkClient client = new NetworkClient("localhost", testPort);
        client.setListener(new NetworkClient.NetworkClientListener() {
            @Override
            public void onConnected(int playerId, String message) {
                System.out.println("Client: Connecté - ID " + playerId);
                connectedPlayerId.set(playerId);
            }
            
            @Override
            public void onGameStart() {}
            
            @Override
            public void onGameStateReceived(NetworkMessage.GameStateData state) {}
            
            @Override
            public void onCursorMoved(int playerId, int x, int y) {}
            
            @Override
            public void onGameOver(int winnerId) {}
            
            @Override
            public void onError(String error) {
                System.err.println("Client: Erreur - " + error);
            }
            
            @Override
            public void onDisconnected() {
                System.out.println("Client: Déconnecté");
            }
            
            @Override
            public void onLobbyUpdate(int connectedPlayers, int minPlayers) {
                System.out.println("Client: Lobby mis à jour - " + connectedPlayers + "/" + minPlayers + " joueurs");
            }
        });
        
        client.connect();
        System.out.println("Client connecté au serveur");
        
        // Attendre que le serveur confirme la connexion
        assertTrue(clientConnected.await(3, TimeUnit.SECONDS), 
                  "Le client ne s'est pas connecté à temps");
        
        // Vérifier que le client a reçu un ID valide
        Thread.sleep(500);
        assertTrue(connectedPlayerId.get() >= 0, 
                  "ID du client invalide: " + connectedPlayerId.get());
        
        // Envoyer un mouvement de curseur
        client.sendCursorMove(100, 200);
        System.out.println("Client: Mouvement de curseur envoyé");
        
        // Attendre que le serveur reçoive le message
        assertTrue(messageReceived.await(3, TimeUnit.SECONDS), 
                  "Le serveur n'a pas reçu le message à temps");
        
        // Déconnecter proprement
        client.disconnect();
        Thread.sleep(500);
        
        System.out.println("✅ ServerClientConnection: Test réussi");
    }
    
    @Test
    @Timeout(5)
    public void testMessageSerialization() {
        // Test que les messages sont bien sérialisables
        NetworkMessage msg1 = new NetworkMessage(
            NetworkMessage.MessageType.CURSOR_MOVE, 
            1, 
            new NetworkMessage.CursorPosition(50, 75)
        );
        
        assertNotNull(msg1);
        assertTrue(msg1 instanceof java.io.Serializable);
        
        NetworkMessage msg2 = new NetworkMessage(
            NetworkMessage.MessageType.GAME_STATE,
            -1,
            new NetworkMessage.GameStateData(
                new int[][]{{10, 20}},
                new int[]{100},
                123
            )
        );
        
        assertNotNull(msg2);
        assertTrue(msg2 instanceof java.io.Serializable);
        
        System.out.println("✅ MessageSerialization: Test réussi");
    }
}

