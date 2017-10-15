package org.superhelt.wow.dao;

import org.superhelt.wow.om.Raid;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

public class RaidDao {

    private static final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Connection conn;

    public RaidDao(Connection connection) {
        conn = connection;
    }

    public List<Raid> getRaids() {
        List<Raid> raids = new ArrayList<>();
        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from raid")) {
            while(rs.next()) {
                raids.add(new Raid(LocalDate.parse(rs.getString("start"), df)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return raids;
    }

    public Raid getRaid(LocalDate date) {

        try(PreparedStatement st = conn.prepareStatement("select * from raid where start=?")) {
            st.setString(1, df.format(date));
            try(ResultSet rs = st.executeQuery()) {
                while(rs.next()) {
                    return new Raid(LocalDate.parse(rs.getString("start"), df));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Unknown raid "+date);
    }

    public void addRaid(Raid raid) {
        try(PreparedStatement st = conn.prepareStatement("insert into raid values (?)")) {
            st.setString(1, df.format(raid.start));
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Unable to create raid "+raid.start);
        }
    }
}
