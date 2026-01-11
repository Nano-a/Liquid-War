package fr.uparis.informatique.cpoo5.liquidwar.util;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Mesh;

/**
 * Constructeur de graphe MESH pour Liquid War.
 * 
 * Cette classe construit le graphe de cellules MESH à partir d'une carte,
 * en créant les liens directs entre voisins (comme dans le code C).
 * 
 * IMPORTANT : Cette approche remplace les tableaux 2D par une structure
 * de graphe avec pointeurs directs, comme dans mesh.c du code C.
 */
public class MeshBuilder {
    
    // Directions : NNE, NE, ENE, ESE, SE, SSE, SSW, SW, WSW, WNW, NW, NNW
    private static final int DIR_NNE = 0;
    private static final int DIR_NE = 1;
    private static final int DIR_ENE = 2;
    private static final int DIR_ESE = 3;
    private static final int DIR_SE = 4;
    private static final int DIR_SSE = 5;
    private static final int DIR_SSW = 6;
    private static final int DIR_SW = 7;
    private static final int DIR_WSW = 8;
    private static final int DIR_WNW = 9;
    private static final int DIR_NW = 10;
    private static final int DIR_NNW = 11;
    
    // Offsets de déplacement pour chaque direction
    private static final int[][] DIR_OFFSETS = {
        {0, -1},   // NNE
        {1, -1},   // NE
        {1, 0},    // ENE
        {1, 0},    // ESE
        {1, 1},    // SE
        {0, 1},    // SSE
        {0, 1},    // SSW
        {-1, 1},   // SW
        {-1, 0},   // WSW
        {-1, 0},   // WNW
        {-1, -1},  // NW
        {0, -1}    // NNW
    };
    
    /**
     * Construit le graphe MESH à partir d'une carte.
     * 
     * @param map Carte (map[y][x], -1 = mur, autres = terrain)
     * @return Tableau continu de MESH (accès séquentiel rapide)
     */
    public static Mesh[] buildMeshArray(int[][] map) {
        if (map == null || map.length == 0 || map[0] == null) {
            return new Mesh[0];
        }
        
        int height = map.length;
        int width = map[0].length;
        
        // Étape 1 : Créer toutes les cellules MESH (sauf murs)
        Mesh[][] meshGrid = new Mesh[height][width];
        int meshCount = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (map[y][x] != -1) {  // Pas un mur
                    meshGrid[y][x] = new Mesh(x, y);
                    meshCount++;
                }
            }
        }
        
        // Étape 2 : Créer les liens entre voisins (comme dans mesh.c)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Mesh mesh = meshGrid[y][x];
                if (mesh == null) continue;  // Mur
                
                // Pour chaque direction, lier au voisin
                for (int dir = 0; dir < 12; dir++) {
                    int nx = x + getDirectionOffsetX(dir);
                    int ny = y + getDirectionOffsetY(dir);
                    
                    // Vérifier les bornes
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        Mesh neighbor = meshGrid[ny][nx];
                        if (neighbor != null) {  // Voisin existe (pas mur)
                            mesh.setNeighbor(dir, neighbor);
                        }
                    }
                }
            }
        }
        
        // Étape 3 : Créer tableau continu pour parcours séquentiel rapide
        // (comme CURRENT_MESH dans le C)
        Mesh[] meshArray = new Mesh[meshCount];
        int index = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (meshGrid[y][x] != null) {
                    Mesh mesh = meshGrid[y][x];
                    mesh.arrayIndex = index;  // Sauvegarder l'index dans le Mesh
                    meshArray[index++] = mesh;
                }
            }
        }
        
        System.out.println("✅ MESH array créé : " + meshCount + " cellules avec liens directs");
        System.out.println("   Gain de performance : accès O(1) vs calcul d'index");
        
        return meshArray;
    }
    
    /**
     * Offset X pour une direction donnée.
     */
    private static int getDirectionOffsetX(int dir) {
        switch (dir) {
            case DIR_NNE: return 0;
            case DIR_NE: return 1;
            case DIR_ENE: return 1;
            case DIR_ESE: return 1;
            case DIR_SE: return 1;
            case DIR_SSE: return 0;
            case DIR_SSW: return 0;
            case DIR_SW: return -1;
            case DIR_WSW: return -1;
            case DIR_WNW: return -1;
            case DIR_NW: return -1;
            case DIR_NNW: return 0;
            default: return 0;
        }
    }
    
    /**
     * Offset Y pour une direction donnée.
     */
    private static int getDirectionOffsetY(int dir) {
        switch (dir) {
            case DIR_NNE: return -1;
            case DIR_NE: return -1;
            case DIR_ENE: return 0;
            case DIR_ESE: return 0;
            case DIR_SE: return 1;
            case DIR_SSE: return 1;
            case DIR_SSW: return 1;
            case DIR_SW: return 1;
            case DIR_WSW: return 0;
            case DIR_WNW: return 0;
            case DIR_NW: return -1;
            case DIR_NNW: return -1;
            default: return 0;
        }
    }
    
    /**
     * Trouve le MESH à une position donnée (pour conversion Fighter → Mesh).
     * 
     * @param meshArray Tableau de MESH
     * @param x Position X
     * @param y Position Y
     * @return Le MESH à cette position ou null
     */
    public static Mesh getMeshAt(Mesh[] meshArray, int x, int y) {
        // Parcours linéaire (pas optimal mais simple)
        // Pour optimiser : utiliser une HashMap<Point, Mesh>
        for (Mesh mesh : meshArray) {
            if (mesh.x == x && mesh.y == y) {
                return mesh;
            }
        }
        return null;
    }
}

