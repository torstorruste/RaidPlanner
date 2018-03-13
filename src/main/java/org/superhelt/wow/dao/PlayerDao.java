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

    public List<Player> getActivePlayers() {
        List<Player> players = new ArrayList<>();

        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from player where active=1 order by name")) {
            while(rs.next()) {
                players.add(map(rs));
            }
        } catch (SQLException e) {
            log.error("Unable to find players", e);
        }

        return players;
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();

        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from player order by name")) {
            while(rs.next()) {
                players.add(map(rs));
            }
        } catch (SQLException e) {
            log.error("Unable to find players", e);
        }

        return players;
    }

    public Player map(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        Player.PlayerClass playerClass = Player.PlayerClass.valueOf(rs.getString("class"));
        List<Player.Role> roles = getRoles(rs);
        boolean active = rs.getInt("active")==1;

        return new Player(name, playerClass, roles, active);
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
                    boolean active = rs.getInt("active")==1;

                    return new Player(name, playerClass, roles, active);
                }
            }
        } catch (SQLException e) {
            log.error("Unable to find player with name {}", name, e);
        }

        return new Player(name, Player.PlayerClass.Druid, new ArrayList<>(), false);
    }

    public void updatePlayer(String originalName, Player player) {
        try(PreparedStatement st = conn.prepareStatement("update player set name=?, class=?, roles=?, active=? where name=?")) {
            st.setString(1, player.name);
            st.setString(2, player.playerClass.toString());
            st.setString(3, serializeRoles(player));
            st.setInt(4, player.active?1:0);
            st.setString(5, originalName);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Unable to update player {}", player, e);
        }
    }

    public void addPlayer(Player player) {
        try(PreparedStatement st = conn.prepareStatement("insert into player (name, class, roles) values (?, ?, ?)")) {
            st.setString(1, player.name);
            st.setString(2, player.playerClass.toString());
            st.setString(3, serializeRoles(player));
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Unable to add player", e);
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


