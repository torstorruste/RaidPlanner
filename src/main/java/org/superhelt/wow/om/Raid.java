package org.superhelt.wow.om;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Raid {

    public final LocalDate start;
    public final List<Event> events;
    public final List<Boss> bosses;

    public Raid(LocalDate start, List<Event> events) {
        this.start = start;
        this.events = events;
        this.bosses = new ArrayList<>();
    }
}
