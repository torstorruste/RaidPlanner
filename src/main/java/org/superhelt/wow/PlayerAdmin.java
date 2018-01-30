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

        String originalName = request.getParameter("originalName");
        if(originalName!=null) {
            updatePlayer(request);
        }

        listPlayers(response.getWriter());
    }

    private void updatePlayer(HttpServletRequest request) {
        String originalName = request.getParameter("originalName");
        String name = request.getParameter("name");
        String playerClass = request.getParameter("class");
        String tank = request.getParameter("Tank");
        String healer = request.getParameter("Healer");
        String melee = request.getParameter("Melee");
        String ranged = request.getParameter("Ranged");

        List<Player.Role> roles = new ArrayList<>();
        if(tank!=null) roles.add(Player.Role.Tank);
        if(healer!=null) roles.add(Player.Role.Healer);
        if(melee!=null) roles.add(Player.Role.Melee);
        if(ranged!=null) roles.add(Player.Role.Ranged);


        Player updatedPlayer = new Player(name, Player.PlayerClass.valueOf(playerClass), roles);
        playerDao.updatePlayer(originalName, updatedPlayer);
    }

    private void listPlayers(PrintWriter writer) {
        writer.print("<table><tr><th>Name</th><th>Class</th><th>Tank</th><th>Healer</th><th>Melee</th><th>Ranged</th></tr>");
        List<Player> players = playerDao.getPlayers();
        players.sort(Comparator.comparing(a -> a.name));
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

            writer.format("<td><input type=\"submit\" value=\"Change\"><input type=\"hidden\" name=\"originalName\" value=\"%s\"/></form></td></tr>", player.name);
        }
        writer.println("</table>");
    }
}
