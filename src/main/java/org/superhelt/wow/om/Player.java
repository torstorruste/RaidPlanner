package org.superhelt.wow.om;

public class Player {
    public final String name;
    public final PlayerClass playerClass;

    public Player(String name, PlayerClass playerClass) {
        this.name = name;
        this.playerClass = playerClass;
    }

    public enum PlayerClass {
        Deathknight, Druid, Paladin, Priest, Rogue, Warrior
    }
}
