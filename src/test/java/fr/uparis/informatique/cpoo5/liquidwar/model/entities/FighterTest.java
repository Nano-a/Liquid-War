package fr.uparis.informatique.cpoo5.liquidwar.model.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

/**
 * Tests unitaires pour Fighter.
 * 
 * Teste :
 * - Création de fighter
 * - Initialisation des valeurs
 * - Santé initiale
 */
class FighterTest {

    @Test
    @DisplayName("Création de fighter avec position et équipe")
    void testFighterCreation() {
        Fighter fighter = new Fighter(10, 20, 0);

        assertEquals(10, fighter.x, "X doit être correct");
        assertEquals(20, fighter.y, "Y doit être correct");
        assertEquals(0, fighter.team, "L'équipe doit être correcte");
        assertEquals(GameConfig.FIGHTER_INITIAL_HEALTH, fighter.health,
                "La santé initiale doit être correcte");
    }

    @Test
    @DisplayName("Fighter peut être créé pour différentes équipes")
    void testFighterDifferentTeams() {
        Fighter f0 = new Fighter(0, 0, 0);
        Fighter f1 = new Fighter(0, 0, 1);

        assertEquals(0, f0.team, "Fighter 0 doit être de l'équipe 0");
        assertEquals(1, f1.team, "Fighter 1 doit être de l'équipe 1");
    }

    @Test
    @DisplayName("Fighter peut être modifié après création")
    void testFighterModification() {
        Fighter fighter = new Fighter(10, 20, 0);

        fighter.x = 30;
        fighter.y = 40;
        fighter.health = 50;

        assertEquals(30, fighter.x, "X doit être modifiable");
        assertEquals(40, fighter.y, "Y doit être modifiable");
        assertEquals(50, fighter.health, "La santé doit être modifiable");
    }
}
