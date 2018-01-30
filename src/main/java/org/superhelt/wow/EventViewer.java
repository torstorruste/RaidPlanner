package org.superhelt.wow;

import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.dao.RaidDao;
import org.superhelt.wow.om.Event;
import org.superhelt.wow.om.Player;
import org.superhelt.wow.om.Raid;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EventViewer extends AbstractHandler {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private RaidDao raidDao;
    private PlayerDao playerDao;

    public EventViewer(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void handle(HttpServletRequest request, PrintWriter writer) throws IOException {
        super.handle(request, writer);
        String raid = request.getParameter("raid");
        String player = request.getParameter("player");

        if(raid==null && player==null) {
            printAllEvents(writer);
        } else if(raid!=null) {
            LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
            printRaid(writer, raidDao.getRaid(raidStart));
        } else if(player!=null) {
            printPlayer(writer, playerDao.getByName(player));
        }
    }

    public void printPlayer(PrintWriter writer, Player player) {
        writer.format("<table><tr><th>%s</th></tr>", player.classString());

        raidDao.getRaids().stream().filter(r->r.events.stream().anyMatch(e->e.player.equals(player))).forEach(r-> {
                StringBuilder content = new StringBuilder();
                r.events.stream().filter(e->e.player.equals(player)).forEach(e->content.append(e.type).append(": ").append(e.comment));

                writer.format("<tr><td>%s</td><td>%s</td></tr>", r.start, content);
            }
        );

        writer.format("</table>");
    }

    public void printRaid(PrintWriter writer, Raid raid) {
        List<Player> players = playerDao.getPlayers();
        writer.format("<table><tr><th>%s</th></tr>", dateFormatter.format(raid.start));

        players.forEach(p->{
            StringBuilder content = new StringBuilder();
            raid.events.stream().filter(e->e.player.equals(p)).forEach(e->content.append(e.type).append(": ").append(e.comment));

            writer.format("<tr><td class=\"%s\">%s</td><td>%s</td></tr>", p.playerClass.toString().toLowerCase(), p.name, content);
        });
        writer.println("</table>");
    }

    public void printAllEvents(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();
        List<Player> players = playerDao.getPlayers();
        writer.print("<table><tr><th>Player</th>");
        for(Raid raid : raids) {
            writer.format("<th><a href=\"?raid=%s\">%s</a></th>", dateFormatter.format(raid.start), dateFormatter.format(raid.start));
        }
        writer.println("</tr>");
        for(Player player : players) {
            writer.format("<tr><td><a href=\"?player=%s\">%s</a></td>", player.name, player.classString());

            for(Raid raid : raids) {
                StringBuilder classes = new StringBuilder();

                List<Event.EventType> types = raid.events.stream()
                        .filter(e -> e.player.equals(player))
                        .map(e -> e.type)
                        .distinct().collect(Collectors.toList());

                types.forEach(t->classes.append(t).append(" "));

                writer.format("<th class=\"%s\"></th>", classes);
            }
            writer.format("</tr>");
        }

        writer.print("</table>");
    }
}
