package org.superhelt.wow;

import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.dao.RaidDao;
import org.superhelt.wow.om.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RaidPlanner extends AbstractHandler {

    private static final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter tf = DateTimeFormatter.ISO_TIME;

    private RaidDao raidDao;
    private PlayerDao playerDao;

    public RaidPlanner(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super.handle(request, response.getWriter());
        PrintWriter writer = response.getWriter();

        String action = request.getParameter("action");
        listRaids(writer);

        if (request.getParameter("raid") != null) {
            LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), df);
            Raid raid = raidDao.getRaid(raidStart);


            if (action != null) {
                switch (action) {
                    case "addEncounter":
                        addEncounter(request, raid);
                        break;
                    case "addPlayer":
                        addPlayer(request, raid);
                        break;
                    case "removePlayer":
                        removePlayer(request, raid);
                        break;
                    case "addEvent":
                        addEvent(request, raid);
                        break;
                    case "removeEvent":
                        removeEvent(request, raid);
                        break;
                    case "finalize":
                        finalize(raid);
                        break;
                    case "reopen":
                        reopen(raid);
                        break;
                }
            }
            raid = raidDao.getRaid(raidStart);
            planRaid(writer, raid);

            String boss = request.getParameter("boss");
            if (boss != null) {
                planBoss(raid, Encounter.Boss.valueOf(boss), writer);
            }

            listAbsentees(raid, writer);
            showEvents(raid, writer);
        }
    }

    private void removeEvent(HttpServletRequest request, Raid raid) {
        LocalTime time = LocalTime.parse(request.getParameter("event"), tf);

        raidDao.removeEvent(raid, time);
    }

    private void addEvent(HttpServletRequest request, Raid raid) {
        Player player = playerDao.getByName(request.getParameter("player"));
        Event.EventType type = Event.EventType.valueOf(request.getParameter("type"));
        String comment = request.getParameter("comment");

        raid.events.add(new Event(LocalTime.now(), player, type, comment));

        raidDao.addEvent(raid, new Event(LocalTime.now(), player, type, comment));
    }

    private void showEvents(Raid raid, PrintWriter writer) {
        writer.println("<div>");

        List<Player> players = playerDao.getPlayers();

        writer.println("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"addEvent\"/>");
        writer.println("<select name=\"player\">");
        players.forEach(p -> writer.format("<option value=\"%s\">%s</option>", p.name, p.name));
        writer.print("</select>");
        writer.print("<select name=\"type\">");
        for (Event.EventType eventType : Event.EventType.values()) {
            writer.format("<option value=\"%s\">%s</option>", eventType, eventType);
        }
        writer.print("<input type=\"text\" name=\"comment\"/>");
        writer.format("<input type=\"hidden\" name=\"raid\" value=\"%s\"/>", df.format(raid.start));
        writer.print("</select><input type=\"submit\"></form>");

        writer.println("<ul>");
        for(Event event : raid.events) {
            if(!raid.isFinalized()) {
                writer.format("<form method=\"post\">");
            }
            writer.format("<ul>%s %s: %s", event.player.classString(), event.type, event.comment);
            if(!raid.isFinalized()) {
                writer.format("<input type=\"hidden\" name=\"raid\" value=\"%s\"/>",  df.format(raid.start));
                writer.format("<input type=\"hidden\" name=\"action\" value=\"removeEvent\"/>");
                writer.format("<input type=\"hidden\" name=\"event\" value=\"%s\"/>", tf.format(event.time));
                writer.format("<input type=\"submit\" value=\"Remove\"/></form>");
            }
            writer.println("</ul>");
        }

        writer.println("</div>");
    }

    private void listAbsentees(Raid raid, PrintWriter writer) {
        writer.println("<div>");

        if (raid.signups.stream().filter(s -> s.type == Signup.Type.TENTATIVE).count() > 0) {
            writer.println("<h2>Tentative</h2><ul>");
            raid.signups.stream().filter(s -> s.type == Signup.Type.TENTATIVE).forEach(s -> writer.format("<li>%s: %s</li>", s.player.classString(), s.comment));
            writer.println("</ul>");
        }

        if (raid.signups.stream().filter(s -> s.type == Signup.Type.DECLINED).count() > 0) {
            writer.println("<h2>Declined</h2><ul>");
            raid.signups.stream().filter(s -> s.type == Signup.Type.DECLINED).forEach(s -> writer.format("<li>%s: %s</li>", s.player.classString(), s.comment));
            writer.println("</ul>");
        }

        List<Player> knownPlayers = raid.signups.stream().map(s -> s.player).distinct().collect(Collectors.toList());
        if (knownPlayers.size() < playerDao.getPlayers().size()) {
            writer.println("<h2>Unknown</h2><ul>");
            playerDao.getPlayers().stream().filter(p -> !knownPlayers.contains(p)).forEach(p -> writer.format("<li>%s</li>", p.classString()));
            writer.println("</ul>");
        }

        List<Raid> raids = raidDao.getRaids();
        List<BenchedPlayer> benchedPlayers = getBenchedPlayers(raids, raid);

        if(benchedPlayers.size()>0) {
            writer.println("<h2>Benched</h2><ul>");
            benchedPlayers.sort(Comparator.comparingInt((BenchedPlayer a) -> a.numBenched).reversed());
            for(BenchedPlayer bp : benchedPlayers) {
                writer.format("<li>%s: %d (%d)</li>", bp.player.classString(), bp.numBenched, bp.numBenchedTotal);
            }
            writer.println("</ul>");
        }
        writer.println("</div>");
    }

    private List<BenchedPlayer> getBenchedPlayers(List<Raid> raids, Raid currentRaid) {
        List<BenchedPlayer> benchedPlayers = new ArrayList<>();
        for(Player player : currentRaid.acceptedPlayers()) {
            int numBenched = 0;
            int numBenchedTotal = 0;
            for(Raid raid : raids) {
                if(raid.isAccepted(player)) {
                    for (Encounter encounter : raid.encounters) {
                        if (!encounter.isParticipating(player)) {
                            if (raid.start.equals(currentRaid.start))
                                numBenched++;
                            numBenchedTotal++;
                        }
                    }
                }
            }
            if(numBenched>0) {
                benchedPlayers.add(new BenchedPlayer(player, numBenched, numBenchedTotal));
            }
        }
        return benchedPlayers;
    }

    private void removePlayer(HttpServletRequest request, Raid raid) {
        Encounter.Boss boss = Encounter.Boss.valueOf(request.getParameter("boss"));
        Player player = playerDao.getByName(request.getParameter("player"));

        raidDao.removePlayer(raid, boss, player);
    }

    private void addPlayer(HttpServletRequest request, Raid raid) {
        Encounter.Boss boss = Encounter.Boss.valueOf(request.getParameter("boss"));
        Player.Role role = Player.Role.valueOf(request.getParameter("role"));
        Player player = playerDao.getByName(request.getParameter("player"));

        raidDao.addPlayer(raid, boss, player, role);
    }

    private void addEncounter(HttpServletRequest request, Raid raid) {
        Encounter.Boss boss = Encounter.Boss.valueOf(request.getParameter("boss"));
        raidDao.addEncounter(raid, boss);
    }

    private void finalize(Raid raid) {
        raidDao.finalize(raid, LocalDateTime.now());
    }

    private void reopen(Raid raid) {
        raidDao.reopen(raid);
    }

    private void planRaid(PrintWriter writer, Raid raid) {
        writer.format("<div><h1>%s</h1>", df.format(raid.start));
        if (!raid.isFinalized()) {
            writer.format("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"finalize\"/><input type=\"hidden\"name=\"raid\" value=\"%s\"/><input type=\"submit\" value=\"Finalize\"></form>", df.format(raid.start));
            writer.format("<form method=\"post\" action=\"planRaid\"><input type=\"hidden\" name=\"raid\" value=\"%s\"/>", df.format(raid.start));
            writer.println("<input type=\"hidden\" name=\"action\" value=\"addEncounter\"/>");

            if (Arrays.stream(Encounter.Boss.values()).filter(b -> !raid.containsBoss(b)).count() > 0) {
                writer.println("Add encounter: <select name=\"boss\">");
                for (Encounter.Boss boss : Encounter.Boss.values()) {
                    if (!raid.containsBoss(boss) && boss!= Encounter.Boss.UNKNOWN) {
                        writer.format("<option value=\"%s\">%s</option>", boss, boss);
                    }
                }
                writer.println("</select><input type=\"submit\"/>");
                writer.println("</form>");
            }
        } else {
            writer.format("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"reopen\"/><input type=\"hidden\"name=\"raid\" value=\"%s\"/><input type=\"submit\" value=\"Reopen\"></form>", df.format(raid.start));
        }

        writer.println("<h1>Encounters</h1>");
        raid.encounters.forEach(e -> {
            writer.format("<a href=\"?raid=%s&boss=%s\">%s</a><br/>\n", df.format(raid.start), e.boss, e.boss);
        });
        writer.println("</div>");
    }

    private void planBoss(Raid raid, Encounter.Boss boss, PrintWriter writer) {
        Encounter encounter = raid.getEncounter(boss);
        List<Player> players = raid.acceptedPlayers();
        writer.format("<div><h1>%s (%d)</h1>", boss, encounter.numParticipants());


        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Tank);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Healer);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Melee);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Ranged);


        writer.format("<h1>Bench (%d)</h1>", players.stream().filter(p -> !encounter.isParticipating(p)).count());
        if (!raid.isFinalized()) {
            writer.println("<table><tr><th>Player</th><th>Tank</th><th>Healer</th><th>Melee</th><th>Ranged</th></tr>");
            for (Player player : players) {
                if (!encounter.isParticipating(player)) {
                    writer.format("<tr><td>%s</td>", player.classString());
                    for (Player.Role role : Player.Role.values()) {
                        if (player.roles.contains(role) && !raid.isFinalized()) {
                            writer.format("<td><a href=\"?action=addPlayer&raid=%s&boss=%s&role=%s&player=%s\">%s</a></td>",
                                    df.format(raid.start), boss, role, player.name, role);
                        } else {
                            writer.println("<td></td>");
                        }
                    }
                    writer.println("</tr>");
                }
            }
            writer.println("</table>");
        } else {
            players.stream().filter(p->!encounter.isParticipating(p)).forEach(p->writer.format("%s<br/>", p.classString()));
        }

        writer.println("</div>");
    }

    private void printPlayersOfRole(Raid raid, Encounter.Boss boss, PrintWriter writer, Encounter encounter, List<Player> players, Player.Role role) {
        int numWithRole = encounter.getPlayersOfRole(role).size();
        if (numWithRole > 0) {
            writer.format("<h2>%s (%d)</h2>", role, numWithRole);
        } else {
            writer.format("<h2>%s</h2>", role);
        }

        if (!raid.isFinalized()) {
            encounter.getPlayersOfRole(role).forEach(p -> writer.format("<a href=\"?raid=%s&boss=%s&action=removePlayer&player=%s\">%s</a><br/>\n",
                    df.format(raid.start), boss, p.name, p.classString()));
        } else {
            encounter.getPlayersOfRole(role).forEach(p -> writer.format("%s<br/>", p.classString()));
        }
    }

    public void listRaids(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();
        writer.println("<div><h1>Raids</h1>");
        raids.forEach(r -> writer.format("<a href=\"?raid=%s\">%s</a><br/>\n", r.start, r.start));
        writer.println("</div>");
    }

    class BenchedPlayer {
        final Player player;
        final Integer numBenched;
        final Integer numBenchedTotal;

        public BenchedPlayer(Player player, Integer numBenched, Integer numBenchedTotal) {
            this.player = player;
            this.numBenched = numBenched;
            this.numBenchedTotal = numBenchedTotal;
        }
    }
}
