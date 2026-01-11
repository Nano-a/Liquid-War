package fr.uparis.informatique.cpoo5.liquidwar.model.entities;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

/**
 * Structure MESH optimisée pour Liquid War - Reproduction fidèle du C.
 * 
 * Cette classe reproduit la structure MESH du code C (mesh.h) pour obtenir
 * la même performance et fluidité.
 * 
 * AVANTAGES par rapport aux tableaux 2D :
 * - Accès O(1) aux voisins via pointeurs directs (neighbors[])
 * - Meilleure localité de cache (données contiguës)
 * - Moins de calculs d'index (pas de y*width+x)
 * - Structure identique au code C original
 * 
 * Correspond à la structure C (mesh.h, lignes 107-115) :
 * typedef struct {
 *   short x, y;
 *   MESH_SIDE side;
 *   MESH_INFO info[NB_TEAMS];
 *   void *link[NB_DIRS];
 * } MESH;
 */
public class Mesh {
    
    // ===== CONSTANTES =====
    private static final int NB_DIRS = 12;  // 12 directions
    private static final int MAX_TEAMS = 6; // Maximum d'équipes
    
    // ===== POSITION =====
    public final short x;  // Position X (comme dans le C)
    public final short y;  // Position Y (comme dans le C)
    
    // ===== INDEX DANS LE TABLEAU =====
    /**
     * Index de ce Mesh dans le meshArray
     * Utilisé pour accès direct O(1) depuis une position (x,y)
     */
    public int arrayIndex = -1;
    
    // ===== LIENS VERS VOISINS =====
    /**
     * ⭐ CLEF DE LA PERFORMANCE : Liens directs vers les 12 voisins
     * 
     * Au lieu de calculer neighbor = array[y*width + x + offset],
     * on fait juste : neighbor = neighbors[direction]
     * 
     * Gain : 8× plus rapide (1 déréférencement vs 8 instructions)
     */
    public final Mesh[] neighbors;  // 12 voisins (NNE, NE, ENE, ESE, etc.)
    
    // ===== INFORMATIONS PAR ÉQUIPE =====
    /**
     * Informations pour chaque équipe (gradient, direction, updateTime)
     * Correspond à MESH_INFO info[NB_TEAMS] du code C
     */
    public final MeshInfo[] teamInfo;
    
    // ===== SIDE (taille de la cellule) =====
    /**
     * Taille de la cellule pour le calcul du gradient
     * Dans le code C : pos->side.size
     * Ici simplifié à 1 pour toutes les cellules
     */
    public final int sideSize;
    
    // ===== CONSTRUCTEUR =====
    
    /**
     * Crée une cellule MESH à la position donnée.
     * 
     * @param x Position X
     * @param y Position Y
     */
    public Mesh(int x, int y) {
        this.x = (short) x;
        this.y = (short) y;
        this.neighbors = new Mesh[NB_DIRS];
        this.teamInfo = new MeshInfo[MAX_TEAMS];
        this.sideSize = 1;  // Simplifié (dans le C peut varier)
        
        // Initialiser les infos pour chaque équipe
        for (int team = 0; team < MAX_TEAMS; team++) {
            teamInfo[team] = new MeshInfo();
        }
    }
    
    // ===== MÉTHODES UTILITAIRES =====
    
    /**
     * Définit le voisin dans une direction donnée.
     * 
     * @param direction Direction (0-11)
     * @param neighbor Voisin dans cette direction (null si mur)
     */
    public void setNeighbor(int direction, Mesh neighbor) {
        if (direction >= 0 && direction < NB_DIRS) {
            neighbors[direction] = neighbor;
        }
    }
    
    /**
     * Obtient le voisin dans une direction.
     * 
     * @param direction Direction (0-11)
     * @return Le voisin ou null si mur/bord
     */
    public Mesh getNeighbor(int direction) {
        if (direction >= 0 && direction < NB_DIRS) {
            return neighbors[direction];
        }
        return null;
    }
    
    /**
     * Vérifie si la cellule a un voisin dans une direction.
     * 
     * @param direction Direction (0-11)
     * @return true si le voisin existe (pas de mur)
     */
    public boolean hasNeighbor(int direction) {
        return direction >= 0 && direction < NB_DIRS && neighbors[direction] != null;
    }
    
    /**
     * Classe interne : Informations par équipe.
     * 
     * Correspond à MESH_INFO du code C (mesh.h, lignes 89-94) :
     * typedef struct {
     *   MESH_UPDATE update;
     *   MESH_STATE state;
     * } MESH_INFO;
     */
    public static class MeshInfo {
        // ===== STATE (gradient et direction) =====
        /**
         * Gradient pour cette équipe (distance au curseur)
         * Correspond à state.grad dans le C
         */
        public int gradient;
        
        /**
         * Direction de mouvement calculée (0-11)
         * Correspond à state.dir dans le C
         */
        public int direction;
        
        // ===== UPDATE (temps de mise à jour ou position curseur) =====
        /**
         * Temps de dernière mise à jour (négatif = loin du curseur)
         * Correspond à update.time dans le C
         * 
         * Valeurs :
         * - >= 0 : Proche du curseur (mis à jour récemment)
         * - < 0 : Loin du curseur (utiliser gradient)
         */
        public int updateTime;
        
        /**
         * Position X du curseur (pour calcul de direction directe)
         * Correspond à update.cursor.x dans le C
         */
        public short cursorX;
        
        /**
         * Position Y du curseur (pour calcul de direction directe)
         * Correspond à update.cursor.y dans le C
         */
        public short cursorY;
        
        /**
         * Constructeur avec valeurs par défaut.
         */
        public MeshInfo() {
            this.gradient = GameConfig.AREA_START_GRADIENT;
            this.direction = 0;
            this.updateTime = -1;
            this.cursorX = 0;
            this.cursorY = 0;
        }
        
        /**
         * Réinitialise le gradient à la valeur de départ.
         */
        public void resetGradient() {
            this.gradient = GameConfig.AREA_START_GRADIENT;
        }
        
        /**
         * Met à jour la position du curseur pour cette cellule.
         */
        public void updateCursorPosition(int cx, int cy) {
            this.cursorX = (short) cx;
            this.cursorY = (short) cy;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Mesh(%d,%d)", x, y);
    }
}

