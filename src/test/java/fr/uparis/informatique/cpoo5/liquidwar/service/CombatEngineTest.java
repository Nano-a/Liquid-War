package fr.uparis.informatique.cpoo5.liquidwar.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;
import fr.uparis.informatique.cpoo5.liquidwar.model.entities.Fighter;

/**
 * Tests unitaires pour CombatEngine.
 * 
 * Teste :
 * - Détection des combats
 * - Application des dégâts
 * - Conversion de camp
 * - Conservation du nombre total de fighters
 */
class CombatEngineTest {

    private ArrayList<Fighter> fighters;
    private int[] teamFighterCount;

    @BeforeEach
    void setUp() {
        fighters = new ArrayList<>();
        teamFighterCount = new int[] { 0, 0 };
    }

    @Test
    @DisplayName("Détection d'un combat entre deux équipes")
    void testCombatDetection() {
        // Créer deux fighters de camps différents à la même position
        Fighter f1 = new Fighter(10, 10, 0);
        Fighter f2 = new Fighter(10, 10, 1);
        fighters.add(f1);
        fighters.add(f2);
        teamFighterCount[0] = 1;
        teamFighterCount[1] = 1;

        int initialHealth1 = f1.health;
        int initialHealth2 = f2.health;

        List<Fighter> removed = CombatEngine.checkCombat(fighters, teamFighterCount);

        // Les deux fighters doivent avoir perdu de la santé
        assertTrue(f1.health < initialHealth1 || f2.health < initialHealth2,
                "Au moins un fighter doit avoir perdu de la santé");

        // Aucun fighter ne doit être supprimé (changement de camp seulement)
        assertEquals(0, removed.size(), "Aucun fighter ne doit être supprimé");
        assertEquals(2, fighters.size(), "Le nombre total de fighters doit rester constant");
    }

    @Test
    @DisplayName("Conversion de camp quand santé < 0")
    void testTeamConversion() {
        // Créer un fighter avec peu de santé
        Fighter weakFighter = new Fighter(10, 10, 0);
        weakFighter.health = 5; // Très peu de santé

        Fighter strongFighter = new Fighter(10, 10, 1);
        strongFighter.health = GameConfig.FIGHTER_INITIAL_HEALTH;

        fighters.add(weakFighter);
        fighters.add(strongFighter);
        teamFighterCount[0] = 1;
        teamFighterCount[1] = 1;

        int initialTeam0 = teamFighterCount[0];
        int initialTeam1 = teamFighterCount[1];

        CombatEngine.checkCombat(fighters, teamFighterCount);

        // Si le fighter faible a perdu toute sa santé, il doit changer de camp
        if (weakFighter.health < 0) {
            assertEquals(1, weakFighter.team, "Le fighter faible doit changer de camp");
            assertTrue(weakFighter.health >= 0, "La santé doit être restaurée après conversion");
        }

        // Le nombre total doit rester constant
        assertEquals(initialTeam0 + initialTeam1,
                teamFighterCount[0] + teamFighterCount[1],
                "Le nombre total de fighters doit rester constant");
    }

    @Test
    @DisplayName("Pas de combat si même équipe")
    void testNoCombatSameTeam() {
        Fighter f1 = new Fighter(10, 10, 0);
        Fighter f2 = new Fighter(10, 10, 0); // Même équipe
        fighters.add(f1);
        fighters.add(f2);
        teamFighterCount[0] = 2;

        int initialHealth1 = f1.health;
        int initialHealth2 = f2.health;

        CombatEngine.checkCombat(fighters, teamFighterCount);

        // Pas de dégâts si même équipe
        assertEquals(initialHealth1, f1.health, "Pas de dégâts si même équipe");
        assertEquals(initialHealth2, f2.health, "Pas de dégâts si même équipe");
    }

    @Test
    @DisplayName("Pas de combat si positions différentes")
    void testNoCombatDifferentPositions() {
        Fighter f1 = new Fighter(10, 10, 0);
        Fighter f2 = new Fighter(20, 20, 1); // Positions différentes
        fighters.add(f1);
        fighters.add(f2);
        teamFighterCount[0] = 1;
        teamFighterCount[1] = 1;

        int initialHealth1 = f1.health;
        int initialHealth2 = f2.health;

        CombatEngine.checkCombat(fighters, teamFighterCount);

        // Pas de dégâts si positions différentes
        assertEquals(initialHealth1, f1.health, "Pas de dégâts si positions différentes");
        assertEquals(initialHealth2, f2.health, "Pas de dégâts si positions différentes");
    }

    @Test
    @DisplayName("Conservation du nombre total de fighters")
    void testTotalFightersConserved() {
        // Créer plusieurs combats
        for (int i = 0; i < 10; i++) {
            fighters.add(new Fighter(i, i, 0));
            fighters.add(new Fighter(i, i, 1)); // Même position, équipe différente
        }
        teamFighterCount[0] = 10;
        teamFighterCount[1] = 10;

        int initialTotal = fighters.size();

        CombatEngine.checkCombat(fighters, teamFighterCount);

        // Le nombre total doit rester constant
        assertEquals(initialTotal, fighters.size(),
                "Le nombre total de fighters doit rester constant");
        assertEquals(initialTotal, teamFighterCount[0] + teamFighterCount[1],
                "Le nombre total par équipe doit rester constant");
    }

    @Test
    @DisplayName("Restauration de la santé après conversion")
    void testHealthRestoration() {
        Fighter weakFighter = new Fighter(10, 10, 0);
        weakFighter.health = 1; // Très peu de santé

        Fighter strongFighter = new Fighter(10, 10, 1);
        strongFighter.health = GameConfig.FIGHTER_INITIAL_HEALTH;

        fighters.add(weakFighter);
        fighters.add(strongFighter);
        teamFighterCount[0] = 1;
        teamFighterCount[1] = 1;

        CombatEngine.checkCombat(fighters, teamFighterCount);

        // Si conversion, la santé doit être restaurée
        if (weakFighter.team == 1) {
            assertTrue(weakFighter.health >= 0 &&
                    weakFighter.health <= GameConfig.FIGHTER_INITIAL_HEALTH,
                    "La santé doit être restaurée dans une plage valide");
        }
    }

    @Test
    @DisplayName("Combat mutuel - les deux fighters s'attaquent")
    void testMutualCombat() {
        Fighter f1 = new Fighter(10, 10, 0);
        Fighter f2 = new Fighter(10, 10, 1);
        fighters.add(f1);
        fighters.add(f2);
        teamFighterCount[0] = 1;
        teamFighterCount[1] = 1;

        int initialHealth1 = f1.health;
        int initialHealth2 = f2.health;

        CombatEngine.checkCombat(fighters, teamFighterCount);

        // Les deux doivent avoir perdu de la santé
        assertTrue(f1.health < initialHealth1, "Fighter 1 doit avoir perdu de la santé");
        assertTrue(f2.health < initialHealth2, "Fighter 2 doit avoir perdu de la santé");
    }

    @Test
    @DisplayName("Liste vide retournée (pas de suppression)")
    void testEmptyListReturned() {
        Fighter f1 = new Fighter(10, 10, 0);
        Fighter f2 = new Fighter(10, 10, 1);
        fighters.add(f1);
        fighters.add(f2);
        teamFighterCount[0] = 1;
        teamFighterCount[1] = 1;

        List<Fighter> removed = CombatEngine.checkCombat(fighters, teamFighterCount);

        // Doit retourner une liste vide
        assertNotNull(removed, "La liste retournée ne doit pas être null");
        assertEquals(0, removed.size(), "La liste retournée doit être vide");
    }
}
