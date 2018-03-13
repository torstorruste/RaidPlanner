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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class RaidInviter extends AbstractHandler {

    private static final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RaidDao raidDao;
    private final PlayerDao playerDao;

    public RaidInviter(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super.handle(request, response.getWriter());
        PrintWriter writer = response.getWriter();

        String action = request.getParameter("action");
        if (action != null && action.equals("addRaid")) {
            addRaid(request, writer);
        }

        listRaids(writer);
        if (request.getParameter("raid") != null) {
            LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
            Raid raid = raidDao.getRaid(raidStart);

            if (action != null) {
                switch (action) {
                    case "signup":
                        signup(request, writer, raid);
                        break;
                    case "unsign":
                        unsign(request, writer, raid);
                        break;
                }
            }

            raid = raidDao.getRaid(raidStart);

            printSignupForm(writer, raid);
            printSignups(writer, raid);
        } else {
            printSignupStats(writer);
        }
    }

    private void printSignupStats(PrintWriter writer) {
        writer.println("<h1>Stats</h1>");

        Map<Player, PlayerStat> tentativeMap = new HashMap<>();
        Map<Player, PlayerStat> declinedMap = new HashMap<>();
        Map<Player, PlayerStat> unknownMap = new HashMap<>();
        Map<Player, PlayerStat> noShowMap = new HashMap<>();
        Map<Player, PlayerStat> lateMap = new HashMap<>();

        List<Player> players = playerDao.getActivePlayers();
        List<Raid> raids = raidDao.getRaids();
        raids.sort(Comparator.comparing(r -> r.start));
        for (Player player : players) {
            PlayerStat tenative = tentativeMap.computeIfAbsent(player, (k) -> new PlayerStat(k));
            PlayerStat declined = declinedMap.computeIfAbsent(player, (k) -> new PlayerStat(k));
            PlayerStat unknown = unknownMap.computeIfAbsent(player, (k) -> new PlayerStat(k));
            PlayerStat noShow = noShowMap.computeIfAbsent(player, (k) -> new PlayerStat(k));
            PlayerStat late = lateMap.computeIfAbsent(player, (k) -> new PlayerStat(k));
            boolean alreadyJoined = false;
            for (Raid raid : raids) {
                if (alreadyJoined || raid.getSignupStatus(player).isPresent()) {

                    Optional<Signup.Type> type = raid.getSignupStatus(player);
                    if (!type.isPresent()) { // Unknown - did not sign up for the raid
                        incrementStats(unknown, raid);
                    } else if (type.get().equals(Signup.Type.TENTATIVE)) {
                        incrementStats(tenative, raid);
                    } else if (type.get().equals(Signup.Type.DECLINED)) {
                        incrementStats(declined, raid);
                    }
                    alreadyJoined = true;
                }
                List<Event> events = raid.events.stream().filter(e -> e.player.equals(player)).collect(Collectors.toList());

                for (Event event : events) {
                    switch (event.type) {
                        case NOSHOW:
                            incrementStats(noShow, raid);
                            break;
                        case LATE:
                            incrementStats(late, raid);
                            break;
                    }
                }
            }
        }

        writer.println("<table class=\"statTable\"><tr><th>Player</th><th colspan=\"3\">Tentative</th><th colspan=\"3\">Declined</th>");
        writer.println("<th colspan=\"3\">Unknown</th><th colspan=\"3\">Noshow</th><th colspan=\"3\">Late</th></tr>");

        printHeaders(writer, 5);
        for (Player player : players) {
            writer.format("<tr><td>%s</td>", player.classString());
            printStats(writer, tentativeMap.get(player));
            printStats(writer, declinedMap.get(player));
            printStats(writer, unknownMap.get(player));
            printStats(writer, noShowMap.get(player));
            printStats(writer, lateMap.get(player));
            writer.println("</tr>");
        }
        writer.println("</table>");
    }

    private void printHeaders(PrintWriter writer, int numHeaders) {

        writer.print("<tr><th></th>");
        for(int i=0;i<numHeaders;i++) {
            writer.print("<th>Two Weeks</th><th>Month</th><th>Total</th>");
        }
        writer.println("</tr>");
    }

    private void printStats(PrintWriter writer, PlayerStat stat) {
        String twoWeekClass = getCellClass(stat.getTwoWeeks(), 1, 4);
        String monthClass = getCellClass(stat.getMonth(), 3, 8);
        String totalClass = getCellClass(stat.getTotal(), 10, 20);
        writer.format("<td class=\"%s\">%d</td><td class=\"%s\">%d</td><td class=\"%s\">%d</td>",
                twoWeekClass, stat.getTwoWeeks(),
                monthClass, stat.getMonth(),
                totalClass, stat.getTotal());
    }

    private String getCellClass(int num, int yellowThreshold, int redThreshold) {
        if(num>=redThreshold) return "red";
        if(num>=yellowThreshold) return "yellow";
        return "green";
    }

    private void incrementStats(PlayerStat stat, Raid raid) {
        stat.incrementTotal();
        if (raid.start.equals(LocalDate.now())) {
            stat.incrementToday();
        }
        if (raid.start.isAfter(LocalDate.now().minus(2, ChronoUnit.WEEKS))) {
            stat.incrementTwoWeeks();
        }
        if(raid.start.isAfter(LocalDate.now().minus(1, ChronoUnit.MONTHS))) {
            stat.incrementMonth();
        }
    }

    private void printSignups(PrintWriter writer, Raid raid) {
        writer.println("<div><h1>Signups</h1><ul>");
        for (Signup signup : raid.signups) {
            writer.println("<li>");
            if (!raid.isFinalized()) {
                writer.println("<form method=\"post\">");
            }
            switch (signup.type) {
                case ACCEPTED:
                    writer.format("%s signed up for the raid", signup.player.classString());
                    break;
                case TENTATIVE:
                    writer.format("%s is tentative with the following comment: %s", signup.player.classString(), signup.comment);
                    break;
                case DECLINED:
                    writer.format("%s declined the raid with the following comment: %s", signup.player.classString(), signup.comment);
                    break;
            }
            if (!raid.isFinalized()) {
                writer.format("<input type=\"hidden\" name=\"raid\" value=\"%s\"/><input type=\"hidden\" name=\"player\" value=\"%s\"/>" +
                        "<input type=\"hidden\" name=\"action\" value=\"unsign\"><input type=\"submit\" value=\"remove\"></form>", dateFormatter.format(raid.start), signup.player.name);
            }
            writer.print("</li>");
        }

        writer.println("</ul></div>");
    }

    private void signup(HttpServletRequest request, PrintWriter writer, Raid raid) {
        String[] players = request.getParameterValues("player");
        for (String playerName : players) {
            Player player = playerDao.getByName(playerName);
            Signup.Type type = Signup.Type.valueOf(request.getParameter("type"));
            String comment = request.getParameter("comment");

            if (type != Signup.Type.ACCEPTED && (comment == null || comment.isEmpty())) {
                writer.format("<h2>Signups of type %s require a comment</h2>", type);
            } else {
                Signup signup = new Signup(LocalDateTime.now(), player, type, comment);
                raidDao.addSignup(raid, signup);
            }
        }
    }

    private void unsign(HttpServletRequest request, PrintWriter writer, Raid raid) {
        String player = request.getParameter("player");

        raidDao.removeSignup(raid, player);

    }

    private void printSignupForm(PrintWriter writer, Raid raid) {
        writer.format("<div><h1>%s</h1>", dateFormatter.format(raid.start));

        if (raid.isFinalized()) {
            writer.format("<h2>Raid is finalized</h2>");
        } else {
            writer.println("<script language=\"JavaScript\">function toggle(source) {\n" +
                    "  checkboxes = document.getElementsByName('player');\n" +
                    "  for(var i=0, n=checkboxes.length;i<n;i++) {\n" +
                    "    checkboxes[i].checked = source.checked;\n" +
                    "  }\n" +
                    "}" +
                    "</script>");
            writer.format("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"signup\"><input type=\"hidden\" name=\"raid\" value=\"%s\"/>", raid.start);
            writer.println("<input type=\"checkbox\" onClick=\"toggle(this)\"/>Toggle all<br/>");
            for (Player player : playerDao.getActivePlayers()) {
                if (!raid.signups.stream().anyMatch(s -> s.player.name.equals(player.name))) {
                    writer.format("<input type=\"checkbox\" name=\"player\" value=\"%s\">%s<br/>", player.name, player.classString());
                }
            }
            writer.println("<select name=\"type\">");
            for (Signup.Type type : Signup.Type.values()) {
                writer.format("<option value=\"%s\">%s</option>", type, type);
            }
            writer.println("</select><input type=\"text\" name=\"comment\" maxlength=\"200\" placeholder=\"comment if not accepted\"/>");
            writer.println("<input type=\"submit\"></form>");
        }
        writer.println("</div>");
    }

    public void listRaids(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();
        writer.println("<div><h1>Raids</h1>");
        writer.format("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"addRaid\"/><input type=\"text\" name=\"time\" value=\"%s\"/><br/><input type=\"submit\"/></form>", df.format(LocalDate.now()));
        raids.forEach(r -> writer.format("<a href=\"?raid=%s\">%s</a><br/>\n", r.start, r.start));
        writer.println("</div>");
    }

    private void addRaid(HttpServletRequest request, PrintWriter writer) {
        LocalDate date = LocalDate.parse(request.getParameter("time"), df);
        List<Raid> raids = raidDao.getRaids();
        if (raids.stream().anyMatch(r -> r.start.isEqual(date))) {
            writer.format("<h2>Raid at %s already exists</h2>", df.format(date));
        } else {
            writer.format("<h2>Adding raid: %s</h2>", df.format(date));
            raidDao.addRaid(new Raid(date));
        }
    }
}
