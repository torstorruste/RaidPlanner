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
        return participants.values().stream().flatMap(l->l.stream()).anyMatch(p->p.name.equals(player.name));
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

    public void removePlayer(Player player) {
        for(Player.Role role : participants.keySet()) {
            participants.get(role).remove(player);
        }
    }

    public int numParticipants() {
        int num = 0;
        for(List<Player> players : participants.values()) {
            num += players.size();
        }
        return num;
    }

    public enum Boss {
        Champion, Grong, JadefireMasters, Opulence, Conclave, Rastakhan, Mekkatorque, Blockade, Jaina, UNKNOWN
    }
}
