package fr.uparis.informatique.cpoo5.liquidwar.config;

import fr.uparis.informatique.cpoo5.liquidwar.audio.AudioManager;

/**
 * Singleton pour stocker les options du jeu.
 * Ces options persistent entre les parties.
 */
public class GameOptions {

    private static GameOptions instance;

    // Options audio
    private int volumeLevel = 70; // 0-100

    // Options graphiques
    private String graphicsQuality = "Moyenne"; // "Basse", "Moyenne", "Élevée"

    // Options de jeu
    private int gameSpeed = 100; // 50-150%

    private GameOptions() {
        // Appliquer le volume initial
        applyVolume();
    }

    public static synchronized GameOptions getInstance() {
        if (instance == null) {
            instance = new GameOptions();
        }
        return instance;
    }

    // ========== VOLUME ==========

    public int getVolumeLevel() {
        return volumeLevel;
    }

    public void setVolumeLevel(int level) {
        this.volumeLevel = Math.max(0, Math.min(100, level));
        applyVolume();
    }

    private void applyVolume() {
        float volume = volumeLevel / 100.0f;
        AudioManager.getInstance().setMusicVolume(volume);
        AudioManager.getInstance().setSfxVolume(volume);
    }

    // ========== QUALITÉ GRAPHIQUE ==========

    public String getGraphicsQuality() {
        return graphicsQuality;
    }

    public void setGraphicsQuality(String quality) {
        this.graphicsQuality = quality;
    }

    /**
     * Retourne true si l'antialiasing doit être activé.
     * Basse = pas d'antialiasing (rapide)
     * Moyenne = antialiasing basique
     * Élevée = antialiasing complet
     */
    public boolean isAntialiasingEnabled() {
        // "Basse" = false, "Moyenne" ou "Élevée" = true
        return !"Basse".equals(graphicsQuality);
    }

    /**
     * Retourne true si l'interpolation bilinéaire doit être utilisée.
     * Uniquement pour la qualité Élevée.
     */
    public boolean isBilinearInterpolationEnabled() {
        return "Élevée".equals(graphicsQuality);
    }

    /**
     * Retourne le nombre de frames à sauter pour les qualités basses.
     * 1 = toutes les frames, 2 = une frame sur deux, etc.
     */
    public int getRenderSkipFrames() {
        switch (graphicsQuality) {
            case "Basse":
                return 3;
            case "Moyenne":
                return 2;
            case "Élevée":
            default:
                return 1;
        }
    }

    // ========== VITESSE DU JEU ==========

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int speed) {
        // 🆕 Plage élargie : 25% à 200% pour une différence TRÈS visible
        this.gameSpeed = Math.max(25, Math.min(200, speed));
    }

    /**
     * Retourne le délai du timer de logique en millisecondes.
     * 100% = 5ms (200 Hz logique - comme dans le code C)
     * 25% = 20ms (4× plus lent - super lent !)
     * 200% = 2.5ms (2× plus rapide - hyper rapide !)
     */
    public int getLogicTimerDelay() {
        // Base : 5ms à 100% (200 Hz comme le code C pour fluidité maximale)
        // Plus le gameSpeed est élevé, plus le délai est court
        // Minimum 2ms pour éviter de surcharger le CPU
        return Math.max(2, (int) (5 * 100.0 / gameSpeed));
    }

    /**
     * Retourne le multiplicateur de vitesse (1.0 = normal).
     */
    public float getSpeedMultiplier() {
        return gameSpeed / 100.0f;
    }
}
