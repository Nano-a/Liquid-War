package fr.uparis.informatique.cpoo5.liquidwar.util;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger singleton pour Liquid War.
 * 
 * <p><b>Pattern Singleton</b> : Une seule instance de logger pour toute l'application.
 * Centralise la gestion des logs (debug, info, erreurs).
 * 
 * <h2>Avantages</h2>
 * <ul>
 *   <li>✅ Accès global via getInstance()</li>
 *   <li>✅ Configuration centralisée (niveau de log, format)</li>
 *   <li>✅ Facile à remplacer par un vrai logger (Log4j, SLF4J) plus tard</li>
 *   <li>✅ Thread-safe</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // Au lieu de System.out.println
 * GameLogger.getInstance().info("Jeu démarré");
 * GameLogger.getInstance().debug("Position curseur: " + x + "," + y);
 * GameLogger.getInstance().error("Erreur: " + e.getMessage());
 * }</pre>
 * 
 * @author Liquid War Team
 * @version 1.0
 * @since 2025-12-01
 */
public class GameLogger {
    
    /**
     * Niveaux de log.
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * Instance unique (Singleton).
     */
    private static GameLogger instance;
    
    /**
     * Niveau de log actuel (par défaut INFO).
     */
    private LogLevel currentLevel = LogLevel.INFO;
    
    /**
     * Stream pour les logs (par défaut System.out).
     */
    private PrintStream outputStream = System.out;
    
    /**
     * Stream pour les erreurs (par défaut System.err).
     */
    private PrintStream errorStream = System.err;
    
    /**
     * Format de date pour les logs.
     */
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Constructeur privé (Singleton).
     */
    private GameLogger() {
        // Empêcher l'instanciation directe
    }
    
    /**
     * Obtenir l'instance unique du logger.
     * 
     * @return Instance du logger
     */
    public static synchronized GameLogger getInstance() {
        if (instance == null) {
            instance = new GameLogger();
        }
        return instance;
    }
    
    /**
     * Définir le niveau de log.
     * 
     * @param level Niveau de log (DEBUG, INFO, WARN, ERROR)
     */
    public void setLevel(LogLevel level) {
        this.currentLevel = level;
    }
    
    /**
     * Définir le stream de sortie pour les logs.
     * 
     * @param stream Stream de sortie
     */
    public void setOutputStream(PrintStream stream) {
        this.outputStream = stream;
    }
    
    /**
     * Définir le stream de sortie pour les erreurs.
     * 
     * @param stream Stream de sortie
     */
    public void setErrorStream(PrintStream stream) {
        this.errorStream = stream;
    }
    
    /**
     * Vérifier si un niveau de log doit être affiché.
     * 
     * @param level Niveau à vérifier
     * @return true si le niveau doit être affiché
     */
    private boolean shouldLog(LogLevel level) {
        return level.ordinal() >= currentLevel.ordinal();
    }
    
    /**
     * Formater un message de log.
     * 
     * @param level Niveau de log
     * @param message Message
     * @return Message formaté
     */
    private String formatMessage(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String levelStr = String.format("%-5s", level.name());
        return String.format("[%s] [%s] %s", timestamp, levelStr, message);
    }
    
    /**
     * Logger un message de debug.
     * 
     * @param message Message à logger
     */
    public void debug(String message) {
        if (shouldLog(LogLevel.DEBUG)) {
            outputStream.println(formatMessage(LogLevel.DEBUG, message));
        }
    }
    
    /**
     * Logger un message d'information.
     * 
     * @param message Message à logger
     */
    public void info(String message) {
        if (shouldLog(LogLevel.INFO)) {
            outputStream.println(formatMessage(LogLevel.INFO, message));
        }
    }
    
    /**
     * Logger un avertissement.
     * 
     * @param message Message à logger
     */
    public void warn(String message) {
        if (shouldLog(LogLevel.WARN)) {
            outputStream.println(formatMessage(LogLevel.WARN, message));
        }
    }
    
    /**
     * Logger une erreur.
     * 
     * @param message Message à logger
     */
    public void error(String message) {
        if (shouldLog(LogLevel.ERROR)) {
            errorStream.println(formatMessage(LogLevel.ERROR, message));
        }
    }
    
    /**
     * Logger une erreur avec exception.
     * 
     * @param message Message à logger
     * @param throwable Exception
     */
    public void error(String message, Throwable throwable) {
        if (shouldLog(LogLevel.ERROR)) {
            errorStream.println(formatMessage(LogLevel.ERROR, message));
            throwable.printStackTrace(errorStream);
        }
    }
    
    /**
     * Logger un message de debug avec format (comme printf).
     * 
     * @param format Format du message
     * @param args Arguments
     */
    public void debug(String format, Object... args) {
        if (shouldLog(LogLevel.DEBUG)) {
            debug(String.format(format, args));
        }
    }
    
    /**
     * Logger un message d'information avec format.
     * 
     * @param format Format du message
     * @param args Arguments
     */
    public void info(String format, Object... args) {
        if (shouldLog(LogLevel.INFO)) {
            info(String.format(format, args));
        }
    }
    
    /**
     * Logger un avertissement avec format.
     * 
     * @param format Format du message
     * @param args Arguments
     */
    public void warn(String format, Object... args) {
        if (shouldLog(LogLevel.WARN)) {
            warn(String.format(format, args));
        }
    }
    
    /**
     * Logger une erreur avec format.
     * 
     * @param format Format du message
     * @param args Arguments
     */
    public void error(String format, Object... args) {
        if (shouldLog(LogLevel.ERROR)) {
            error(String.format(format, args));
        }
    }
}

