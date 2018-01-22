package org.superhelt.wow.om;

import java.util.List;

public class Player {
    public final String name;
    public final PlayerClass playerClass;
    public final List<Role> roles;

    public Player(String name, PlayerClass playerClass, List<Role> roles) {
        this.name = name;
        this.playerClass = playerClass;
        this.roles = roles;
    }

    public boolean hasRole(Role role) {
        return roles.stream().anyMatch(r->r==role);
    }

    public String classString() {
        return String.format("<span class=\"%s\">%s</span>", playerClass.toString().toLowerCase(), name);
    }

    public enum PlayerClass {
        Deathknight, DemonHunter, Druid, Paladin, Priest, Rogue, Warrior, Shaman, Warlock, Mage, Hunter, Monk
    }

    public enum Role {
        Tank, Healer, Melee, Ranged
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (name != null ? !name.equals(player.name) : player.name != null) return false;
        if (playerClass != player.playerClass) return false;
        return roles != null ? roles.equals(player.roles) : player.roles == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (playerClass != null ? playerClass.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }
}
