package org.superhelt.wow.om;

import java.util.Arrays;
import java.util.List;

public class Player {
    public final String name;
    public final PlayerClass playerClass;
    public final List<Role> roles;

    public Player(String name, PlayerClass playerClass, Role... roles) {
        this.name = name;
        this.playerClass = playerClass;
        this.roles = Arrays.asList(roles);
    }

    public boolean hasRole(Role role) {
        return roles.stream().anyMatch(r->r==role);
    }

    public String classString() {
        return String.format("<span class=\"%s\">%s</span>", playerClass.toString().toLowerCase(), name);
    }

    public enum PlayerClass {
        Deathknight, DemonHunter, Druid, Paladin, Priest, Rogue, Warrior, Shaman, Warlock, Mage, Hunter
    }

    public enum Role {
        Tank, Healer, Melee, Ranged
    }
}
