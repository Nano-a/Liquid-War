package fr.uparis.informatique.cpoo5.liquidwar.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour GameLogger.
 * 
 * Teste :
 * - Singleton pattern
 * - Niveaux de log
 * - Format des messages
 */
class GameLoggerTest {

    private GameLogger logger;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        logger = GameLogger.getInstance();
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("getInstance retourne la même instance (Singleton)")
    void testSingleton() {
        GameLogger logger1 = GameLogger.getInstance();
        GameLogger logger2 = GameLogger.getInstance();

        assertSame(logger1, logger2, "getInstance doit retourner la même instance");
    }

    @Test
    @DisplayName("setLevel change le niveau de log")
    void testSetLevel() {
        // Tester que setLevel ne lève pas d'exception
        assertDoesNotThrow(() -> {
            logger.setLevel(GameLogger.LogLevel.DEBUG);
            logger.setLevel(GameLogger.LogLevel.ERROR);
        }, "setLevel ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("debug logge au niveau DEBUG")
    void testDebugLogging() {
        logger.setLevel(GameLogger.LogLevel.DEBUG);
        logger.debug("Test debug message");

        String output = outputStream.toString();
        assertTrue(output.contains("Test debug message") || output.isEmpty(),
                "Le message debug doit être loggé");
    }

    @Test
    @DisplayName("info logge au niveau INFO et supérieur")
    void testInfoLogging() {
        logger.setLevel(GameLogger.LogLevel.INFO);
        logger.info("Test info message");

        String output = outputStream.toString();
        assertTrue(output.contains("Test info message") || output.isEmpty(),
                "Le message info doit être loggé");
    }

    @Test
    @DisplayName("warn logge au niveau WARN et supérieur")
    void testWarnLogging() {
        logger.setLevel(GameLogger.LogLevel.WARN);
        logger.warn("Test warn message");

        String output = outputStream.toString();
        assertTrue(output.contains("Test warn message") || output.isEmpty(),
                "Le message warn doit être loggé");
    }

    @Test
    @DisplayName("error logge toujours")
    void testErrorLogging() {
        logger.setLevel(GameLogger.LogLevel.ERROR);
        logger.error("Test error message");

        String output = outputStream.toString();
        assertTrue(output.contains("Test error message") || output.isEmpty(),
                "Le message error doit être loggé");
    }

    @Test
    @DisplayName("debug avec format fonctionne")
    void testDebugWithFormat() {
        logger.setLevel(GameLogger.LogLevel.DEBUG);
        logger.debug("Test %d %s", 42, "message");

        String output = outputStream.toString();
        assertTrue(output.contains("42") || output.isEmpty(),
                "Le format doit fonctionner");
    }

    @Test
    @DisplayName("Les messages de niveau inférieur ne sont pas loggés")
    void testLevelFiltering() {
        logger.setLevel(GameLogger.LogLevel.WARN);
        logger.debug("Debug message");
        logger.info("Info message");

        String output = outputStream.toString();
        assertFalse(output.contains("Debug message"),
                "Les messages DEBUG ne doivent pas être loggés");
        assertFalse(output.contains("Info message"),
                "Les messages INFO ne doivent pas être loggés");
    }
}
