package org.superhelt.wow.om;

import java.time.LocalTime;

public class Event {

    public final LocalTime date;
    public final Player player;
    public final EventType type;
    public final String comment;

    public Event(LocalTime date, Player player, EventType type, String comment) {
        this.date = date;
        this.player = player;
        this.type = type;
        this.comment = comment;
    }

    public enum EventType {
        BENCH, NOSHOW, SLACK, SWAP, LATE
    }
}
