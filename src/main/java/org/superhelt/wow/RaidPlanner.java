package org.superhelt.wow;

import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.dao.RaidDao;
import org.superhelt.wow.om.Encounter;
import org.superhelt.wow.om.Player;
import org.superhelt.wow.om.Raid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RaidPlanner {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private RaidDao raidDao;
    private PlayerDao playerDao;

    public RaidPlanner(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void planRaids(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getParameter("raid")==null) listRaids(response.getWriter());
        else {
            LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
            Raid raid = raidDao.getRaid(raidStart);

            String action = request.getParameter("action");

            if(action!=null) {
                switch (action) {
                    case "addEncounter":
                        addEncounter(request, raid);
                        break;
                    case "addPlayer":
                        addPlayer(request, raid);
                        break;

                }
            }
            planRaid(response.getWriter(), raid);

            String boss = request.getParameter("boss");
            if(boss!=null) {
                planBoss(raid, Encounter.Boss.valueOf(boss), response.getWriter());
            }
        }
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
    }

    private void planRaid(PrintWriter writer, Raid raid) {
        writer.format("<div style=\"float: left;\"><h1>%s</h1>", dateFormatter.format(raid.start));
        writer.format("<form method=\"post\" action=\"planRaid\"><input type=\"hidden\" name=\"raid\" value=\"%s\"/>", dateFormatter.format(raid.start));
        writer.println("<input type=\"hidden\" name=\"action\" value=\"addEncounter\"/>");
        writer.println("Add encounter: <select name=\"boss\">");
        for(Encounter.Boss boss : Encounter.Boss.values()) {
            if(!raid.containsBoss(boss)) {
                writer.format("<option value=\"%s\">%s</option>", boss, boss);
            }
        }
        writer.println("</select><input type=\"submit\"/>");
        writer.println("</form>");

        writer.println("<h1>Encounters</h1>");
        raid.encounters.forEach(e->{
            writer.format("<a href=\"?raid=%s&boss=%s\">%s</a><br/>\n", dateFormatter.format(raid.start), e.boss, e.boss);
        });
        writer.println("</div>");
    }

    private void planBoss(Raid raid, Encounter.Boss boss, PrintWriter writer) {
        writer.format("<div style=\"float: left;\"><h1>%s</h1>", boss);

        Encounter encounter = raid.getEncounter(boss);
        List<Player> players = playerDao.getPlayers();

        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Tank);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Healer);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Melee);
        printPlayersOfRole(raid, boss, writer, encounter, players, Player.Role.Ranged);
    }

    private void printPlayersOfRole(Raid raid, Encounter.Boss boss, PrintWriter writer, Encounter encounter, List<Player> players, Player.Role role) {
        writer.format("<h2>%s</h2>", role);
        encounter.getPlayersOfRole(role).forEach(p->writer.format("%s<br/>\n", p.name));


        long numAvailablePlayers = players.stream().filter(p->p.hasRole(role)).filter(p->!encounter.isParticipating(p)).count();

        if(numAvailablePlayers>0) {
            writer.format("<form method=\"post\" action=\"planRaid\">");
            writer.format("<input type=\"hidden\" name=\"action\" value=\"addPlayer\"/>");
            writer.format("<input type=\"hidden\" name=\"raid\" value=\"%s\"/>", dateFormatter.format(raid.start));
            writer.format("<input type=\"hidden\" name=\"boss\" value=\"%s\"/>", boss);
            writer.format("<input type=\"hidden\" name=\"role\" value=\"%s\"/>", role);
            writer.format("<select name=\"player\">");

            players.stream().filter(p -> !encounter.isParticipating(p)).filter(p -> p.hasRole(role)).forEach(p -> {
                writer.format("<option value=\"%s\">%s</option>", p.name, p.name);
            });
            writer.format("</select><input type=\"submit\">");
            writer.format("</form>");
        }
    }

    public void listRaids(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();

        raids.forEach(r->writer.format("<a href=\"?raid=%s\">%s</a><br/>\n", r.start, r.start));
    }
}
