package fr.uparis.informatique.cpoo5.liquidwar.view;

import java.awt.image.BufferedImage;
import java.awt.Color;

/**
 * Système de distorsion de pixels pour effet d'eau - Inspiration de distor.c.
 * 
 * Cette classe reproduit les effets de vague du code C (distor.c) pour
 * donner l'impression que les particules "ondulent" comme un vrai liquide.
 * 
 * PRINCIPE (comme dans distor.c, lignes 98-397) :
 * 1. Créer des formes d'onde sinusoïdales (WAVE_SHAPE_WX, WAVE_SHAPE_WY)
 * 2. Déplacer chaque pixel selon ces ondes
 * 3. Créer un effet de "glouglou" (ondulation d'eau)
 * 
 * NOTE : Version simplifiée car Java n'a pas l'accès direct aux pixels
 * comme en C avec Allegro. Utilise BufferedImage pour manipulation.
 */
public class WaterDistortionEffect {
    
    // ===== CONSTANTES (comme dans distor.c) =====
    private static final int DISTORSION_PRECISION = 4096;
    private static final int WAVE_SIZE_SCALE = 4;
    private static final double PI = Math.PI;
    
    // ===== PARAMÈTRES DE VAGUE =====
    private final int width;
    private final int height;
    private final int waveAmplitude;    // Amplitude des vagues
    private final int waveFrequency;    // Fréquence des vagues
    private final double waveSpeed;     // Vitesse d'animation
    
    // ===== TABLES DE FORMES D'ONDE =====
    private final double[] waveShapeWX;  // Déformation horizontale
    private final double[] waveShapeWY;  // Déformation verticale
    private final double[] waveShapeHX;  // Déformation hauteur X
    private final double[] waveShapeHY;  // Déformation hauteur Y
    
    // ===== ÉTAT D'ANIMATION =====
    private double animationPhase = 0.0;
    private boolean enabled = false;
    
    /**
     * Constructeur avec paramètres par défaut.
     * 
     * @param width Largeur de l'image
     * @param height Hauteur de l'image
     */
    public WaterDistortionEffect(int width, int height) {
        this(width, height, 3, 2, 0.1);  // Paramètres modérés pour fluidité
    }
    
    /**
     * Constructeur complet.
     * 
     * @param width Largeur
     * @param height Hauteur
     * @param waveAmplitude Amplitude des vagues (pixels)
     * @param waveFrequency Fréquence (nombre de vagues)
     * @param waveSpeed Vitesse d'animation
     */
    public WaterDistortionEffect(int width, int height, int waveAmplitude, 
                                 int waveFrequency, double waveSpeed) {
        this.width = width;
        this.height = height;
        this.waveAmplitude = waveAmplitude;
        this.waveFrequency = waveFrequency;
        this.waveSpeed = waveSpeed;
        
        // Initialiser les tables de formes d'onde
        this.waveShapeWX = new double[width];
        this.waveShapeWY = new double[width];
        this.waveShapeHX = new double[height];
        this.waveShapeHY = new double[height];
        
        initWaveShapes();
    }
    
    /**
     * Initialise les formes d'onde sinusoïdales.
     * 
     * Inspiré de create_wave_line() dans distor.c (lignes 158-230).
     */
    private void initWaveShapes() {
        // Vagues horizontales (axe X)
        for (int x = 0; x < width; x++) {
            double angle = (double) x / width * waveFrequency * 2 * PI;
            waveShapeWX[x] = Math.sin(angle) * waveAmplitude;
            waveShapeWY[x] = Math.cos(angle) * waveAmplitude * 0.5;
        }
        
        // Vagues verticales (axe Y)
        for (int y = 0; y < height; y++) {
            double angle = (double) y / height * waveFrequency * 2 * PI;
            waveShapeHX[y] = Math.cos(angle) * waveAmplitude * 0.5;
            waveShapeHY[y] = Math.sin(angle) * waveAmplitude;
        }
    }
    
