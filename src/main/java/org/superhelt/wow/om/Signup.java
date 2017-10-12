package org.superhelt.wow.om;

import java.time.LocalDateTime;

public class Signup {

    public final LocalDateTime time;
    public final Player player;
    public final Type type;
    public final String comment;

    public Signup(LocalDateTime time, Player player, Type type, String comment) {
        this.time = time;
        this.player = player;
        this.type = type;
        this.comment = comment;
    }

    public enum Type {
        ACCEPTED, TENTATIVE, DECLINED
    }
}
