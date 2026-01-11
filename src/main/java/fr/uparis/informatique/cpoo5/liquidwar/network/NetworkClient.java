package fr.uparis.informatique.cpoo5.liquidwar.network;

import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client réseau pour se connecter à un serveur Liquid War.
 * 
 * <p>Ce client gère la connexion au serveur, l'envoi des mouvements du curseur
 * et la réception de l'état synchronisé du jeu.
 * 
 * <h2>Architecture</h2>
 * <ul>
 *   <li><b>Thread de réception</b> : Écoute les messages du serveur</li>
 *   <li><b>Thread d'envoi</b> : Envoie les messages en file d'attente</li>
 *   <li><b>Queue thread-safe</b> : File de messages à envoyer</li>
 * </ul>
 * 
 * <h2>Utilisation</h2>
 * <pre>{@code
 * NetworkClient client = new NetworkClient("localhost", 14000);
 * client.setListener(myListener);
 * client.connect();
 * // ... partie en cours ...
 * client.disconnect();
 * }</pre>
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class NetworkClient {
    
    private final String host;
    private final int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final AtomicBoolean connected;
    private Thread receiveThread;
    private Thread sendThread;
    private final BlockingQueue<NetworkMessage> sendQueue;
    private NetworkClientListener listener;
    private int myPlayerId = -1;
    
    private final GameLogger logger = GameLogger.getInstance();
    
    /**
     * Interface pour les événements du client.
     */
    public interface NetworkClientListener {
        /**
         * Appelé quand la connexion est établie.
         * 
         * @param playerId ID attribué au joueur
         * @param message Message de bienvenue
         */
        void onConnected(int playerId, String message);
        
        /**
         * Appelé quand le jeu démarre.
         */
        void onGameStart();
        
        /**
         * Appelé quand l'état du jeu est reçu.
         * 
         * @param state État synchronisé du jeu
         */
        void onGameStateReceived(NetworkMessage.GameStateData state);
        
        /**
         * Appelé quand un curseur est déplacé.
         * 
         * @param playerId ID du joueur
         * @param x Position X
         * @param y Position Y
         */
        void onCursorMoved(int playerId, int x, int y);
        
        /**
         * Appelé quand le jeu se termine.
         * 
         * @param winnerId ID du gagnant
         */
        void onGameOver(int winnerId);
        
        /**
         * Appelé en cas d'erreur.
         * 
         * @param error Message d'erreur
         */
        void onError(String error);
        
        /**
         * Appelé quand la connexion est perdue.
         */
        void onDisconnected();
        
        /**
         * Appelé quand le lobby est mis à jour.
         * 
         * @param connectedPlayers Nombre de joueurs connectés
         * @param minPlayers Nombre minimum de joueurs requis
         */
        void onLobbyUpdate(int connectedPlayers, int minPlayers);
    }
    
    /**
     * Crée un nouveau client réseau.
     * 
     * @param host Adresse du serveur
     * @param port Port du serveur
     */
    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.connected = new AtomicBoolean(false);
        this.sendQueue = new LinkedBlockingQueue<>();
    }
    
    /**
     * Définit le listener pour les événements réseau.
     * 
     * @param listener Listener à notifier
     */
    public void setListener(NetworkClientListener listener) {
        this.listener = listener;
    }
    
    /**
     * Se connecte au serveur.
     * 
     * @throws IOException Si la connexion échoue
     */
    public void connect() throws IOException {
        if (connected.get()) {
            logger.warn("Client déjà connecté");
            return;
        }
        
        logger.info("Connexion au serveur %s:%d...", host, port);
        
        socket = new Socket(host, port);
        socket.setSoTimeout(NetworkProtocol.CLIENT_TIMEOUT_MS);
        
        // Initialiser les streams (IMPORTANT: out avant in!)
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        
        connected.set(true);
        
        // Démarrer les threads
        receiveThread = Thread.ofVirtual().start(this::receiveLoop);
        sendThread = Thread.ofVirtual().start(this::sendLoop);
        
        logger.info("Connecté au serveur");
    }
    
    /**
     * Se déconnecte du serveur.
     */
    public void disconnect() {
        if (!connected.compareAndSet(true, false)) {
            return; // Déjà déconnecté
        }
        
        logger.info("Déconnexion du serveur...");
        
        // Envoyer un message de déconnexion
        try {
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.DISCONNECT, myPlayerId));
            Thread.sleep(100); // Laisser le temps d'envoyer
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Fermer les streams
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.error("Erreur lors de la déconnexion: " + e.getMessage());
        }
        
        // Attendre les threads
        try {
            if (receiveThread != null) receiveThread.join(1000);
            if (sendThread != null) sendThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("Déconnecté");
        
        // Notifier le listener
        if (listener != null) {
            listener.onDisconnected();
        }
    }
    
    /**
     * Boucle de réception des messages.
     */
    private void receiveLoop() {
        while (connected.get()) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                handleMessage(message);
            } catch (IOException e) {
                if (connected.get()) {
                    logger.error("Erreur de réception: " + e.getMessage());
                    disconnect();
                }
                break;
            } catch (ClassNotFoundException e) {
                logger.error("Message invalide reçu");
            }
        }
    }
    
    /**
     * Boucle d'envoi des messages.
     */
    private void sendLoop() {
        while (connected.get()) {
            try {
                NetworkMessage message = sendQueue.take();
                
                synchronized (out) {
                    out.writeObject(message);
                    out.flush();
                    out.reset();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                if (connected.get()) {
                    logger.error("Erreur d'envoi: " + e.getMessage());
                    disconnect();
                }
                break;
            }
        }
    }
    
    /**
     * Traite un message reçu du serveur.
     */
    private void handleMessage(NetworkMessage message) {
        if (listener == null) {
            return;
        }
        
        switch (message.getType()) {
            case CONNECTED:
                myPlayerId = message.getPlayerId();
                String welcomeMsg = message.getDataAsString();
                logger.info("Connecté en tant que Joueur %d", myPlayerId);
                listener.onConnected(myPlayerId, welcomeMsg != null ? welcomeMsg : "");
                break;
                
            case GAME_START:
                logger.info("La partie commence!");
                listener.onGameStart();
                break;
            
            case LOBBY_UPDATE:
                NetworkMessage.LobbyInfo lobbyInfo = message.getDataAsLobbyInfo();
                if (lobbyInfo != null) {
                    logger.info("Lobby mis à jour: %d/%d joueurs", 
                            lobbyInfo.connectedPlayers, lobbyInfo.minPlayers);
                    listener.onLobbyUpdate(lobbyInfo.connectedPlayers, lobbyInfo.minPlayers);
                }
                break;
                
            case GAME_STATE:
                NetworkMessage.GameStateData state = message.getDataAsGameState();
                if (state != null) {
                    listener.onGameStateReceived(state);
                }
                break;
                
            case CURSOR_MOVE:
                NetworkMessage.CursorPosition pos = message.getDataAsCursorPosition();
                if (pos != null) {
                    listener.onCursorMoved(message.getPlayerId(), pos.x, pos.y);
                }
                break;
                
            case GAME_OVER:
                int[] data = message.getDataAsIntArray();
                int winnerId = (data != null && data.length > 0) ? data[0] : -1;
                logger.info("Partie terminée! Gagnant: Joueur %d", winnerId);
                listener.onGameOver(winnerId);
                break;
                
            case ERROR:
                String error = message.getDataAsString();
                logger.error("Erreur du serveur: " + error);
                listener.onError(error != null ? error : "Erreur inconnue");
                break;
                
            case PONG:
                // Réponse au ping - connexion OK
                break;
                
            default:
                logger.warn("Message non géré: %s", message.getType());
        }
    }
    
    /**
     * Envoie un mouvement de curseur au serveur.
     * 
     * @param x Position X du curseur
     * @param y Position Y du curseur
     */
    public void sendCursorMove(int x, int y) {
        if (!connected.get()) {
            return;
        }
        
        NetworkMessage.CursorPosition pos = new NetworkMessage.CursorPosition(x, y);
        NetworkMessage message = new NetworkMessage(
                NetworkMessage.MessageType.CURSOR_MOVE, myPlayerId, pos);
        sendMessage(message);
    }
    
    /**
     * Envoie un message au serveur (ajouté à la queue).
     * 
     * @param message Message à envoyer
     */
    public void sendMessage(NetworkMessage message) {
        if (!connected.get()) {
            return;
        }
        
        try {
            sendQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interruption lors de l'ajout d'un message à la queue");
        }
    }
    
    /**
     * Envoie un ping au serveur pour vérifier la connexion.
     */
    public void sendPing() {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.PING, myPlayerId));
    }
    
    /**
     * Obtient l'ID du joueur attribué par le serveur.
     * 
     * @return ID du joueur, ou -1 si pas encore connecté
     */
    public int getMyPlayerId() {
        return myPlayerId;
    }
    
    /**
     * Vérifie si le client est connecté.
     * 
     * @return true si connecté
     */
    public boolean isConnected() {
        return connected.get();
    }
    
    /**
     * Obtient l'adresse du serveur.
     * 
     * @return Adresse du serveur
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Obtient le port du serveur.
     * 
     * @return Port du serveur
     */
    public int getPort() {
        return port;
    }
}

