package org.superhelt.wow.dao;

import org.superhelt.wow.om.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerDao {

    public static List<Player> players = new ArrayList<>();

    static {
        players.add(new Player("Brew", Player.PlayerClass.Druid));
        players.add(new Player("Dunder", Player.PlayerClass.Priest));
        players.add(new Player("Furo", Player.PlayerClass.Paladin));
        players.add(new Player("Lashin", Player.PlayerClass.Deathknight));
        players.add(new Player("Mattis", Player.PlayerClass.Druid));
        players.add(new Player("Rza", Player.PlayerClass.Warrior));
        players.add(new Player("Slip", Player.PlayerClass.Rogue));
        players.add(new Player("Zikura", Player.PlayerClass.Druid));
    }

    public List<Player> getPlayers() {
        return players;
    }
}
