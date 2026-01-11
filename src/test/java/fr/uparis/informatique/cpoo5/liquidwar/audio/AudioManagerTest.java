package fr.uparis.informatique.cpoo5.liquidwar.audio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour AudioManager.
 * 
 * Teste :
 * - Singleton pattern
 * - Gestion du volume
 * - Activation/désactivation
 */
class AudioManagerTest {

    private AudioManager audioManager;

    @BeforeEach
    void setUp() {
        audioManager = AudioManager.getInstance();
    }

    @Test
    @DisplayName("getInstance retourne la même instance (Singleton)")
    void testSingleton() {
        AudioManager instance1 = AudioManager.getInstance();
        AudioManager instance2 = AudioManager.getInstance();

        assertSame(instance1, instance2, "getInstance doit retourner la même instance");
    }

    @Test
    @DisplayName("setMusicVolume limite les valeurs entre 0 et 1")
    void testSetMusicVolume() {
        audioManager.setMusicVolume(1.5f);
        assertTrue(audioManager.getMusicVolume() <= 1.0f, "Volume doit être limité à 1.0");

        audioManager.setMusicVolume(-0.5f);
        assertTrue(audioManager.getMusicVolume() >= 0.0f, "Volume doit être limité à 0.0");

        audioManager.setMusicVolume(0.7f);
        assertEquals(0.7f, audioManager.getMusicVolume(), 0.01f, "Volume doit être 0.7");
    }

    @Test
    @DisplayName("setSfxVolume limite les valeurs entre 0 et 1")
    void testSetSfxVolume() {
        audioManager.setSfxVolume(1.5f);
        assertTrue(audioManager.getSfxVolume() <= 1.0f, "Volume SFX doit être limité à 1.0");

        audioManager.setSfxVolume(-0.5f);
        assertTrue(audioManager.getSfxVolume() >= 0.0f, "Volume SFX doit être limité à 0.0");

        audioManager.setSfxVolume(0.8f);
        assertEquals(0.8f, audioManager.getSfxVolume(), 0.01f, "Volume SFX doit être 0.8");
    }

    @Test
    @DisplayName("setMusicEnabled active/désactive la musique")
    void testSetMusicEnabled() {
        audioManager.setMusicEnabled(true);
        assertTrue(audioManager.isMusicEnabled(), "La musique doit être activée");

        audioManager.setMusicEnabled(false);
        assertFalse(audioManager.isMusicEnabled(), "La musique doit être désactivée");
    }

    @Test
    @DisplayName("setSfxEnabled active/désactive les effets")
    void testSetSfxEnabled() {
        audioManager.setSfxEnabled(true);
        assertTrue(audioManager.isSfxEnabled(), "Les effets doivent être activés");

        audioManager.setSfxEnabled(false);
        assertFalse(audioManager.isSfxEnabled(), "Les effets doivent être désactivés");
    }

    @Test
    @DisplayName("stopMusic ne crash pas")
    void testStopMusic() {
        assertDoesNotThrow(() -> audioManager.stopMusic(),
                "stopMusic ne doit pas lever d'exception");
    }

    @Test
    @DisplayName("shutdown ne crash pas")
    void testShutdown() {
        assertDoesNotThrow(() -> audioManager.shutdown(),
                "shutdown ne doit pas lever d'exception");
    }
}
