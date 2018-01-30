package org.superhelt.wow.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.superhelt.wow.om.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerDao {

    private static final Logger log = LoggerFactory.getLogger(PlayerDao.class);

    private final Connection conn;

    public PlayerDao(Connection connection) {
        conn = connection;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from player")) {
            while(rs.next()) {
                String name = rs.getString("name");
                Player.PlayerClass playerClass = Player.PlayerClass.valueOf(rs.getString("class"));
                List<Player.Role> roles = getRoles(rs);

                players.add(new Player(name, playerClass, roles));
            }

        } catch (SQLException e) {
            log.error("Unable to find players", e);
        }

        return players;
    }

    private List<Player.Role> getRoles(ResultSet rs) throws SQLException {
        String[] roles = rs.getString("roles").split(",");

        return Arrays.stream(roles).map(s-> Player.Role.valueOf(s.trim())).collect(Collectors.toList());

    }

    public Player getByName(String name) {
        try(PreparedStatement st = conn.prepareStatement("select * from player where name=?")) {
            st.setString(1, name);

            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Player.PlayerClass playerClass = Player.PlayerClass.valueOf(rs.getString("class"));
                    List<Player.Role> roles = getRoles(rs);

                    return new Player(name, playerClass, roles);
                }
            }
        } catch (SQLException e) {
            log.error("Unable to find player with name {}", name, e);
        }

        throw new IllegalArgumentException("Unknown player "+name);
    }

    public void updatePlayer(String originalName, Player player) {
        try(PreparedStatement st = conn.prepareStatement("update player set name=?, class=?, roles=? where name=?")) {
            st.setString(1, player.name);
            st.setString(2, player.playerClass.toString());
            st.setString(3, serializeRoles(player));
            st.setString(4, originalName);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Unable to update player {}", player, e);
        }
    }

    private String serializeRoles(Player player) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for(Player.Role role : player.roles) {
            sb.append(sep).append(role);
            sep =",";
        }

        return sb.toString();
    }
}


