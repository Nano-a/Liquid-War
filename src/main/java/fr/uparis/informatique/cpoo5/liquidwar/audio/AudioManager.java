package fr.uparis.informatique.cpoo5.liquidwar.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Gestionnaire audio pour Liquid War
 * Gère la musique MIDI et les effets sonores WAV
 * Basé sur le système audio du code C original
 */
public class AudioManager {

    // Singleton
    private static AudioManager instance;

    // Séquenceur MIDI pour la musique
    private Sequencer sequencer;
    private Synthesizer synthesizer;
    private Sequence currentMusicSequence;

    // Contrôles de volume (0.0 à 1.0)
    private float musicVolume = 0.5f;
    private float sfxVolume = 0.5f;

    // État
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private boolean musicPlaying = false;

    /**
     * Constructeur privé (singleton)
     */
    private AudioManager() {
        try {
            // Initialiser le synthétiseur et le séquenceur MIDI
            synthesizer = MidiSystem.getSynthesizer();
            sequencer = MidiSystem.getSequencer(false); // false = pas de connexion automatique

            if (synthesizer == null || sequencer == null) {
                System.err.println("⚠️ Séquenceur ou Synthétiseur MIDI non disponible");
            } else {
                synthesizer.open();
                sequencer.open();

                // Connecter le séquenceur au synthétiseur
                Transmitter transmitter = sequencer.getTransmitter();
                Receiver receiver = synthesizer.getReceiver();
                transmitter.setReceiver(receiver);

                System.out.println("✅ Séquenceur MIDI connecté au Synthétiseur");
            }
        } catch (MidiUnavailableException e) {
            System.err.println("❌ Erreur d'initialisation MIDI : " + e.getMessage());
        }
    }

    /**
     * Obtenir l'instance unique
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Charger et jouer une musique MIDI (boucle infinie)
     * 
     * @param resourcePath Chemin vers le fichier MIDI (ex: "/music/fodder.mid")
     */
    public void playMusic(String resourcePath) {
        if (!musicEnabled) {
            System.out.println("🎵 Musique désactivée, lecture ignorée");
            return;
        }

        // Essayer plusieurs fois en cas d'échec (problème de timing)
        int maxRetries = 3;
        int retryDelay = 100; // ms

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    System.out.println("🔄 Tentative " + attempt + "/" + maxRetries + " de lancement de la musique...");
                    Thread.sleep(retryDelay * attempt); // Délai progressif
                }

                // Réinitialiser le séquenceur s'il a été fermé
                ensureSequencerOpen();

                if (sequencer == null) {
                    System.err
                            .println("❌ Séquenceur MIDI non disponible (tentative " + attempt + "/" + maxRetries + ")");
                    if (attempt < maxRetries)
                        continue;
                    return;
                }

                // Vérifier que le séquenceur est vraiment ouvert
                if (!sequencer.isOpen()) {
                    System.err.println("⚠️ Séquenceur fermé, tentative de réouverture...");
                    sequencer.open();
                    // Reconnecter au synthétiseur
                    if (synthesizer != null && synthesizer.isOpen()) {
                        try {
                            Transmitter transmitter = sequencer.getTransmitter();
                            Receiver receiver = synthesizer.getReceiver();
                            transmitter.setReceiver(receiver);
                        } catch (MidiUnavailableException e) {
                            System.err.println("⚠️ Impossible de reconnecter le séquenceur : " + e.getMessage());
                        }
                    }
                }

                // Arrêter la musique actuelle si elle joue
                stopMusic();

                // Attendre un peu pour que le séquenceur se stabilise
                Thread.sleep(50);

                // Charger le fichier MIDI
                InputStream is = getClass().getResourceAsStream(resourcePath);
                if (is == null) {
                    System.err.println("❌ Fichier audio introuvable : " + resourcePath);
                    // Essayer avec un chemin alternatif
                    is = AudioManager.class.getResourceAsStream(resourcePath);
                    if (is == null) {
                        System.err.println("❌ Fichier audio introuvable (tentative alternative) : " + resourcePath);
                        if (attempt < maxRetries)
                            continue;
                        return;
                    }
                }

                BufferedInputStream bis = new BufferedInputStream(is);
                currentMusicSequence = MidiSystem.getSequence(bis);
                bis.close();
                is.close();

