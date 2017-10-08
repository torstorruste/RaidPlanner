package org.superhelt.wow.om;

import java.util.*;

public class Encounter {

    public final Boss boss;
    public final Map<Player.Role, List<Player>> participants;

    public Encounter(Boss boss) {
        this.boss = boss;
        this.participants = new HashMap<>();
    }

    public boolean isParticipating(Player player) {
        return participants.values().stream().flatMap(l->l.stream()).anyMatch(p->p.equals(player));
    }

    public List<Player> getPlayersOfRole(Player.Role role) {
        if(participants.containsKey(role)) return participants.get(role);
        else return Collections.emptyList();
    }

    public void addPlayer(Player player, Player.Role role) {
        if(!participants.containsKey(role)) {
            participants.put(role, new ArrayList<>());
        }
        participants.get(role).add(player);
    }

    public enum Boss {
        Goroth, Inquisition, Harjatan, Sisters, DesolateHost, Mistress, Maiden, Avatar, KilJaeden
    }
}
