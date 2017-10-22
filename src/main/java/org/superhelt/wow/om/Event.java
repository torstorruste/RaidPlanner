package org.superhelt.wow.om;

import java.time.LocalTime;

public class Event {

    public final LocalTime time;
    public final Player player;
    public final EventType type;
    public final String comment;

    public Event(LocalTime time, Player player, EventType type, String comment) {
        this.time = time;
        this.player = player;
        this.type = type;
        this.comment = comment;
    }

    public enum EventType {
        LATE, NOSHOW, BENCH, SWAP, PERFORMANCE
    }
}
