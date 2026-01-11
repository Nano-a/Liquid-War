package fr.uparis.informatique.cpoo5.liquidwar.network;

import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Serveur de jeu pour le mode multijoueur en réseau.
 * 
 * <p>Ce serveur gère les connexions des clients, synchronise l'état du jeu
 * et transmet les mouvements des curseurs entre les joueurs.
 * 
 * <h2>Architecture</h2>
 * <ul>
 *   <li><b>Thread principal</b> : Accepte les connexions entrantes</li>
 *   <li><b>Threads clients</b> : Un thread virtuel par client connecté</li>
 *   <li><b>Thread de synchronisation</b> : Broadcast périodique de l'état</li>
 * </ul>
 * 
 * <h2>Utilisation</h2>
 * <pre>{@code
 * NetworkServer server = new NetworkServer(14000);
 * server.start();
 * // ... partie en cours ...
 * server.stop();
 * }</pre>
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class NetworkServer {
    
    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients;
    private final AtomicBoolean running;
    private Thread acceptThread;
    private Thread syncThread;
    private NetworkServerListener listener;
    
    private final GameLogger logger = GameLogger.getInstance();
    
    /**
     * Interface pour les événements du serveur.
     */
    public interface NetworkServerListener {
        /**
         * Appelé quand un client se connecte.
         * 
         * @param playerId ID du joueur connecté
         */
        void onClientConnected(int playerId);
        
        /**
         * Appelé quand un client se déconnecte.
         * 
         * @param playerId ID du joueur déconnecté
         */
        void onClientDisconnected(int playerId);
        
        /**
         * Appelé quand un curseur est déplacé.
         * 
         * @param playerId ID du joueur
         * @param x Position X
         * @param y Position Y
         */
        void onCursorMoved(int playerId, int x, int y);
        
        /**
         * Appelé pour obtenir l'état actuel du jeu.
         * 
         * @return État du jeu à synchroniser
         */
        NetworkMessage.GameStateData getCurrentGameState();
    }
    
    /**
     * Crée un nouveau serveur sur le port spécifié.
     * 
     * @param port Port d'écoute
     */
    public NetworkServer(int port) {
        this.port = port;
        this.clients = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Définit le listener pour les événements serveur.
     * 
     * @param listener Listener à notifier
     */
    public void setListener(NetworkServerListener listener) {
        this.listener = listener;
    }
    
    /**
     * Démarre le serveur.
     * 
     * @throws IOException Si le serveur ne peut pas être démarré
     */
    public void start() throws IOException {
        if (running.get()) {
            logger.warn("Le serveur est déjà démarré");
            return;
        }
        
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000); // Timeout pour permettre l'arrêt propre
        running.set(true);
        
        logger.info("╔════════════════════════════════════════════╗");
        logger.info("║  🌐 SERVEUR LIQUID WAR DÉMARRÉ            ║");
        logger.info("║  Port: %-36d║", port);
        logger.info("║  Max joueurs: %-28d║", NetworkProtocol.MAX_PLAYERS);
        logger.info("╚════════════════════════════════════════════╝");
        
        // Thread pour accepter les connexions
        acceptThread = Thread.ofVirtual().start(() -> acceptClients());
        
        // Thread pour synchroniser l'état du jeu
        syncThread = Thread.ofVirtual().start(() -> synchronizeGameState());
    }
    
    /**
     * Arrête le serveur et déconnecte tous les clients.
     */
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        logger.info("Arrêt du serveur...");
        running.set(false);
        
        // Déconnecter tous les clients
        for (ClientHandler client : new ArrayList<>(clients)) {
            client.disconnect();
        }
        clients.clear();
        
        // Fermer le socket serveur
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la fermeture du serveur: " + e.getMessage());
        }
        
        // Attendre les threads
        try {
            if (acceptThread != null) acceptThread.join(2000);
            if (syncThread != null) syncThread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("Serveur arrêté");
    }
    
    /**
     * Boucle d'acceptation des clients.
     */
    private void acceptClients() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Vérifier si le serveur est plein
                if (clients.size() >= NetworkProtocol.MAX_PLAYERS) {
                    logger.warn("Connexion refusée: serveur plein");
                    sendErrorAndClose(clientSocket, NetworkProtocol.getServerFullMessage());
                    continue;
                }
                
                // Créer un handler pour ce client
                int playerId = clients.size();
                ClientHandler handler = new ClientHandler(clientSocket, playerId);
                clients.add(handler);
                
                // Démarrer le thread du client
                Thread.ofVirtual().start(handler);
                
                logger.info("Client connecté: Joueur %d (%s)", playerId, 
                        clientSocket.getInetAddress().getHostAddress());
                
                // Notifier le listener
                if (listener != null) {
                    listener.onClientConnected(playerId);
                }
                
            } catch (SocketTimeoutException e) {
                // Normal - timeout pour permettre la vérification de running
            } catch (IOException e) {
                if (running.get()) {
                    logger.error("Erreur lors de l'acceptation d'un client: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Envoie une erreur à un socket puis le ferme.
     */
    private void sendErrorAndClose(Socket socket, String errorMessage) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            NetworkMessage error = new NetworkMessage(
                    NetworkMessage.MessageType.ERROR, -1, errorMessage);
            out.writeObject(error);
            out.flush();
        } catch (IOException e) {
            logger.error("Erreur lors de l'envoi d'erreur: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignorer
            }
        }
    }
    
    /**
     * Boucle de synchronisation de l'état du jeu.
     */
    private void synchronizeGameState() {
        while (running.get()) {
            try {
                Thread.sleep(NetworkProtocol.SYNC_INTERVAL_MS);
                
                // Obtenir l'état actuel du jeu
                if (listener != null && !clients.isEmpty()) {
                    NetworkMessage.GameStateData state = listener.getCurrentGameState();
                    if (state != null) {
                        NetworkMessage message = new NetworkMessage(
                                NetworkMessage.MessageType.GAME_STATE, -1, state);
                        broadcast(message);
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Envoie un message à tous les clients connectés.
     * 
     * @param message Message à envoyer
     */
    public void broadcast(NetworkMessage message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    /**
     * Envoie un message à un client spécifique.
     * 
     * @param playerId ID du joueur destinataire
     * @param message Message à envoyer
     */
    public void sendToPlayer(int playerId, NetworkMessage message) {
        if (playerId >= 0 && playerId < clients.size()) {
            clients.get(playerId).sendMessage(message);
        }
    }
    
    /**
     * Obtient le nombre de clients connectés.
     * 
     * @return Nombre de clients
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * Vérifie si le serveur est en cours d'exécution.
     * 
     * @return true si le serveur est actif
     */
    public boolean isRunning() {
        return running.get();
    }
    
    // ==================== HANDLER POUR CHAQUE CLIENT ====================
    
    /**
     * Gère la communication avec un client spécifique.
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private final AtomicBoolean connected;
        
        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
            this.connected = new AtomicBoolean(true);
        }
        
        @Override
        public void run() {
            try {
                // Initialiser les streams (IMPORTANT: out avant in!)
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                
                // Envoyer la confirmation de connexion
                NetworkMessage welcome = new NetworkMessage(
                        NetworkMessage.MessageType.CONNECTED, playerId, 
                        NetworkProtocol.WELCOME_MESSAGE);
                sendMessage(welcome);
                
                // Boucle de réception des messages
                while (connected.get() && running.get()) {
                    try {
                        NetworkMessage message = (NetworkMessage) in.readObject();
                        handleMessage(message);
                    } catch (ClassNotFoundException e) {
                        logger.error("Message invalide reçu du joueur %d", playerId);
                    }
                }
                
            } catch (IOException e) {
                if (connected.get()) {
                    logger.info("Joueur %d déconnecté: %s", playerId, e.getMessage());
                }
            } finally {
                disconnect();
            }
        }
        
        /**
         * Traite un message reçu du client.
         */
        private void handleMessage(NetworkMessage message) {
            switch (message.getType()) {
                case CURSOR_MOVE:
                    NetworkMessage.CursorPosition pos = message.getDataAsCursorPosition();
                    if (pos != null && listener != null) {
                        listener.onCursorMoved(playerId, pos.x, pos.y);
                        // Transmettre le mouvement aux autres joueurs
                        broadcast(message);
                    }
                    break;
                    
                case PING:
                    // Répondre au ping
                    sendMessage(new NetworkMessage(NetworkMessage.MessageType.PONG, playerId));
                    break;
                    
                case DISCONNECT:
                    disconnect();
                    break;
                    
                default:
                    logger.warn("Message non géré: %s", message.getType());
            }
        }
        
        /**
         * Envoie un message au client.
         */
        public void sendMessage(NetworkMessage message) {
            if (!connected.get() || out == null) {
                return;
            }
            
            try {
                synchronized (out) {
                    out.writeObject(message);
                    out.flush();
                    out.reset(); // Éviter l'accumulation de références
                }
            } catch (IOException e) {
                logger.error("Erreur lors de l'envoi au joueur %d: %s", playerId, e.getMessage());
                disconnect();
            }
        }
        
        /**
         * Déconnecte le client proprement.
         */
        public void disconnect() {
            if (!connected.compareAndSet(true, false)) {
                return; // Déjà déconnecté
            }
            
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                logger.error("Erreur lors de la déconnexion du joueur %d", playerId);
            }
            
            clients.remove(this);
            logger.info("Joueur %d déconnecté", playerId);
            
            // Notifier le listener
            if (listener != null) {
                listener.onClientDisconnected(playerId);
            }
        }
    }
}

