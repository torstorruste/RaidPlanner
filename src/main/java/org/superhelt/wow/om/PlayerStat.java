package org.superhelt.wow.om;

public class PlayerStat {
    private final Player player;
    private int today;
    private int twoWeeks;
    private int total;

    public PlayerStat(Player player) {
        this.player = player;
    }

    public PlayerStat(Player player, int today, int twoWeeks, int total) {
        this.player = player;
        this.today = today;
        this.twoWeeks = twoWeeks;
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

    public void incrementToday() {
        total++;
    }

    public void incrementTwoWeeks() {
        twoWeeks++;
    }

    public void incrementTotal() {
        total++;
    }
}
