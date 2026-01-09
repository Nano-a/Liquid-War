package fr.uparis.informatique.cpoo5.liquidwar.model.factory;

import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory pour créer les entités du jeu.
 * 
 * <p><b>Pattern Factory</b> : Centralise la création d'objets complexes.
 * Permet de modifier la logique de création sans toucher au code client.
 * 
 * <h2>Avantages</h2>
 * <ul>
 *   <li>✅ Centralisation de la logique de création</li>
 *   <li>✅ Facile de changer la façon dont les objets sont créés</li>
 *   <li>✅ Testable (peut créer des mocks facilement)</li>
 *   <li>✅ Respecte le principe Open/Closed</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // Au lieu de :
 * Fighter f = new Fighter(x, y, team);
 * 
 * // Utiliser :
 * Fighter f = EntityFactory.createFighter(x, y, team);
 * 
 * // Ou pour créer une armée complète :
 * List<Fighter> army = EntityFactory.createArmy(0, center, 2000);
 * }</pre>
 * 
 * @author Votre Nom
 * @version 1.0
 * @since 2025-12-01
 * @see Fighter
 * @see Cursor
 */
public class EntityFactory {
    
    // Empêcher l'instanciation (factory statique)
    private EntityFactory() {}
    
    /**
     * Crée un fighter à une position donnée.
     * 
     * @param x Position X
     * @param y Position Y
     * @param team Numéro de l'équipe (0 ou 1)
     * @return Nouveau fighter créé
     */
    public static Fighter createFighter(int x, int y, int team) {
        return new Fighter(x, y, team);
    }
    
    /**
     * Crée un curseur pour une équipe.
     * 
     * @param x Position initiale X
     * @param y Position initiale Y
     * @param team Numéro de l'équipe
     * @return Nouveau curseur créé
     */
    public static Cursor createCursor(int x, int y, int team) {
        Cursor cursor = new Cursor();
        cursor.x = x;
        cursor.y = y;
        cursor.team = team;
        cursor.active = 1;
        return cursor;
    }
    
    /**
     * Crée une armée complète de fighters autour d'un point central.
     * 
     * <p>Les fighters sont placés en cercle autour du centre, avec un rayon défini
     * par {@link GameConfig#FIGHTER_SPAWN_RADIUS}.
     * 
     * <p>Si une position est invalide (mur ou hors limites), elle est ignorée.
     * La méthode garantit de créer exactement {@code count} fighters ou échoue
     * après un nombre maximal de tentatives.
     * 
     * @param team Numéro de l'équipe
     * @param center Point central autour duquel spawn l'armée
     * @param count Nombre de fighters à créer
     * @param map Carte du jeu (pour vérifier les murs)
     * @return Liste des fighters créés (peut être < count si pas assez d'espace)
     */
    public static List<Fighter> createArmy(int team, Point center, int count, int[][] map) {
        List<Fighter> army = new ArrayList<>();
        int radius = GameConfig.FIGHTER_SPAWN_RADIUS;
        
        int spawned = 0;
        int attempts = 0;
        int maxAttempts = count * 10; // Limite pour éviter boucle infinie
        
        while (spawned < count && attempts < maxAttempts) {
            // Générer position aléatoire en cercle
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;
            
            int x = center.x + (int) (Math.cos(angle) * distance);
            int y = center.y + (int) (Math.sin(angle) * distance);
            
            // Vérifier validité
            if (isValidSpawnPosition(x, y, map, army)) {
                army.add(createFighter(x, y, team));
                spawned++;
            }
            
            attempts++;
        }
        
        if (spawned < count) {
            System.err.println("⚠️ Avertissement : Seulement " + spawned + "/" + count + 
                             " fighters créés pour l'équipe " + team);
        }
        
        return army;
    }
    
    /**
     * Vérifie si une position est valide pour spawn un fighter.
     * 
     * @param x Position X
     * @param y Position Y
     * @param map Carte du jeu
     * @param existingFighters Fighters déjà créés (pour éviter superposition)
     * @return true si la position est valide
     */
    private static boolean isValidSpawnPosition(int x, int y, int[][] map, 
                                                 List<Fighter> existingFighters) {
        // Vérifier limites
        if (x < 0 || x >= GameConfig.MAP_WIDTH || 
            y < 0 || y >= GameConfig.MAP_HEIGHT) {
            return false;
        }
        
        // Vérifier mur
        if (map[y][x] == -1) {
            return false;
        }
        
        // Vérifier superposition avec fighters existants
        for (Fighter f : existingFighters) {
            if (f.x == x && f.y == y) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Crée tous les fighters initiaux pour une partie.
     * 
     * <p>Crée {@link GameConfig#INITIAL_FIGHTERS_PER_TEAM} fighters par équipe,
     * placés autour de leurs curseurs respectifs.
     * 
     * @param cursors Curseurs des équipes (définissent les positions de spawn)
     * @param activeTeams Nombre d'équipes actives
     * @param map Carte du jeu
     * @return Liste de tous les fighters créés
     */
    public static List<Fighter> createInitialFighters(Cursor[] cursors, 
                                                       int activeTeams, 
                                                       int[][] map) {
        List<Fighter> allFighters = new ArrayList<>();
        
        for (int team = 0; team < activeTeams; team++) {
            if (cursors[team] != null && cursors[team].active != 0) {
                Point center = new Point(cursors[team].x, cursors[team].y);
                List<Fighter> teamFighters = createArmy(team, center, 
                                                        GameConfig.INITIAL_FIGHTERS_PER_TEAM, 
                                                        map);
                allFighters.addAll(teamFighters);
                
                System.out.println("✅ Équipe " + team + " : " + teamFighters.size() + 
                                 " fighters créés");
            }
        }
        
        System.out.println("✅ Total : " + allFighters.size() + " fighters créés");
        return allFighters;
    }
    
    /**
     * Crée un fighter "élite" avec des stats améliorées (exemple d'extension).
     * 
     * <p>Démontre comment le pattern Factory facilite l'ajout de nouveaux types.
     * 
     * @param x Position X
     * @param y Position Y
     * @param team Équipe
     * @return Fighter élite avec health × 2
     */
    public static Fighter createEliteFighter(int x, int y, int team) {
        Fighter elite = new Fighter(x, y, team);
        elite.health = GameConfig.FIGHTER_INITIAL_HEALTH * 2; // Double health
        return elite;
    }
    
    /**
     * Clone un fighter (utile pour save/load).
     * 
     * @param original Fighter à cloner
     * @return Copie profonde du fighter
     */
    public static Fighter cloneFighter(Fighter original) {
        Fighter clone = new Fighter(original.x, original.y, original.team);
        clone.health = original.health;
        return clone;
    }
}

