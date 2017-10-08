package org.superhelt.wow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RaidPlanner {
    public void planRaid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getParameter("raid")==null) listRaids(response.getWriter());
    }

    public void listRaids(PrintWriter writer) {

    }
}
