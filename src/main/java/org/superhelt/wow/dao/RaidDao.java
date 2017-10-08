package org.superhelt.wow.dao;

import org.superhelt.wow.om.Event;
import org.superhelt.wow.om.Player;
import org.superhelt.wow.om.Raid;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RaidDao {

    public static List<Raid> raids = new ArrayList<>();

    static {
        List<Player> players = new PlayerDao().getPlayers();

        List<Event> events = new ArrayList<>();
        events.add(new Event(LocalTime.of(20, 0), players.get(1), Event.EventType.NOSHOW, "Unable to attend due to IRL-issues"));
        raids.add(new Raid(LocalDate.of(2017, 10, 1), events));

        events = new ArrayList<>();
        events.add(new Event(LocalTime.of(20, 30), players.get(3), Event.EventType.LATE, "Arrived half an hour late"));
        events.add(new Event(LocalTime.of(21, 30), players.get(5), Event.EventType.LATE, "Arrived at break"));
        events.add(new Event(LocalTime.of(20, 0), players.get(0), Event.EventType.BENCH, "Chosen for bench"));
        raids.add(new Raid(LocalDate.of(2017,10,2), events));

        events = new ArrayList<>();
        events.add(new Event(LocalTime.of(22,30), players.get(0), Event.EventType.SWAP, "Could not get microphone to work"));
        raids.add(new Raid(LocalDate.of(2017, 10, 4), events));
    }


    public List<Raid> getRaids() {
        return raids;
    }
}
