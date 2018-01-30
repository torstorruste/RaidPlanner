package org.superhelt.wow;

import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.om.Player;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerAdmin extends AbstractHandler {
    private final PlayerDao playerDao;

    public PlayerAdmin(PlayerDao playerDao) {

        this.playerDao = playerDao;
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super.handle(request, response.getWriter());

        String action = request.getParameter("action");
        if (action != null && action.equals("new")) {
            newPlayer(request);
        } else if(action!=null && action.equals("edit")) {
            updatePlayer(request);
        }
        listNewPlayer(response.getWriter());
        listPlayers(response.getWriter());
    }

    private void newPlayer(HttpServletRequest request) {
        Player player = deserializePlayer(request);

        playerDao.addPlayer(player);
    }

    private void listNewPlayer(PrintWriter writer) {
        writer.print("<form method=\"post\"><input type=\"hidden\" name=\"action\" value=\"new\">");
        writer.print("<table><tr><th>Name</th><th>Class</th><th>Tank</th><th>Healer</th><th>Melee</th><th>Ranged</th></tr>");
        writer.print("<tr><td><input type=\"text\" name=\"name\"/></td><td><select name=\"class\">");
        for(Player.PlayerClass c : Player.PlayerClass.values()) {
            writer.format("<option value=\"%s\">%s</option>", c, c);
        }
        writer.print("</td>");
        for(Player.Role role : Player.Role.values()) {
            writer.format("<td><input type=\"checkbox\" name=\"%s\"/></td>", role);
        }
        writer.print("<td><input type=\"submit\" value=\"Add\"/></td>");
        writer.print("</table>");
    }

    private void updatePlayer(HttpServletRequest request) {
        String originalName = request.getParameter("originalName");
        Player updatedPlayer = deserializePlayer(request);
        playerDao.updatePlayer(originalName, updatedPlayer);
    }

    private Player deserializePlayer(HttpServletRequest request) {
        String name = request.getParameter("name");
        String playerClass = request.getParameter("class");
        String tank = request.getParameter("Tank");
        String healer = request.getParameter("Healer");
        String melee = request.getParameter("Melee");
        String ranged = request.getParameter("Ranged");
        boolean active = request.getParameter("active")!=null;

        List<Player.Role> roles = new ArrayList<>();
        if(tank!=null) roles.add(Player.Role.Tank);
        if(healer!=null) roles.add(Player.Role.Healer);
        if(melee!=null) roles.add(Player.Role.Melee);
        if(ranged!=null) roles.add(Player.Role.Ranged);

        return new Player(name, Player.PlayerClass.valueOf(playerClass), roles, active);
    }

    private void listPlayers(PrintWriter writer) {
        writer.print("<table><tr><th>Name</th><th>Class</th><th>Tank</th><th>Healer</th><th>Melee</th><th>Ranged</th><th>Active</th></tr>");
        List<Player> players = playerDao.getAllPlayers();
        players.sort(Comparator.comparing(a -> a.name.toLowerCase()));
        for(Player player : players) {
            writer.format("<tr><td><form method=\"post\"><input type=\"text\" name=\"name\" value=\"%s\"/></td>", player.name);
            writer.print("<td><select name=\"class\">");
            for(Player.PlayerClass c : Player.PlayerClass.values()) {
                if(player.playerClass==c) {
                    writer.format("<option value=\"%s\" selected>%s</option>", c.toString(), c.toString());
                } else {
                    writer.format("<option value=\"%s\">%s</option>", c.toString(), c.toString());
                }
            }
            writer.print("</select></td>");

            for(Player.Role role : Player.Role.values()) {
                if(player.hasRole(role)) {
                    writer.format("<td><input type=\"checkbox\" name=\"%s\" checked/></td>", role);
                } else {
                    writer.format("<td><input type=\"checkbox\" name=\"%s\"/></td>", role);
                }
            }

            if(player.active) {
                writer.print("<td><input type=\"checkbox\" name=\"active\" checked/></td>");
            } else {
                writer.print("<td><input type=\"checkbox\" name=\"active\"/></td>");
            }

            writer.print("<td><input type=\"submit\" value=\"Edit\">");
            writer.format("<input type=\"hidden\" name=\"originalName\" value=\"%s\"/>", player.name);
            writer.print("<input type=\"hidden\" name=\"action\" value=\"edit\"/></form></td></tr>");
        }
        writer.println("</table>");
    }
}
