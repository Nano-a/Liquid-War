package fr.uparis.informatique.cpoo5.liquidwar.model.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Cursor;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;

/**
 * Tests unitaires pour EntityFactory.
 * 
 * Teste :
 * - Création de fighters
 * - Création de curseurs
 * - Création d'armées
 * - Clonage de fighters
 */
class EntityFactoryTest {

    private int[][] map;
    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 50;

    @BeforeEach
    void setUp() {
        map = new int[MAP_HEIGHT][MAP_WIDTH];
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = 0;
            }
        }
    }

    @Test
    @DisplayName("createFighter crée un fighter valide")
    void testCreateFighter() {
        Fighter fighter = EntityFactory.createFighter(10, 20, 0);

        assertNotNull(fighter, "Le fighter ne doit pas être null");
        assertEquals(10, fighter.x, "X doit être correct");
        assertEquals(20, fighter.y, "Y doit être correct");
        assertEquals(0, fighter.team, "L'équipe doit être correcte");
        assertEquals(GameConfig.FIGHTER_INITIAL_HEALTH, fighter.health,
                "La santé initiale doit être correcte");
    }

    @Test
    @DisplayName("createCursor crée un curseur valide")
    void testCreateCursor() {
        Cursor cursor = EntityFactory.createCursor(15, 25, 1);

        assertNotNull(cursor, "Le curseur ne doit pas être null");
        assertEquals(15, cursor.x, "X doit être correct");
        assertEquals(25, cursor.y, "Y doit être correct");
        assertEquals(1, cursor.team, "L'équipe doit être correcte");
        assertEquals(1, cursor.active, "Le curseur doit être actif");
    }

    @Test
    @DisplayName("createArmy crée des fighters")
    void testCreateArmy() {
        Point center = new Point(25, 25);
        int count = 50;
        int team = 0;

        List<Fighter> army = EntityFactory.createArmy(team, center, count, map);

        assertNotNull(army, "L'armée ne doit pas être null");
        assertTrue(army.size() > 0, "Des fighters doivent être créés");

        // Vérifier que tous les fighters sont de la bonne équipe
        for (Fighter f : army) {
            assertEquals(team, f.team, "Tous les fighters doivent être de la bonne équipe");
            assertTrue(f.x >= 0 && f.x < MAP_WIDTH, "X doit être dans les limites");
            assertTrue(f.y >= 0 && f.y < MAP_HEIGHT, "Y doit être dans les limites");
        }
    }

    @Test
    @DisplayName("createArmy respecte les obstacles")
    void testCreateArmyRespectsObstacles() {
        // Créer un obstacle
        map[25][25] = -1;

        Point center = new Point(25, 25);
        int count = 50;
        int team = 0;

        List<Fighter> army = EntityFactory.createArmy(team, center, count, map);

        // Aucun fighter ne doit être sur l'obstacle
        for (Fighter f : army) {
            assertNotEquals(-1, map[f.y][f.x], "Aucun fighter ne doit être sur un obstacle");
        }
    }

    @Test
    @DisplayName("createEliteFighter crée un fighter avec double santé")
    void testCreateEliteFighter() {
        Fighter elite = EntityFactory.createEliteFighter(10, 20, 0);

        assertNotNull(elite, "Le fighter élite ne doit pas être null");
        assertEquals(GameConfig.FIGHTER_INITIAL_HEALTH * 2, elite.health,
                "La santé doit être doublée");
    }

    @Test
    @DisplayName("cloneFighter crée une copie")
    void testCloneFighter() {
        Fighter original = new Fighter(10, 20, 0);
        original.health = 50;

        Fighter clone = EntityFactory.cloneFighter(original);

        assertNotNull(clone, "Le clone ne doit pas être null");
        assertEquals(original.x, clone.x, "X doit être identique");
        assertEquals(original.y, clone.y, "Y doit être identique");
        assertEquals(original.team, clone.team, "L'équipe doit être identique");
        assertEquals(original.health, clone.health, "La santé doit être identique");

        // Modifier le clone ne doit pas affecter l'original
        clone.x = 100;
        assertNotEquals(original.x, clone.x, "Le clone doit être indépendant");
    }

    @Test
    @DisplayName("createInitialFighters crée des fighters pour toutes les équipes")
    void testCreateInitialFighters() {
        Cursor[] cursors = new Cursor[2];
        for (int team = 0; team < 2; team++) {
            cursors[team] = EntityFactory.createCursor(20 + team * 10, 20, team);
        }

        List<Fighter> allFighters = EntityFactory.createInitialFighters(cursors, 2, map);

        assertNotNull(allFighters, "La liste ne doit pas être null");
        assertTrue(allFighters.size() > 0, "Des fighters doivent être créés");

        // Vérifier qu'il y a des fighters des deux équipes
        boolean hasTeam0 = false;
        boolean hasTeam1 = false;
        for (Fighter f : allFighters) {
            if (f.team == 0)
                hasTeam0 = true;
            if (f.team == 1)
                hasTeam1 = true;
        }

        assertTrue(hasTeam0, "Il doit y avoir des fighters de l'équipe 0");
        assertTrue(hasTeam1, "Il doit y avoir des fighters de l'équipe 1");
    }
}
