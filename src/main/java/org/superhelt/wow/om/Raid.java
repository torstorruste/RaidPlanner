package org.superhelt.wow.om;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Raid {

    public final LocalDate start;
    public final List<Event> events;
    public final List<Encounter> encounters;
    public final List<Signup> signups;


    public Raid(LocalDate start, List<Event> events) {
        this.start = start;
        this.events = events;
        this.encounters = new ArrayList<>();
        this.signups = new ArrayList<>();
    }

    public boolean containsBoss(Encounter.Boss boss) {
        return encounters.stream().anyMatch(e->e.boss==boss);
    }

    public Encounter getEncounter(Encounter.Boss boss) {
        return encounters.stream().filter(e->e.boss==boss).findFirst().orElseThrow(()->new IllegalArgumentException("No encounter with boss "+boss));
    }

    public List<Player> acceptedPlayers() {
        return this.signups.stream().map(s->s.player).distinct().collect(Collectors.toList());
    }
}
