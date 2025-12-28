package fr.uparis.informatique.cpoo5.liquidwar.model.entities;

import fr.uparis.informatique.cpoo5.liquidwar.config.GameConfig;

public class Fighter {
    public int x, y;
    public int team;
    public int health;

    public Fighter(int x, int y, int team) {
        this.x = x;
        this.y = y;
        this.team = team;
        this.health = GameConfig.FIGHTER_INITIAL_HEALTH;
    }
}
