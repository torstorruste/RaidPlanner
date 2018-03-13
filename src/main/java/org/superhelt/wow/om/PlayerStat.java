package org.superhelt.wow.om;

public class PlayerStat {
    private final Player player;
    private int today;
    private int twoWeeks;
    private int month;
    private int total;

    public PlayerStat(Player player) {
        this.player = player;
    }

    public PlayerStat(Player player, int today, int twoWeeks, int month, int total) {
        this.player = player;
        this.today = today;
        this.twoWeeks = twoWeeks;
        this.month = month;
        this.total = total;
    }

    public Player getPlayer() {
        return player;
    }

    public int getToday() {
        return today;
    }

    public int getTwoWeeks() {
        return twoWeeks;
    }

    public int getTotal() {
        return total;
    }

    public int getMonth() {
        return month;
    }

    public void incrementToday() {
        total++;
    }

    public void incrementTwoWeeks() {
        twoWeeks++;
    }

    public void incrementTotal() {
        total++;
    }

    public void incrementMonth() {
        month++;
    }
}
