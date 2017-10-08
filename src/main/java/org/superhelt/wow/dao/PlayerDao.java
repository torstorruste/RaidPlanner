package org.superhelt.wow.dao;

import org.superhelt.wow.om.Player;

import java.util.ArrayList;
import java.util.List;

import static org.superhelt.wow.om.Player.PlayerClass.*;
import static org.superhelt.wow.om.Player.Role.*;

public class PlayerDao {

    public static List<Player> players = new ArrayList<>();

    static {
        players.add(new Player("Twiniings", Druid, Tank));
        players.add(new Player("Gussie", Paladin, Tank));

        players.add(new Player("Lashin", Deathknight, Melee));
        players.add(new Player("Talltheuh", Deathknight, Melee, Tank));
        players.add(new Player("Drahc", Druid, Tank, Melee));
        players.add(new Player("Frozzenfire", Druid, Tank, Melee));
        players.add(new Player("Oxymortisai", Warrior, Melee));
        players.add(new Player("Rza", Warrior, Melee));
        players.add(new Player("Nestyyw", Warrior, Melee));
        players.add(new Player("Eliias", Rogue, Melee));
        players.add(new Player("Slip", Rogue, Melee));
        players.add(new Player("Bujumbura", DemonHunter, Melee));

        players.add(new Player("Rathhal", Druid, Healer, Ranged));
        players.add(new Player("Brew", Druid, Healer));
        players.add(new Player("Furo", Paladin, Healer));
        players.add(new Player("Cowstyle", Priest, Healer, Ranged));
        players.add(new Player("Drizz", Shaman, Healer));

        players.add(new Player("Dunder", Priest, Ranged));
        players.add(new Player("Crispy", Priest, Ranged));
        players.add(new Player("Gainsborough", Warlock, Ranged));
        players.add(new Player("Serthii", Warlock, Ranged));
        players.add(new Player("Infuszes", Mage, Ranged));
        players.add(new Player("Mattis", Druid, Ranged, Tank));
        players.add(new Player("Zikura", Druid, Ranged, Healer, Tank));
        players.add(new Player("Jorgypewpew", Hunter, Ranged));
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getByName(String name) {
        return players.stream().filter(p->p.name.equals(name)).findFirst().orElseThrow(()->new IllegalArgumentException("No player with name "+name));
    }
}