                // Configurer le séquenceur
                sequencer.setSequence(currentMusicSequence);
                sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY); // Boucle infinie

                // Appliquer le volume
                applyMusicVolume();

                // Démarrer la lecture
                sequencer.start();

                // Vérifier que la musique a bien démarré
                Thread.sleep(100); // Petit délai pour laisser le séquenceur démarrer
                if (sequencer.isRunning()) {
                    musicPlaying = true;
                    System.out.println("🎵 Musique lancée avec succès : " + resourcePath);
                    return; // Succès !
                } else {
                    System.err.println(
                            "⚠️ Séquenceur démarré mais ne joue pas (tentative " + attempt + "/" + maxRetries + ")");
                    if (attempt < maxRetries)
                        continue;
                }

            } catch (InvalidMidiDataException | IOException e) {
                System.err.println(
                        "❌ Erreur de lecture MIDI (tentative " + attempt + "/" + maxRetries + ") : " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    continue;
                }
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Interruption lors du lancement de la musique");
                return;
            } catch (Exception e) {
                System.err.println("❌ Erreur inattendue lors du lancement de la musique (tentative " + attempt + "/"
                        + maxRetries + ") : " + e.getMessage());
                e.printStackTrace();
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    continue;
                }
            }
        }

        System.err.println("❌ Impossible de lancer la musique après " + maxRetries + " tentatives");
    }

    /**
     * S'assurer que le séquenceur et le synthétiseur sont ouverts (les réouvrir si
     * nécessaire)
     */
    private void ensureSequencerOpen() {
        try {
            // Réinitialiser complètement si nécessaire
            if (synthesizer == null) {
                synthesizer = MidiSystem.getSynthesizer();
                if (synthesizer != null) {
                    System.out.println("🔄 Synthétiseur MIDI recréé");
                }
            }
            if (sequencer == null) {
                sequencer = MidiSystem.getSequencer(false);
                if (sequencer != null) {
                    System.out.println("🔄 Séquenceur MIDI recréé");
                }
            }

            // Ouvrir le synthétiseur si nécessaire
            if (synthesizer != null && !synthesizer.isOpen()) {
                synthesizer.open();
                System.out.println("✅ Synthétiseur MIDI réouvert");
            }

            // Ouvrir le séquenceur si nécessaire
            if (sequencer != null && !sequencer.isOpen()) {
                sequencer.open();
                System.out.println("✅ Séquenceur MIDI réouvert");

                // Reconnecter le séquenceur au synthétiseur
                if (synthesizer != null && synthesizer.isOpen()) {
                    try {
                        // Fermer l'ancien transmitter s'il existe
                        Transmitter transmitter = sequencer.getTransmitter();
                        if (transmitter != null) {
                            transmitter.close();
                        }
                        // Créer une nouvelle connexion
                        transmitter = sequencer.getTransmitter();
                        Receiver receiver = synthesizer.getReceiver();
                        if (receiver != null) {
                            transmitter.setReceiver(receiver);
                            System.out.println("✅ Séquenceur MIDI reconnecté au synthétiseur");
                        }
                    } catch (MidiUnavailableException e) {
                        System.err.println("⚠️ Impossible de reconnecter le séquenceur : " + e.getMessage());
                    }
                }
            } else if (sequencer != null && sequencer.isOpen()) {
                // Vérifier que la connexion est toujours active
                try {
                    if (synthesizer != null && synthesizer.isOpen()) {
                        // Tester la connexion en vérifiant le transmitter
                        Transmitter transmitter = sequencer.getTransmitter();
                        if (transmitter == null || transmitter.getReceiver() == null) {
                            // Reconnecter si nécessaire
                            if (transmitter != null) {
                                transmitter.close();
                            }
                            transmitter = sequencer.getTransmitter();
                            Receiver receiver = synthesizer.getReceiver();
                            if (receiver != null) {
                                transmitter.setReceiver(receiver);
                                System.out.println("✅ Connexion MIDI vérifiée et réparée");
                            }
                        }
                    }
                } catch (MidiUnavailableException e) {
                    System.err.println("⚠️ Erreur lors de la vérification de la connexion MIDI : " + e.getMessage());
                }
            }
        } catch (MidiUnavailableException e) {
            System.err.println("❌ Impossible de réouvrir le séquenceur MIDI : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Arrêter la musique
     */
    public void stopMusic() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
            musicPlaying = false;
            System.out.println("⏹️ Musique arrêtée");
        }
    }

    /**
     * Mettre en pause / reprendre la musique
     */
    public void toggleMusicPause() {
        if (sequencer == null)
            return;

        if (sequencer.isRunning()) {
            sequencer.stop();
            musicPlaying = false;
            System.out.println("⏸️ Musique en pause");
        } else if (currentMusicSequence != null) {
            sequencer.start();
            musicPlaying = true;
            System.out.println("▶️ Musique reprise");
        }
    }

    /**
     * Jouer un effet sonore WAV
     * 
     * @param resourcePath Chemin vers le fichier WAV (ex: "/sfx/splash1.wav")
     */
    public void playSoundEffect(String resourcePath) {
        if (!sfxEnabled) {
            return;
        }

        // Jouer dans un thread séparé pour ne pas bloquer
        new Thread(() -> {
            try {
                // Charger le fichier WAV
                InputStream is = getClass().getResourceAsStream(resourcePath);
                if (is == null) {
                    System.err.println("❌ Fichier son introuvable : " + resourcePath);
                    return;
                }

                BufferedInputStream bis = new BufferedInputStream(is);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);

                // Créer un clip audio
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                // Appliquer le volume
                applySfxVolume(clip);

                // Jouer le son
                clip.start();

                // Attendre la fin et fermer
                Thread.sleep(clip.getMicrosecondLength() / 1000);
                clip.close();
                audioStream.close();

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
                System.err.println("❌ Erreur de lecture WAV : " + e.getMessage());
            }
        }).start();
    }

    /**
     * Définir le volume de la musique (0.0 à 1.0)
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        applyMusicVolume();
        System.out.println("🎵 Volume musique : " + (int) (musicVolume * 100) + "%");
    }

    /**
     * Définir le volume des effets sonores (0.0 à 1.0)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("🔊 Volume SFX : " + (int) (sfxVolume * 100) + "%");
    }

    /**
     * Obtenir le volume de la musique
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Obtenir le volume des effets sonores
     */
    public float getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Activer/désactiver la musique
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled && musicPlaying) {
            stopMusic();
        }
        System.out.println("🎵 Musique : " + (enabled ? "Activée" : "Désactivée"));
    }

    /**
     * Activer/désactiver les effets sonores
     */
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
        System.out.println("🔊 Effets sonores : " + (enabled ? "Activés" : "Désactivés"));
    }

    /**
     * Vérifier si la musique est activée
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Vérifier si les effets sonores sont activés
     */
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    /**
     * Appliquer le volume à la musique MIDI
     * Utilise directement le Synthesizer pour un contrôle fiable du volume.
     */
    private void applyMusicVolume() {
        if (synthesizer == null || !synthesizer.isOpen()) {
            return;
        }

        try {
            // Le volume MIDI va de 0 à 127
            int midiVolume = (int) (musicVolume * 127);

            // Appliquer le volume sur tous les canaux du synthétiseur
            MidiChannel[] channels = synthesizer.getChannels();
            if (channels != null) {
                for (MidiChannel channel : channels) {
                    if (channel != null) {
                        channel.controlChange(7, midiVolume); // Main Volume (CC7)
                        channel.controlChange(11, midiVolume); // Expression (CC11)
                    }
                }
                System.out.println("✅ Volume MIDI appliqué via Synthesizer : " + midiVolume + "/127 ("
                        + (int) (musicVolume * 100) + "%)");
            }

            // Si le volume est à 0, couper le son sur tous les canaux
            if (musicVolume <= 0.0f) {
                for (MidiChannel channel : channels) {
                    if (channel != null) {
                        channel.allSoundOff();
                    }
                }
                System.out.println("🔇 Son MIDI coupé (volume = 0)");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'ajustement du volume MIDI : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Appliquer le volume à un effet sonore WAV
     */
    private void applySfxVolume(Clip clip) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            // Convertir le volume linéaire (0.0-1.0) en décibels
            // Volume 0.0 = silence (-80 dB)
            // Volume 1.0 = maximum (0 dB)
            float dB;
            if (sfxVolume <= 0.0f) {
                dB = gainControl.getMinimum();
            } else {
                // Formule : dB = 20 * log10(volume)
                // Mais on utilise une approximation linéaire plus simple
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                dB = min + (max - min) * sfxVolume;
            }

            gainControl.setValue(dB);

        } catch (IllegalArgumentException e) {
            // Le contrôle de volume n'est pas disponible pour ce clip
            System.err.println("⚠️ Contrôle de volume non disponible pour ce son");
        }
    }

    /**
     * Fermer le gestionnaire audio (libérer les ressources)
     */
    public void shutdown() {
        stopMusic();
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.close();
        }
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
        System.out.println("🔇 AudioManager fermé");
    }
}
