package fr.uparis.informatique.cpoo5.liquidwar.service;

import fr.uparis.informatique.cpoo5.liquidwar.model.GameState;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.List;
import java.util.ArrayList;

/**
 * Classe démontrant l'utilisation de :
 * - CompletableFuture (Java 8+)
 * - ForkJoinPool (Java 7+)
 * - Programmation asynchrone
 * 
 * @author Abderrahman AJINOU &amp; Ahmed CHABIRA-MOUNCEF
 * @since 2026-01-11
 */
public class AsyncGameLoader {
    
    private static final ExecutorService executor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    /**
     * Charge une carte de manière asynchrone avec CompletableFuture.
     * 
     * <p><b>CompletableFuture (Java 8+)</b> : Programmation asynchrone non bloquante.
     * 
     * @param mapName Nom de la carte
     * @return CompletableFuture contenant la carte chargée
     */
    public static CompletableFuture<int[][]> loadMapAsync(String mapName) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulation de chargement
            System.out.println("🔄 Chargement de la carte " + mapName + "...");
            try {
                Thread.sleep(1000); // Simulation I/O
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Retourne une carte simple pour démonstration
            int[][] map = new int[GameConfig.MAP_WIDTH][GameConfig.MAP_HEIGHT];
            System.out.println("✅ Carte " + mapName + " chargée");
            return map;
        }, executor);
    }
    
    /**
     * Charge plusieurs ressources en parallèle avec CompletableFuture.
     * 
     * @param mapName Nom de la carte
     * @return CompletableFuture de Boolean indiquant le succès
     */
    public static CompletableFuture<Boolean> loadGameAsync(String mapName) {
        // Composition de CompletableFutures
        CompletableFuture<int[][]> mapFuture = loadMapAsync(mapName);
        CompletableFuture<Boolean> audioFuture = loadAudioAsync();
        CompletableFuture<Boolean> texturesFuture = loadTexturesAsync();
        
        // Combine tous les résultats
        return CompletableFuture.allOf(mapFuture, audioFuture, texturesFuture)
            .thenApply(v -> {
                System.out.println("✅ Toutes les ressources chargées");
                return true;
            })
            .exceptionally(ex -> {
                System.err.println("❌ Erreur de chargement : " + ex.getMessage());
                return false;
            });
    }
    
    /**
     * Charge l'audio de manière asynchrone.
     * 
     * @return CompletableFuture<Boolean>
     */
    private static CompletableFuture<Boolean> loadAudioAsync() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("🔄 Chargement audio...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("✅ Audio chargé");
            return true;
        }, executor);
    }
    
    /**
     * Charge les textures de manière asynchrone.
     * 
     * @return CompletableFuture<Boolean>
     */
    private static CompletableFuture<Boolean> loadTexturesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("🔄 Chargement textures...");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("✅ Textures chargées");
            return true;
        }, executor);
    }
    
    /**
     * Tâche récursive pour ForkJoinPool.
     * 
     * <p><b>ForkJoinPool (Java 7+)</b> : Pool de threads avec work-stealing
     * pour algorithmes récursifs de type divide-and-conquer.
     */
    static class GradientCalculationTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 1000;
        private final int[] data;
        private final int start;
        private final int end;
        
        public GradientCalculationTask(int[] data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Integer compute() {
            int length = end - start;
            
            // Si la tâche est petite, calcul direct
            if (length <= THRESHOLD) {
                return computeDirectly();
            }
            
            // Sinon, diviser en sous-tâches (Fork)
            int mid = start + length / 2;
            GradientCalculationTask leftTask = 
                new GradientCalculationTask(data, start, mid);
            GradientCalculationTask rightTask = 
                new GradientCalculationTask(data, mid, end);
            
            // Fork : exécute en parallèle
            leftTask.fork();
            
            // Calcule la partie droite dans le thread courant
            int rightResult = rightTask.compute();
            
            // Join : attend le résultat de la tâche forkée
            int leftResult = leftTask.join();
            
            // Combine les résultats
            return leftResult + rightResult;
        }
        
        private Integer computeDirectly() {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += data[i];
            }
            return sum;
        }
    }
    
    /**
     * Calcule un gradient avec ForkJoinPool.
     * 
     * @param data Données à traiter
     * @return Somme calculée en parallèle
     */
    public static int calculateGradientWithForkJoin(int[] data) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        GradientCalculationTask task = new GradientCalculationTask(data, 0, data.length);
        return pool.invoke(task);
    }
    
    /**
     * Ferme l'executor.
     */
    public static void shutdown() {
        executor.shutdown();
    }
}
