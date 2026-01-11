package fr.uparis.informatique.cpoo5.liquidwar.util;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Pool d'objets réutilisables pour réduire les allocations et le GC.
 * 
 * En Java, créer des objets temporaires (comme des String pour les clés de HashMap)
 * génère beaucoup de travail pour le Garbage Collector, causant des pauses.
 * 
 * Ce pool permet de réutiliser des objets au lieu d'en créer de nouveaux,
 * réduisant ainsi la pression sur le GC et améliorant la fluidité.
 * 
 * PRINCIPE :
 * 1. Pré-allouer N objets au démarrage
 * 2. acquire() pour obtenir un objet du pool
 * 3. release() pour remettre l'objet dans le pool
 * 4. Réutilisation au lieu d'allocation/désallocation
 * 
 * RÉSULTAT : Moins de GC pauses = mouvement plus fluide
 */
public class ObjectPool<T> {
    
    private final ArrayList<T> available;  // Objets disponibles
    private final ArrayList<T> inUse;      // Objets en utilisation
    private final Supplier<T> factory;     // Factory pour créer nouveaux objets
    private final int maxSize;             // Taille maximum du pool
    
    // Statistiques
    private long allocCount = 0;   // Nombre d'allocations
    private long reuseCount = 0;   // Nombre de réutilisations
    private long releaseCount = 0; // Nombre de libérations
    
    /**
     * Crée un pool d'objets.
     * 
     * @param factory Factory pour créer de nouveaux objets
     * @param initialSize Taille initiale (objets pré-alloués)
     * @param maxSize Taille maximum
     */
    public ObjectPool(Supplier<T> factory, int initialSize, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.available = new ArrayList<>(initialSize);
        this.inUse = new ArrayList<>(initialSize);
        
        // Pré-allocation
        for (int i = 0; i < initialSize; i++) {
            available.add(factory.get());
        }
    }
    
    /**
     * Obtient un objet du pool (ou en crée un si nécessaire).
     * 
     * @return Objet réutilisable
     */
    public synchronized T acquire() {
        T obj;
        
        if (!available.isEmpty()) {
            // Réutiliser un objet existant
            obj = available.remove(available.size() - 1);
            reuseCount++;
        } else if (inUse.size() + available.size() < maxSize) {
            // Créer un nouvel objet
            obj = factory.get();
            allocCount++;
        } else {
            // Pool plein ! Créer quand même (pas optimal)
            obj = factory.get();
            allocCount++;
            System.err.println("⚠️ Pool plein ! Allocation supplémentaire.");
        }
        
        inUse.add(obj);
        return obj;
    }
    
    /**
     * Remet un objet dans le pool pour réutilisation.
     * 
     * @param obj Objet à libérer
     */
    public synchronized void release(T obj) {
        if (inUse.remove(obj)) {
            available.add(obj);
            releaseCount++;
        }
    }
    
    /**
     * Libère tous les objets en utilisation.
     */
    public synchronized void releaseAll() {
        available.addAll(inUse);
        inUse.clear();
    }
    
    /**
     * Obtient le taux de réutilisation (0-1).
     */
    public double getReuseRate() {
        long total = allocCount + reuseCount;
        if (total == 0) return 0;
        return (double) reuseCount / total;
    }
    
    /**
     * Affiche les statistiques du pool.
     */
    public void printStats() {
        System.out.printf("📦 ObjectPool Stats | " +
                        "Allocs: %d | Reuses: %d (%.1f%%) | " +
                        "Available: %d | InUse: %d%n",
                        allocCount, reuseCount, getReuseRate() * 100,
                        available.size(), inUse.size());
    }
    
    /**
     * Réinitialise les statistiques.
     */
    public synchronized void resetStats() {
        allocCount = 0;
        reuseCount = 0;
        releaseCount = 0;
    }
    
    // ===== GETTERS =====
    
    public int getAvailableCount() {
        return available.size();
    }
    
    public int getInUseCount() {
        return inUse.size();
    }
    
    public int getTotalSize() {
        return available.size() + inUse.size();
    }
    
    /**
     * Pool spécialisé pour les clés de position (évite String.format).
     * 
     * Au lieu de créer des String à chaque frame avec "x,y",
     * on réutilise des objets Point mutables.
     */
    public static class Point {
        public int x;
        public int y;
        
        public Point() {
            this(0, 0);
        }
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point p = (Point) obj;
            return x == p.x && y == p.y;
        }
        
        @Override
        public int hashCode() {
            return x * 31 + y;
        }
        
        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }
}