    /**
     * ⭐ Applique la distorsion d'eau à une image.
     * 
     * Inspiré de la boucle principale de distor.c (lignes 318-393).
     * 
     * @param source Image source
     * @return Image distordue avec effet d'eau
     */
    public BufferedImage applyDistortion(BufferedImage source) {
        if (!enabled || source == null) {
            return source;  // Pas d'effet si désactivé
        }
        
        BufferedImage distorted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Incrémenter la phase d'animation
        animationPhase += waveSpeed;
        
        // Pour chaque pixel de destination
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calculer la déformation pour ce pixel
                double offsetX = waveShapeWX[x % waveShapeWX.length] * Math.sin(animationPhase + y * 0.1);
                double offsetY = waveShapeHY[y % waveShapeHY.length] * Math.cos(animationPhase + x * 0.1);
                
                // Position source (avec déformation)
                int srcX = (int) (x + offsetX);
                int srcY = (int) (y + offsetY);
                
                // Vérifier les bornes (avec wrapping pour effet continu)
                srcX = (srcX + width) % width;
                srcY = (srcY + height) % height;
                
                // Copier le pixel déformé
                if (srcX >= 0 && srcX < source.getWidth() && 
                    srcY >= 0 && srcY < source.getHeight()) {
                    int rgb = source.getRGB(srcX, srcY);
                    distorted.setRGB(x, y, rgb);
                }
            }
        }
        
        return distorted;
    }
    
    /**
     * Version optimisée avec sous-échantillonnage (pour grandes images).
     * 
     * Applique la distorsion tous les N pixels pour réduire le coût CPU.
     * 
     * @param source Image source
     * @param sampleRate Taux de sous-échantillonnage (1 = aucun, 2 = 1/4, etc.)
     * @return Image distordue
     */
    public BufferedImage applyDistortionFast(BufferedImage source, int sampleRate) {
        if (!enabled || source == null) {
            return source;
        }
        
        if (sampleRate <= 1) {
            return applyDistortion(source);  // Pas d'optimisation
        }
        
        BufferedImage distorted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        animationPhase += waveSpeed;
        
        // Sous-échantillonnage
        for (int y = 0; y < height; y += sampleRate) {
            for (int x = 0; x < width; x += sampleRate) {
                double offsetX = waveShapeWX[x % waveShapeWX.length] * Math.sin(animationPhase + y * 0.1);
                double offsetY = waveShapeHY[y % waveShapeHY.length] * Math.cos(animationPhase + x * 0.1);
                
                int srcX = ((int) (x + offsetX) + width) % width;
                int srcY = ((int) (y + offsetY) + height) % height;
                
                if (srcX >= 0 && srcX < source.getWidth() && 
                    srcY >= 0 && srcY < source.getHeight()) {
                    int rgb = source.getRGB(srcX, srcY);
                    
                    // Remplir le bloc sampleRate × sampleRate
                    for (int dy = 0; dy < sampleRate && y + dy < height; dy++) {
                        for (int dx = 0; dx < sampleRate && x + dx < width; dx++) {
                            distorted.setRGB(x + dx, y + dy, rgb);
                        }
                    }
                }
            }
        }
        
        return distorted;
    }
    
    /**
     * Applique un effet de "ripple" (ondulation circulaire).
     * 
     * Simule l'effet d'une goutte d'eau qui tombe.
     * 
     * @param source Image source
     * @param centerX Centre X de l'ondulation
     * @param centerY Centre Y de l'ondulation
     * @param radius Rayon de l'effet
     * @param strength Force de l'ondulation
     * @return Image avec effet ripple
     */
    public BufferedImage applyRipple(BufferedImage source, int centerX, int centerY, 
                                     int radius, double strength) {
        if (!enabled || source == null) {
            return source;
        }
        
        BufferedImage rippled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Distance au centre
                int dx = x - centerX;
                int dy = y - centerY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < radius && dist > 0) {
                    // Calculer l'ondulation (sin décroissant avec distance)
                    double wave = Math.sin((dist / radius) * 4 * PI - animationPhase * 2);
                    double offset = wave * strength * (1.0 - dist / radius);
                    
                    // Appliquer déformation radiale
                    double angle = Math.atan2(dy, dx);
                    int srcX = (int) (x + Math.cos(angle) * offset);
                    int srcY = (int) (y + Math.sin(angle) * offset);
                    
                    // Copier pixel
                    if (srcX >= 0 && srcX < source.getWidth() && 
                        srcY >= 0 && srcY < source.getHeight()) {
                        rippled.setRGB(x, y, source.getRGB(srcX, srcY));
                    }
                } else {
                    // Pas d'effet ici
                    if (x < source.getWidth() && y < source.getHeight()) {
                        rippled.setRGB(x, y, source.getRGB(x, y));
                    }
                }
            }
        }
        
        return rippled;
    }
    
    // ===== GETTERS / SETTERS =====
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            System.out.println("💧 Distorsion d'eau : ACTIVÉE (comme dans distor.c)");
        } else {
            System.out.println("💧 Distorsion d'eau : DÉSACTIVÉE");
        }
    }
    
    public void resetAnimation() {
        animationPhase = 0.0;
    }
    
    public double getAnimationPhase() {
        return animationPhase;
    }
}

