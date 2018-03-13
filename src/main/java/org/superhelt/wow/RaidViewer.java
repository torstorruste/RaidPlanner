package org.superhelt.wow;

import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.dao.RaidDao;
import org.superhelt.wow.om.Encounter;
import org.superhelt.wow.om.Player;
import org.superhelt.wow.om.Raid;
import org.superhelt.wow.om.Signup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.superhelt.wow.om.Signup.Type.ACCEPTED;

public class RaidViewer {
    private final RaidDao raidDao;
    private final PlayerDao playerDao;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public RaidViewer(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        listRaids(response.getWriter());

        if(request.getParameter("raid")!=null) {
            LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
            Raid raid = raidDao.getRaid(raidStart);

            if(raid.isFinalized()) {
                for (Encounter encounter : raid.encounters) {
                    showBoss(response.getWriter(), raid, encounter);
                }

                listRaidInfo(raid, response.getWriter());
            }
        }
    }

    private void showBoss(PrintWriter writer, Raid raid, Encounter encounter) {
        writer.format("<div><h1>%s (%d)</h1>", encounter.boss, encounter.numParticipants());

        printPlayersOfRole(writer, encounter, Player.Role.Tank);
        printPlayersOfRole(writer, encounter, Player.Role.Healer);
        printPlayersOfRole(writer, encounter, Player.Role.Melee);
        printPlayersOfRole(writer, encounter, Player.Role.Ranged);

        printBench(writer, encounter, raid);

        writer.println("</div>");
    }

    private void printBench(PrintWriter writer, Encounter encounter, Raid raid) {
        List<Player> players = raid.signups.stream().filter(s -> s.type == ACCEPTED).map(s -> s.player).collect(Collectors.toList());
        List<Player> participatingPlayers = players.stream().filter(p -> !encounter.isParticipating(p)).collect(Collectors.toList());

        writer.format("<h2>Bench (%d)</h2>", participatingPlayers.size());
        participatingPlayers.forEach(p->writer.format("%s<br/>", p.classString()));
    }

    private void printPlayersOfRole(PrintWriter writer, Encounter encounter, Player.Role role) {
        int numWithRole = encounter.getPlayersOfRole(role).size();
        if(numWithRole>0) {
            writer.format("<h2>%s (%d)</h2>", role, numWithRole);
        } else {
            writer.format("<h2>%s</h2>", role);
        }

        encounter.getPlayersOfRole(role).forEach(p->writer.format("%s<br/>\n",p.classString()));
    }

    public void listRaids(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();
        writer.println("<div><h1>Raids</h1>");
        raids.stream().filter(r->r.isFinalized()).forEach(r->writer.format("<a href=\"?raid=%s\">%s</a><br/>\n", r.start, r.start));
        writer.println("</div>");
    }

    private void listRaidInfo(Raid raid, PrintWriter writer) {
        writer.println("<div>");

        if(raid.isFinalized()) {
            writer.format("<h1>Roster finalized</h1><p>%s</p>", timeFormatter.format(raid.finalized));
        } else {
            writer.print("<h1>Roster being setup</h1>");
        }

        if(raid.signups.stream().filter(s->s.type== Signup.Type.TENTATIVE).count()>0) {
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
        if(knownPlayers.size() < playerDao.getActivePlayers().size()) {
            writer.println("<h2>Unknown</h2><ul>");
            playerDao.getActivePlayers().stream().filter(p -> !knownPlayers.contains(p)).forEach(p -> writer.format("<li>%s</li>", p.classString()));
            writer.println("</ul>");
        }

        writer.println("</div>");
    }
}
