package fr.uparis.informatique.cpoo5.liquidwar.network;

import java.io.Serializable;

/**
 * Message réseau pour la communication entre serveur et clients.
 * 
 * <p>Cette classe encapsule tous les types de messages échangés pendant
 * une partie multijoueur en réseau. Les messages sont sérialisables
 * pour faciliter l'envoi via Socket.
 * 
 * <h2>Types de messages</h2>
 * <ul>
 *   <li><b>CONNECT</b> : Demande de connexion d'un client</li>
 *   <li><b>CONNECTED</b> : Confirmation de connexion par le serveur</li>
 *   <li><b>GAME_START</b> : Début de la partie</li>
 *   <li><b>CURSOR_MOVE</b> : Déplacement du curseur d'un joueur</li>
 *   <li><b>GAME_STATE</b> : État complet du jeu (synchronisation)</li>
 *   <li><b>GAME_OVER</b> : Fin de partie</li>
 *   <li><b>DISCONNECT</b> : Déconnexion d'un joueur</li>
 *   <li><b>PING</b> : Vérification de connexion</li>
 *   <li><b>PONG</b> : Réponse au ping</li>
 * </ul>
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Types de messages réseau
     */
    public enum MessageType {
        /** Demande de connexion */
        CONNECT,
        /** Confirmation de connexion */
        CONNECTED,
        /** Mise à jour du lobby (nombre de joueurs, etc.) */
        LOBBY_UPDATE,
        /** Début de la partie */
        GAME_START,
        /** Déplacement du curseur */
        CURSOR_MOVE,
        /** État complet du jeu */
        GAME_STATE,
        /** Fin de partie */
        GAME_OVER,
        /** Déconnexion */
        DISCONNECT,
        /** Ping pour vérifier la connexion */
        PING,
        /** Réponse au ping */
        PONG,
        /** Erreur */
        ERROR
    }
    
    /** Type du message */
    private final MessageType type;
    
    /** ID du joueur émetteur */
    private final int playerId;
    
    /** Données du message (flexible) */
    private final Object data;
    
    /** Timestamp du message */
    private final long timestamp;
    
    /**
     * Construit un nouveau message réseau.
     * 
     * @param type Type du message
     * @param playerId ID du joueur émetteur
     * @param data Données associées au message
     */
    public NetworkMessage(MessageType type, int playerId, Object data) {
        this.type = type;
        this.playerId = playerId;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Construit un message réseau sans données.
     * 
     * @param type Type du message
     * @param playerId ID du joueur
     */
    public NetworkMessage(MessageType type, int playerId) {
        this(type, playerId, null);
    }
    
    // ==================== GETTERS ====================
    
    public MessageType getType() {
        return type;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public Object getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Obtient les données sous forme de chaîne de caractères.
     * 
     * @return Données en tant que String, ou null si les données ne sont pas une String
     */
    public String getDataAsString() {
        return (data instanceof String) ? (String) data : null;
    }
    
    /**
     * Obtient les données sous forme de tableau d'entiers.
     * 
     * @return Données en tant que int[], ou null si les données ne sont pas un int[]
     */
    public int[] getDataAsIntArray() {
        return (data instanceof int[]) ? (int[]) data : null;
    }
    
    /**
     * Obtient les données sous forme de CursorPosition.
     * 
     * @return Données en tant que CursorPosition, ou null si les données ne sont pas une CursorPosition
     */
    public CursorPosition getDataAsCursorPosition() {
        return (data instanceof CursorPosition) ? (CursorPosition) data : null;
    }
    
    /**
     * Obtient les données sous forme de GameStateData.
     * 
     * @return Données en tant que GameStateData, ou null si les données ne sont pas une GameStateData
     */
    public GameStateData getDataAsGameState() {
        return (data instanceof GameStateData) ? (GameStateData) data : null;
    }
    
    /**
     * Obtient les données sous forme de LobbyInfo.
     * 
     * @return Données en tant que LobbyInfo, ou null si les données ne sont pas une LobbyInfo
     */
    public LobbyInfo getDataAsLobbyInfo() {
        return (data instanceof LobbyInfo) ? (LobbyInfo) data : null;
    }
    
    @Override
    public String toString() {
        return String.format("NetworkMessage[type=%s, playerId=%d, timestamp=%d, data=%s]",
                type, playerId, timestamp, data);
    }
    
    // ==================== CLASSES INTERNES POUR DONNÉES ====================
    
    /**
     * Position du curseur pour synchronisation réseau.
     */
    public static class CursorPosition implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public final int x;
        public final int y;
        
        public CursorPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return String.format("CursorPosition(%d, %d)", x, y);
        }
    }
    
    /**
     * État du jeu complet pour synchronisation.
     */
    public static class GameStateData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** Positions des curseurs [team][x, y] */
        public final int[][] cursorPositions;
        
        /** Compteurs de fighters par équipe */
        public final int[] fighterCounts;
        
        /** Tick du jeu actuel */
        public final long gameTick;
        
        public GameStateData(int[][] cursorPositions, int[] fighterCounts, long gameTick) {
            this.cursorPositions = cursorPositions;
            this.fighterCounts = fighterCounts;
            this.gameTick = gameTick;
        }
        
        @Override
        public String toString() {
            return String.format("GameStateData[tick=%d, teams=%d]", 
                    gameTick, fighterCounts.length);
        }
    }
    
    /**
     * Informations du lobby pour synchroniser les clients.
     */
    public static class LobbyInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** Nombre de joueurs connectés */
        public final int connectedPlayers;
        
        /** Nombre minimum de joueurs pour démarrer */
        public final int minPlayers;
        
        /** Message de bienvenue */
        public final String welcomeMessage;
        
        public LobbyInfo(int connectedPlayers, int minPlayers, String welcomeMessage) {
            this.connectedPlayers = connectedPlayers;
            this.minPlayers = minPlayers;
            this.welcomeMessage = welcomeMessage;
        }
        
        @Override
        public String toString() {
            return String.format("LobbyInfo[connected=%d, min=%d]", 
                    connectedPlayers, minPlayers);
        }
    }
}

