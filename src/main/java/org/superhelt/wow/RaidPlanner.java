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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RaidPlanner {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

    private RaidDao raidDao;
    private PlayerDao playerDao;

    public RaidPlanner(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void planRaids(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();

        String action = request.getParameter("action");
        if(action!=null && action.equals("addRaid")) {
            addRaid(request, writer);
        }

        listRaids(writer);

        if(request.getParameter("raid")!=null) {
            LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
            Raid raid = raidDao.getRaid(raidStart);


            if(action!=null) {
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
                }
            }
            planRaid(writer, raid);

            String boss = request.getParameter("boss");
            if(boss!=null) {
                planBoss(raid, Encounter.Boss.valueOf(boss), writer);
            }

            listAbsentees(raid, writer);
            showEvents(raid, writer);
        }
    }

    private void addEvent(HttpServletRequest request, Raid raid) {
        Player player = playerDao.getByName(request.getParameter("player"));
        Event.EventType type = Event.EventType.valueOf(request.getParameter("type"));
        String comment = request.getParameter("comment");

        raid.events.add(new Event(LocalTime.now(), player, type, comment));
    }

    private void showEvents(Raid raid, PrintWriter writer) {
        writer.println("<div style=\"float: left\">");

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
        writer.format("<input type=\"hidden\" name=\"raid\" value=\"%s\"/>", dateFormatter.format(raid.start));
        writer.print("</select><input type=\"submit\"></form>");

        writer.println("<ul>");
        raid.events.forEach(e->writer.format("<ul>%s %s: %s</ul>", e.player.classString(), e.type, e.comment));
        writer.println("</ul>");

        writer.println("</div>");
    }

    private void listAbsentees(Raid raid, PrintWriter writer) {
        writer.println("<div style=\"float: left\">");

        if(raid.signups.stream().filter(s->s.type==Signup.Type.TENTATIVE).count()>0) {
            writer.println("<h2>Tentative</h2><ul>");
            raid.signups.stream().filter(s -> s.type == Signup.Type.TENTATIVE).forEach(s -> writer.format("<li>%s: %s</li>", s.player.classString(), s.comment));
            writer.println("</ul>");
        }

        if(raid.signups.stream().filter(s->s.type==Signup.Type.DECLINED).count()>0) {
            writer.println("<h2>Declined</h2><ul>");
            raid.signups.stream().filter(s -> s.type == Signup.Type.DECLINED).forEach(s -> writer.format("<li>%s: %s</li>", s.player.classString(), s.comment));
            writer.println("</ul>");
        }

        List<Player> knownPlayers = raid.signups.stream().map(s -> s.player).distinct().collect(Collectors.toList());
        if(knownPlayers.size() < playerDao.getPlayers().size()) {
            writer.println("<h2>Unknown</h2><ul>");
            playerDao.getPlayers().stream().filter(p -> !knownPlayers.contains(p)).forEach(p -> writer.format("<li>%s</li>", p.classString()));
            writer.println("</ul>");
        }

        writer.println("</div>");
    }

    private void addRaid(HttpServletRequest request, PrintWriter writer) {
        LocalDate date = LocalDate.parse(request.getParameter("date"), dateFormatter);
        List<Raid> raids = raidDao.getRaids();
        if(raids.stream().anyMatch(r->r.start.isEqual(date))) {
            writer.format("<h2>Raid at %s already exists</h2>", dateFormatter.format(date));
        } else {
            writer.format("<h2>Adding raid: %s</h2>", dateFormatter.format(date));
            raidDao.addRaid(new Raid(date));
        }
    }

    private void removePlayer(HttpServletRequest request, Raid raid) {
        Encounter.Boss boss = Encounter.Boss.valueOf(request.getParameter("boss"));
        Player player = playerDao.getByName(request.getParameter("player"));

        raid.getEncounter(boss).removePlayer(player);
    }

    private void addPlayer(HttpServletRequest request, Raid raid) {
        Encounter.Boss boss = Encounter.Boss.valueOf(request.getParameter("boss"));
        Player.Role role = Player.Role.valueOf(request.getParameter("role"));
        Player player = playerDao.getByName(request.getParameter("player"));

        raid.getEncounter(boss).addPlayer(player, role);
    }

    private void addEncounter(HttpServletRequest request, Raid raid) {
        Encounter.Boss boss = Encounter.Boss.valueOf(request.getParameter("boss"));
        System.out.println("Adding boss "+boss+" to raid "+dateFormatter.format(raid.start));
        raid.encounters.add(new Encounter(boss));
        raidDao.addEncounter(raid, boss);
    }

    private void planRaid(PrintWriter writer, Raid raid) {
        writer.format("<div style=\"float: left; width: 300px;\"><h1>%s</h1>", dateFormatter.format(raid.start));
        writer.format("<form method=\"post\" action=\"planRaid\"><input type=\"hidden\" name=\"raid\" value=\"%s\"/>", dateFormatter.format(raid.start));
        writer.println("<input type=\"hidden\" name=\"action\" value=\"addEncounter\"/>");

        if(Arrays.stream(Encounter.Boss.values()).filter(b->!raid.containsBoss(b)).count()>0) {
            writer.println("Add encounter: <select name=\"boss\">");
            for (Encounter.Boss boss : Encounter.Boss.values()) {
                if (!raid.containsBoss(boss)) {
                    writer.format("<option value=\"%s\">%s</option>", boss, boss);
                }
            }
            writer.println("</select><input type=\"submit\"/>");
            writer.println("</form>");
        }

        writer.println("<h1>Encounters</h1>");
        raid.encounters.forEach(e->{
            writer.format("<a href=\"?raid=%s&boss=%s\">%s</a><br/>\n", dateFormatter.format(raid.start), e.boss, e.boss);
        });
        writer.println("</div>");
    }

    private void planBoss(Raid raid, Encounter.Boss boss, PrintWriter writer) {
        Encounter encounter = raid.getEncounter(boss);
        List<Player> players = raid.acceptedPlayers();
        writer.format("<div style=\"float: left; width: 400px;\"><h1>%s (%d)</h1>", boss, encounter.numParticipants());


        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Tank);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Healer);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Melee);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Ranged);

        writer.println("<table><tr><th>Player</th><th>Tank</th><th>Healer</th><th>Melee</th><th>Ranged</th></tr>");
        for(Player player : players) {
            if(!encounter.isParticipating(player)) {
                writer.format("<tr><td>%s</td>", player.classString());
                for (Player.Role role : Player.Role.values()) {
                    if (player.roles.contains(role)) {
                        writer.format("<td><a href=\"?action=addPlayer&raid=%s&boss=%s&role=%s&player=%s\">%s</a></td>",
                                dateFormatter.format(raid.start), boss, role, player.name, role);
                    } else {
                        writer.println("<td></td>");
                    }
                }
                writer.println("</tr>");
            }
        }
        writer.println("</table>");

        writer.println("</div>");
    }

    private void printPlayersOfRole(Raid raid, Encounter.Boss boss, PrintWriter writer, Encounter encounter, List<Player> players, Player.Role role) {
        int numWithRole = encounter.getPlayersOfRole(role).size();
        if(numWithRole>0) {
            writer.format("<h2>%s (%d)</h2>", role, numWithRole);
        } else {
            writer.format("<h2>%s</h2>", role);
        }

        encounter.getPlayersOfRole(role).forEach(p->writer.format("<a href=\"?raid=%s&boss=%s&action=removePlayer&player=%s\">%s</a><br/>\n",
                dateFormatter.format(raid.start), boss, p.name, p.classString()));
    }

    public void listRaids(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();
        writer.println("<div style=\"float: left; width: 200px\"><h1>Raids</h1>");
        writer.format("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"addRaid\"/><input type=\"text\" name=\"date\" value=\"%s\"/><br/><input type=\"submit\"/></form>", dateFormatter.format(LocalDate.now()));
        raids.forEach(r->writer.format("<a href=\"?raid=%s\">%s</a><br/>\n", r.start, r.start));
        writer.println("</div>");
    }
}
