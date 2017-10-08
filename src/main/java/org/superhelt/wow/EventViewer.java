package org.superhelt.wow;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.dao.RaidDao;
import org.superhelt.wow.om.Event;
import org.superhelt.wow.om.Player;
import org.superhelt.wow.om.Raid;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventViewer extends AbstractHandler {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        baseRequest.setHandled(true);
        if(request.getRequestURI().endsWith(".css")) {
            serveCss(response);
        } else {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);

            PrintWriter writer = response.getWriter();
            writer.print("<!DOCTYPE html><html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\"/><title>ANE PlayerNotes</title></head><body>");

            switch (request.getRequestURI()) {
                case "/addEvent":
                    addEvent(request, writer);
                    break;
                case "/addRaid":
                    addRaid(request, writer);
                    break;
                case "/showRaid":
                    showRaid(request, writer);
                    break;
                case "/planRaid":
                    new RaidPlanner().planRaid(request, response);
                default:
                    printEvents(writer);
            }

            writer.print("</body></html>");
        }
    }

    private void serveCss(HttpServletResponse response) throws IOException {
        response.setContentType("text/css");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter writer = response.getWriter();

        writer.println(".NOSHOW { background-color: #f46541; }");
        writer.println(".BENCH { background-color: #41cdf4; }");
        writer.println(".LATE { background-color: #f49542; }");
        writer.println(".SWAP { background-color: #9542f4; }");
    }

    private void showRaid(HttpServletRequest request, PrintWriter writer) {
        LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
        List<Raid> raids = new RaidDao().getRaids();
        List<Player> players = new PlayerDao().getPlayers();
        raids.stream().filter(r->r.start.equals(raidStart)).forEach(r->{
            writer.format("<table><tr><th>%s</th></tr>", dateFormatter.format(raidStart));

            players.forEach(p->{
                StringBuilder content = new StringBuilder();
                r.events.stream().filter(e->e.player.equals(p)).forEach(e->content.append(e.type).append(": ").append(e.comment));

                writer.format("<tr class=\"%s\"><td>%s</td><td>%s</td></tr>", p.playerClass, p.name, content);
            });
        });
    }

    private void addEvent(HttpServletRequest request, PrintWriter writer) {
        if("GET".equals(request.getMethod())) {
            String raid = request.getParameter("raid");
            List<Raid> raids = new RaidDao().getRaids();
            List<Player> players = new PlayerDao().getPlayers();
            if(raid!=null) {
                writer.format("<form method=\"post\"><input type=\"text\" name=\"time\" value=\"%s\"/>", timeFormatter.format(LocalTime.now()));
                writer.print("<select name=\"player\">");
                players.forEach(p -> writer.format("<option value=\"%s\">%s</option>", p.name, p.name));
                writer.print("</select>");
                writer.print("<select name=\"type\">");
                for (Event.EventType eventType : Event.EventType.values()) {
                    writer.format("<option value=\"%s\">%s</option>", eventType, eventType);
                }
                writer.print("<input type=\"text\" name=\"comment\"/>");
                writer.format("<input type=\"hidden\" name=\"raid\" value=\"%s\"/>", raid);
                writer.print("</select><input type=\"submit\"></form>");
            } else {
                writer.print("<form method=\"GET\"><select name=\"raid\">");
                raids.forEach(r->writer.format("<option value=\"%s\">%s</option>", dateFormatter.format(r.start), dateFormatter.format(r.start)));
                writer.print("</select><input type=\"submit\"/>");
            }
        } else {
            String raid = request.getParameter("raid");
            String player = request.getParameter("player");

            // TODO: Actually save new events
        }
    }

    private void addRaid(HttpServletRequest request, PrintWriter writer) throws IOException {
        List<Raid> raids = new RaidDao().getRaids();
        List<Player> players = new PlayerDao().getPlayers();
        if("GET".equals(request.getMethod())) {
            writer.format("<form method=\"post\"><input type=\"text\" name=\"date\" value=\"%s\"/><input type=\"submit\"/></form>", dateFormatter.format(LocalDate.now()));
        } else {
            LocalDate date = LocalDate.parse(request.getParameter("date"), dateFormatter);
            if(raids.stream().anyMatch(r->r.start.isEqual(date))) {
                writer.format("Raid at %s already exists", dateFormatter.format(date));
            } else {
                writer.format("Adding raid: %s", dateFormatter.format(date));
                raids.add(new Raid(date, new ArrayList<>()));
            }
        }
    }

    private void printEvents(PrintWriter writer) throws IOException {
        List<Raid> raids = new RaidDao().getRaids();
        List<Player> players = new PlayerDao().getPlayers();
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

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new EventViewer());

        server.start();
        server.join();
    }
}
