package org.superhelt.wow;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class AbstractHandler {

    public void handle(HttpServletRequest request, PrintWriter writer) throws IOException {
        printMenu(writer);
    }

    protected void printMenu(PrintWriter writer) {
        writer.println("<div style=\"clear:both; width: 100%\" ><a href=\"/signup\">Signups</a> <a href=\"/planRaid\">Plan</a> <a href=\"showEvents\">Events</a> <a href=\"player\">Players</a></div>");
    }
}
