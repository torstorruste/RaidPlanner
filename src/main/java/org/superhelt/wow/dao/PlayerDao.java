package org.superhelt.wow.dao;

import org.superhelt.wow.om.Player;

import java.util.ArrayList;
import java.util.List;

import static org.superhelt.wow.om.Player.PlayerClass.*;
import static org.superhelt.wow.om.Player.Role.*;

public class PlayerDao {

    public static List<Player> players = new ArrayList<>();

    static {
        players.add(new Player("Brew", Druid, Healer));
        players.add(new Player("Dunder", Priest, Ranged));
        players.add(new Player("Furo", Paladin, Healer));
        players.add(new Player("Lashin", Deathknight, Melee));
        players.add(new Player("Mattis", Druid, Ranged, Tank));
        players.add(new Player("Rza", Warrior, Melee));
        players.add(new Player("Slip", Rogue, Melee));
        players.add(new Player("Zikura", Druid, Ranged, Healer, Tank));
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getByName(String name) {
        return players.stream().filter(p->p.name.equals(name)).findFirst().orElseThrow(()->new IllegalArgumentException("No player with name "+name));
    }
}

