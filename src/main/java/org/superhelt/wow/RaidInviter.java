package org.superhelt.wow;

import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.dao.RaidDao;
import org.superhelt.wow.om.Player;
import org.superhelt.wow.om.Raid;
import org.superhelt.wow.om.Signup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RaidInviter {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RaidDao raidDao;
    private final PlayerDao playerDao;

    public RaidInviter(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    public void showSignupPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        listRaids(writer);
        if(request.getParameter("raid")!=null) {
            LocalDate raidStart = LocalDate.parse(request.getParameter("raid"), dateFormatter);
            Raid raid = raidDao.getRaid(raidStart);

            String action = request.getParameter("action");
            if(action !=null) {
                switch (action) {
                    case "signup":
                        signup(request, writer, raid);
                        break;
                }
            }

            printSignupForm(writer, raid);
            printSignups(writer, raid);
        }
    }

    private void printSignups(PrintWriter writer, Raid raid) {
        writer.println("<div style=\"float: left;\"><h1>Signups</h1><ul>");
        for(Signup signup : raid.signups) {
            switch(signup.type) {
                case ACCEPTED:
                    writer.format("<li>%s signed up for the raid</li>", signup.player.classString());
                    break;
                case TENTATIVE:
                    writer.format("<li>%s is tentative with the following comment: %s", signup.player.classString(), signup.comment);
                    break;
                case DECLINED:
                    writer.format("<li>%s declined the raid with the following comment: %s", signup.player.classString(), signup.comment);
            }
        }
        writer.println("</ul></div>");
    }

    private void signup(HttpServletRequest request, PrintWriter writer, Raid raid) {
        Player player = playerDao.getByName(request.getParameter("player"));
        Signup.Type type = Signup.Type.valueOf(request.getParameter("type"));
        String comment = request.getParameter("comment");

        if(type != Signup.Type.ACCEPTED && (comment==null || comment.isEmpty())) {
            writer.format("<h2>Signups of type %s require a comment</h2>", type);
        } else {
            Signup signup = new Signup(LocalDateTime.now(), player, type, comment);
            raidDao.addSignup(raid, signup);
            raid.signups.add(signup);
        }
    }

    private void printSignupForm(PrintWriter writer, Raid raid) {
        writer.format("<div style=\"float: left;\"><h1>%s</h1>", dateFormatter.format(raid.start));

        writer.format("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"signup\"><input type=\"hidden\" name=\"raid\" value=\"%s\"/>", raid.start);
        writer.println("<select name=\"player\">");
        for(Player player : playerDao.getPlayers()) {
            writer.format("<option value=\"%s\">%s</option>", player.name, player.name);
        }
        writer.println("</select><select name=\"type\">");
        for(Signup.Type type : Signup.Type.values()) {
            writer.format("<option value=\"%s\">%s</option>", type, type);
        }
        writer.println("</select><input type=\"text\" name=\"comment\" maxlength=\"200\" placeholder=\"comment if not accepted\"/>");
        writer.println("<input type=\"submit\"></form>");
        writer.println("</div>");
    }

    public void listRaids(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();
        writer.println("<div style=\"float: left; width: 200px\"><h1>Raids</h1>");
        raids.forEach(r->writer.format("<a href=\"?raid=%s\">%s</a><br/>\n", r.start, r.start));
        writer.println("</div>");
    }
}
