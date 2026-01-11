package fr.uparis.informatique.cpoo5.liquidwar.network;

import java.awt.Point;
import java.io.IOException;

import fr.uparis.informatique.cpoo5.liquidwar.model.GameState;
import fr.uparis.informatique.cpoo5.liquidwar.util.GameLogger;

/**
 * Contrôleur pour gérer une partie en mode multijoueur réseau.
 * 
 * <p>
 * Cette classe fait le lien entre le modèle du jeu (GameState) et
 * le système réseau (NetworkServer/NetworkClient). Elle synchronise
 * l'état du jeu entre les joueurs connectés.
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class NetworkGameController {

    private GameState gameState;
    private NetworkServer server;
    private NetworkClient client;
    private boolean isServer;
    private int myTeamId;
    private long gameTick;
    private int minPlayers = 2; // Nombre minimum de joueurs (défini par le serveur)

    // Listener pour les mises à jour du lobby (utilisé par le client)
    private LobbyUpdateListener lobbyUpdateListener;

    private final GameLogger logger = GameLogger.getInstance();

    /**
     * Interface pour recevoir les mises à jour du lobby.
     */
    public interface LobbyUpdateListener {
        void onLobbyUpdate(int connectedPlayers, int minPlayers);
    }

    /**
     * Crée un nouveau contrôleur de jeu réseau.
     * 
     * @param gameState État du jeu à synchroniser (peut être null initialement)
     */
    public NetworkGameController(GameState gameState) {
        this.gameState = gameState;
        this.isServer = false;
        this.myTeamId = 0;
        this.gameTick = 0;
    }

    /**
     * Définit le listener pour les mises à jour du lobby.
     */
    public void setLobbyUpdateListener(LobbyUpdateListener listener) {
        this.lobbyUpdateListener = listener;
    }

    /**
     * Définit l'état du jeu (appelé après la création du GameCanvas).
     * 
     * @param gameState État du jeu
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Définit le nombre minimum de joueurs (mode serveur).
     * 
     * @param minPlayers Nombre minimum de joueurs
     */
    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    /**
     * Obtient le nombre minimum de joueurs.
     * 
     * @return Nombre minimum de joueurs
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Démarre un serveur de jeu.
     * 
     * @param port Port d'écoute
     * @throws IOException Si le serveur ne peut pas être démarré
     */
    public void startServer(int port) throws IOException {
        if (server != null || client != null) {
            throw new IllegalStateException("Déjà connecté");
        }

        isServer = true;
        myTeamId = 0; // Le serveur est toujours l'équipe 0

        server = new NetworkServer(port);
        server.setListener(new ServerListener());
        server.start();

        logger.info("Serveur démarré - Vous êtes l'équipe %d", myTeamId);
    }

    /**
     * Se connecte à un serveur distant.
     * 
     * @param host Adresse du serveur
     * @param port Port du serveur
     * @throws IOException Si la connexion échoue
     */
    public void connectToServer(String host, int port) throws IOException {
        if (server != null || client != null) {
            throw new IllegalStateException("Déjà connecté");
        }

        isServer = false;
        myTeamId = 1; // Le client est l'équipe 1 par défaut (sera mis à jour par le serveur)

        client = new NetworkClient(host, port);
        client.setListener(new ClientListener());
        client.connect();

        logger.info("Connexion au serveur %s:%d...", host, port);
    }

    /**
     * Arrête le réseau (serveur ou client).
     */
    public void shutdown() {
        if (server != null) {
            server.stop();
            server = null;
        }

        if (client != null) {
            client.disconnect();
            client = null;
        }

        logger.info("Réseau arrêté");
    }

    /**
     * Envoie un mouvement de curseur au réseau.
     * 
     * @param x Position X
     * @param y Position Y
     */
    public void sendCursorMove(int x, int y) {
        if (client != null) {
            client.sendCursorMove(x, y);
        }
    }

    /**
     * Broadcast un message de début de partie à tous les clients.
     */
    public void broadcastGameStart() {
        if (server != null) {
            NetworkMessage startMsg = new NetworkMessage(
                    NetworkMessage.MessageType.GAME_START, -1, "La partie commence!");
            server.broadcast(startMsg);
            logger.info("📢 Message GAME_START envoyé à tous les clients");
        }
    }

    /**
     * Broadcast une mise à jour du lobby à tous les clients.
     */
    public void broadcastLobbyUpdate() {
        if (server != null) {
            int connectedPlayers = getPlayerCount();
            NetworkMessage.LobbyInfo lobbyInfo = new NetworkMessage.LobbyInfo(
                    connectedPlayers, minPlayers, "Mise à jour du lobby");
            NetworkMessage lobbyMsg = new NetworkMessage(
                    NetworkMessage.MessageType.LOBBY_UPDATE, -1, lobbyInfo);
            server.broadcast(lobbyMsg);
            logger.info("📢 LOBBY_UPDATE envoyé: %d/%d joueurs", connectedPlayers, minPlayers);
        }
    }

    /**
     * Incrémente le tick du jeu (appelé à chaque frame).
     */
    public void tick() {
        gameTick++;
    }

    /**
     * Obtient l'ID de l'équipe du joueur local.
     * 
     * @return ID de l'équipe
     */
    public int getMyTeamId() {
        return myTeamId;
    }

    /**
     * Vérifie si ce joueur est le serveur.
     * 
     * @return true si serveur
     */
    public boolean isServer() {
        return isServer;
    }

    /**
     * Vérifie si le réseau est actif.
     * 
     * @return true si connecté (serveur ou client)
     */
    public boolean isConnected() {
        return (server != null && server.isRunning()) ||
                (client != null && client.isConnected());
    }

    /**
     * Obtient le nombre de joueurs connectés (mode serveur uniquement).
     * 
     * @return Nombre de joueurs (1 = serveur seul), ou 0 si client
     */
    public int getPlayerCount() {
        if (server != null) {
            return server.getClientCount() + 1; // +1 pour le serveur lui-même
        }
        return 0;
    }

    // ==================== LISTENER SERVEUR ====================

    /**
     * Gère les événements du serveur.
     */
    private class ServerListener implements NetworkServer.NetworkServerListener {

        @Override
        public void onClientConnected(int playerId) {
            logger.info("📥 Joueur %d a rejoint la partie", playerId + 1);

            // Envoyer les infos du lobby au nouveau client
            int connectedPlayers = getPlayerCount();
            NetworkMessage.LobbyInfo lobbyInfo = new NetworkMessage.LobbyInfo(
                    connectedPlayers, minPlayers, "Bienvenue!");
            NetworkMessage lobbyMsg = new NetworkMessage(
                    NetworkMessage.MessageType.LOBBY_UPDATE, -1, lobbyInfo);
            server.sendToPlayer(playerId, lobbyMsg);

            // Envoyer la mise à jour à tous les autres clients aussi
            broadcastLobbyUpdate();
        }

        @Override
        public void onClientDisconnected(int playerId) {
            logger.info("📤 Joueur %d a quitté la partie", playerId + 1);

            // Envoyer la mise à jour du lobby à tous
            broadcastLobbyUpdate();
        }

        @Override
        public void onCursorMoved(int playerId, int x, int y) {
            int teamId = playerId + 1;
            if (gameState != null && teamId < gameState.getActiveTeams()) {
                gameState.setCursorPosition(teamId, x, y);
            }
        }

        @Override
        public NetworkMessage.GameStateData getCurrentGameState() {
            return buildGameStateData();
        }
    }

    // ==================== LISTENER CLIENT ====================

    /**
     * Gère les événements du client.
     */
    private class ClientListener implements NetworkClient.NetworkClientListener {

        @Override
        public void onConnected(int playerId, String message) {
            myTeamId = playerId + 1;
            logger.info("✅ Connecté - Vous êtes l'équipe %d (Joueur %d)", myTeamId, playerId + 1);
            logger.info("Message du serveur: %s", message);
        }

        @Override
        public void onGameStart() {
            logger.info("🎮 La partie commence!");
        }

        @Override
        public void onGameStateReceived(NetworkMessage.GameStateData state) {
            applyGameState(state);
        }

        @Override
        public void onCursorMoved(int playerId, int x, int y) {
            int teamId = playerId + 1;
            if (gameState != null && teamId != myTeamId && teamId < gameState.getActiveTeams()) {
                gameState.setCursorPosition(teamId, x, y);
            }
        }

        @Override
        public void onGameOver(int winnerId) {
            logger.info("🏆 Partie terminée! Gagnant: Équipe %d", winnerId);
        }

        @Override
        public void onError(String error) {
            logger.error("❌ Erreur réseau: %s", error);
        }

        @Override
        public void onDisconnected() {
            logger.warn("⚠️ Déconnecté du serveur");
        }

        @Override
        public void onLobbyUpdate(int connectedPlayers, int serverMinPlayers) {
            minPlayers = serverMinPlayers;
            logger.info("📋 Lobby mis à jour: %d/%d joueurs", connectedPlayers, serverMinPlayers);

            // Notifier le listener externe
            if (lobbyUpdateListener != null) {
                lobbyUpdateListener.onLobbyUpdate(connectedPlayers, serverMinPlayers);
            }
        }
    }

    /**
     * Traite un message LOBBY_UPDATE reçu (côté client).
     * 
     * @param lobbyInfo Informations du lobby
     */
    public void handleLobbyUpdate(NetworkMessage.LobbyInfo lobbyInfo) {
        if (lobbyInfo != null) {
            this.minPlayers = lobbyInfo.minPlayers;
            logger.info("📋 Lobby mis à jour: %d/%d joueurs",
                    lobbyInfo.connectedPlayers, lobbyInfo.minPlayers);

            // Notifier le listener
            if (lobbyUpdateListener != null) {
                lobbyUpdateListener.onLobbyUpdate(lobbyInfo.connectedPlayers, lobbyInfo.minPlayers);
            }
        }
    }

    // ==================== UTILITAIRES ====================

    /**
     * Construit les données d'état du jeu pour la synchronisation.
     */
    private NetworkMessage.GameStateData buildGameStateData() {
        if (gameState == null) {
            return null;
        }

        int teams = gameState.getActiveTeams();
        int[][] cursorPositions = new int[teams][2];
        for (int i = 0; i < teams; i++) {
            Point pos = gameState.getCursorPosition(i);
            cursorPositions[i][0] = pos.x;
            cursorPositions[i][1] = pos.y;
        }

        int[] fighterCounts = gameState.getFighterCountsCopy();
        return new NetworkMessage.GameStateData(cursorPositions, fighterCounts, gameTick);
    }

    /**
     * Applique un état de jeu reçu du réseau.
     */
    private void applyGameState(NetworkMessage.GameStateData state) {
        if (state == null || gameState == null) {
            return;
        }

        for (int i = 0; i < state.cursorPositions.length; i++) {
            if (i != myTeamId && i < gameState.getActiveTeams()) {
                int x = state.cursorPositions[i][0];
                int y = state.cursorPositions[i][1];
                gameState.setCursorPosition(i, x, y);
            }
        }

        for (int i = 0; i < state.fighterCounts.length && i < gameState.getActiveTeams(); i++) {
            gameState.setFighterCount(i, state.fighterCounts[i]);
        }

        this.gameTick = state.gameTick;
    }
}
