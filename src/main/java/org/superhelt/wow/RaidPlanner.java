package org.superhelt.wow;

import org.superhelt.wow.dao.RaidDao;
import org.superhelt.wow.om.Raid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class RaidPlanner {

    private RaidDao raidDao;

    public RaidPlanner(RaidDao raidDao) {
        this.raidDao = raidDao;
    }

    public void planRaids(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getParameter("raid")==null) listRaids(response.getWriter());
        else planRaid(request, response.getWriter());
    }

    private void planRaid(HttpServletRequest request, PrintWriter writer) {

    }

    public void listRaids(PrintWriter writer) {
        List<Raid> raids = raidDao.getRaids();

        raids.forEach(r->writer.format("<a href=\"?raid=%s\">%s</a><br/>\n", r.start, r.start));
    }
}
