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

public class EventViewer {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private RaidDao raidDao;
    private PlayerDao playerDao;

    public EventViewer(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void showRaid(HttpServletRequest request, PrintWriter writer) {
        LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
        List<Player> players = playerDao.getPlayers();
        Raid raid =  raidDao.getRaid(raidStart);

        writer.format("<table><tr><th>%s</th></tr>", dateFormatter.format(raidStart));

        players.forEach(p->{
            StringBuilder content = new StringBuilder();
            raid.events.stream().filter(e->e.player.equals(p)).forEach(e->content.append(e.type).append(": ").append(e.comment));

            writer.format("<tr class=\"%s\"><td>%s</td><td>%s</td></tr>", p.playerClass, p.name, content);
        });
    }

    public void printEvents(PrintWriter writer) throws IOException {
        List<Raid> raids = raidDao.getRaids();
        List<Player> players = playerDao.getPlayers();
        writer.print("<table><tr><th>Player</th>");
        for(Raid raid : raids) {
            writer.format("<th><a href=\"/showRaid?raid=%s\">%s</a></th>", dateFormatter.format(raid.start), dateFormatter.format(raid.start));
        }
        writer.println("</tr>");
        for(Player player : players) {
            writer.format("<tr><th class='%s'>%s</th>", player.playerClass, player.name);

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
