package fr.uparis.informatique.cpoo5.liquidwar.network;

/**
 * Constantes et utilitaires pour le protocole réseau de Liquid War.
 * 
 * <p>Cette classe définit les constantes utilisées par le système réseau,
 * notamment le port par défaut, les timeouts et les limites.
 * 
 * <h2>Configuration Réseau</h2>
 * <ul>
 *   <li><b>Port</b> : 14000 (par défaut)</li>
 *   <li><b>Max joueurs</b> : 4 équipes simultanées</li>
 *   <li><b>Timeout</b> : 30 secondes</li>
 *   <li><b>Ping interval</b> : 5 secondes</li>
 * </ul>
 * 
 * @author Liquid War Team
 * @version 2.0
 * @since 2025-12-02
 */
public final class NetworkProtocol {
    
    // ==================== CONSTANTES RÉSEAU ====================
    
    /** Port TCP par défaut pour le serveur Liquid War */
    public static final int DEFAULT_PORT = 14000;
    
    /** Nombre maximum de joueurs simultanés */
    public static final int MAX_PLAYERS = 4;
    
    /** Timeout de connexion en millisecondes (30 secondes) */
    public static final int CONNECTION_TIMEOUT_MS = 30000;
    
    /** Intervalle entre les pings en millisecondes (5 secondes) */
    public static final int PING_INTERVAL_MS = 5000;
    
    /** Timeout pour considérer un client déconnecté (15 secondes) */
    public static final int CLIENT_TIMEOUT_MS = 15000;
    
    /** Intervalle de synchronisation de l'état du jeu (100ms = 10 fois/seconde) */
    public static final int SYNC_INTERVAL_MS = 100;
    
    // ==================== MESSAGES SYSTÈME ====================
    
    /** Message de bienvenue envoyé par le serveur */
    public static final String WELCOME_MESSAGE = "Bienvenue sur le serveur Liquid War!";
    
    /** Message d'attente de joueurs */
    public static final String WAITING_FOR_PLAYERS = "En attente d'autres joueurs...";
    
    /** Message de début de partie */
    public static final String GAME_STARTING = "La partie commence!";
    
    /** Message de serveur plein */
    public static final String SERVER_FULL = "Le serveur est plein (max %d joueurs).";
    
    // ==================== CONSTRUCTEUR PRIVÉ ====================
    
    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe ne contient que des constantes.
     */
    private NetworkProtocol() {
        throw new AssertionError("NetworkProtocol ne peut pas être instancié");
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Valide un numéro de port.
     * 
     * @param port Port à valider
     * @return true si le port est valide (1-65535), false sinon
     */
    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }
    
    /**
     * Valide un ID de joueur.
     * 
     * @param playerId ID du joueur à valider
     * @return true si l'ID est valide (0 à MAX_PLAYERS-1), false sinon
     */
    public static boolean isValidPlayerId(int playerId) {
        return playerId >= 0 && playerId < MAX_PLAYERS;
    }
    
    /**
     * Obtient le message de serveur plein formaté.
     * 
     * @return Message formaté
     */
    public static String getServerFullMessage() {
        return String.format(SERVER_FULL, MAX_PLAYERS);
    }
}

