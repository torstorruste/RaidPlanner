package org.superhelt.wow.dao;

import org.superhelt.wow.om.Encounter;
import org.superhelt.wow.om.Player;
import org.superhelt.wow.om.Raid;
import org.superhelt.wow.om.Signup;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RaidDao {

    private static final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter tf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Connection conn;
    private final PlayerDao playerDao;

    public RaidDao(Connection connection) {
        conn = connection;
        playerDao = new PlayerDao(conn);
    }

    public List<Raid> getRaids() {
        List<Raid> raids = new ArrayList<>();
        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from raid")) {
            while(rs.next()) {
                Raid raid = new Raid(LocalDate.parse(rs.getString("start"), df));
                addEncounters(raid);
                addSignups(raid);
                raids.add(raid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Collections.sort(raids, Comparator.comparing((Raid r) -> r.start).reversed());
        return raids;
    }

    public Raid getRaid(LocalDate date) {
        try(PreparedStatement st = conn.prepareStatement("select * from raid where start=?")) {
            st.setString(1, df.format(date));
            try(ResultSet rs = st.executeQuery()) {
                while(rs.next()) {
                    Raid raid = new Raid(LocalDate.parse(rs.getString("start"), df));
                    addEncounters(raid);
                    addSignups(raid);
                    return raid;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Unknown raid "+date);
    }

    private void addSignups(Raid raid) {
        try(PreparedStatement st = conn.prepareStatement("select * from signup where raid=?")) {
            st.setString(1, df.format(raid.start));
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime time = LocalDateTime.parse(rs.getString("time"), tf);
                    Player player = playerDao.getByName(rs.getString("player"));
                    Signup.Type type = Signup.Type.valueOf(rs.getString("type"));
                    String comment = rs.getString("comment");

                    raid.signups.add(new Signup(time, player, type, comment));
                }
            }
        } catch (SQLException e) {
            System.out.println("Unable to add signups for raid "+raid.start);
        }
    }

    public void addRaid(Raid raid) {
        try(PreparedStatement st = conn.prepareStatement("insert into raid values (?)")) {
            st.setString(1, df.format(raid.start));
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Unable to create raid "+raid.start);
        }
    }

    public void addEncounter(Raid raid, Encounter.Boss boss) {
        try(PreparedStatement st = conn.prepareStatement("insert into encounter values (?, ?)")) {
            st.setString(1, df.format(raid.start));
            st.setString(2, boss.toString());

            st.executeUpdate();
        } catch(SQLException e) {
            System.out.println("Unable to add encounter "+boss+" to raid "+df.format(raid.start));
        }
    }

    public void addPlayer(Raid raid, Encounter.Boss boss, Player player, Player.Role role) {
        try(PreparedStatement st = conn.prepareStatement("insert into encounter_player values (?, ?, ?, ?)")) {
            st.setString(1, df.format(raid.start));
            st.setString(2, boss.toString());
            st.setString(3, player.name);
            st.setString(4, role.toString());

            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Unable to add player "+player.name+" to raid "+df.format(raid.start)+", boss "+boss);
        }
    }

    public void removePlayer(Raid raid, Encounter.Boss boss, Player player) {
        try(PreparedStatement st = conn.prepareStatement("delete from encounter_player where raid=? and boss=? and player=?")) {
            st.setString(1, df.format(raid.start));
            st.setString(2, boss.toString());
            st.setString(3, player.name);

            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Unable to remove player "+player.name+" from raid "+df.format(raid.start)+", boss "+boss);
        }
    }

    private void addEncounters(Raid raid) throws SQLException {
        try(PreparedStatement st = conn.prepareStatement("select * from encounter where raid=?")) {
            st.setString(1, df.format(raid.start));

            try(ResultSet rs = st.executeQuery()) {
                while(rs.next()) {
                    Encounter.Boss boss = Encounter.Boss.valueOf(rs.getString("boss"));
                    raid.encounters.add(new Encounter(boss));
                    addPlayers(raid, boss);
                }
            }
        }
    }

    private void addPlayers(Raid raid, Encounter.Boss boss) {
        try(PreparedStatement st = conn.prepareStatement("select * from encounter_player where raid=? and boss=?")) {
            st.setString(1, df.format(raid.start));
            st.setString(2, boss.toString());
            
            try(ResultSet rs = st.executeQuery()) {
                while(rs.next()) {
                    Player.Role role = Player.Role.valueOf(rs.getString("role"));
                    Player player = playerDao.getByName(rs.getString("player"));

                    raid.getEncounter(boss).addPlayer(player, role);
                }
            }
        } catch (SQLException e) {
            System.out.println("Unable to add players to raid "+df.format(raid.start)+", boss "+boss);
        }
    }

    public void addSignup(Raid raid, Signup signup) {
        try(PreparedStatement st = conn.prepareStatement("insert into signup values (?, ?, ?, ?, ?)")) {
            st.setString(1, df.format(raid.start));
            st.setString(2, tf.format(signup.time));
            st.setString(3, signup.player.name);
            st.setString(4, signup.type.toString());
            st.setString(5, signup.comment);

            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Unable to add signup to raid");
        }
    }

    public void removeSignup(Raid raid, String player) {
        try(PreparedStatement st = conn.prepareStatement("delete from signup where raid=? and player=?")) {
            st.setString(1, df.format(raid.start));
            st.setString(2, player);

            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Unable to remove signup from raid");
        }
    }
}
