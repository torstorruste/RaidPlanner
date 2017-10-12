package org.superhelt.wow;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.superhelt.wow.dao.PlayerDao;
import org.superhelt.wow.dao.RaidDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class HttpHandler extends AbstractHandler {

    private RaidDao raidDao;
    private PlayerDao playerDao;

    public HttpHandler(RaidDao raidDao, PlayerDao playerDao) {
        this.raidDao = raidDao;
        this.playerDao = playerDao;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        baseRequest.setHandled(true);
        if(request.getRequestURI().endsWith(".css")) {
            serveCss(request, response);
        } else {
            EventViewer eventViewer = new EventViewer(raidDao, playerDao);
            RaidPlanner raidPlanner = new RaidPlanner(raidDao, playerDao);
            RaidInviter raidInviter = new RaidInviter(raidDao, playerDao);

            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);

            PrintWriter writer = response.getWriter();
            writer.print("<!DOCTYPE html><html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\"/><title>ANE PlayerNotes</title></head><body>");

            switch (request.getRequestURI()) {
                case "/showRaid":
                    eventViewer.showRaid(request, writer);
                    break;
                case "/planRaid":
                    raidPlanner.planRaids(request, response);
                    break;
                case "/showEvents":
                    eventViewer.printEvents(writer);
                    break;
                default:
                    raidInviter.showSignupPage(request, response);
            }

            writer.print("</body></html>");
        }
    }

    private void serveCss(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = request.getRequestURI();
        InputStream is = getClass().getResourceAsStream(fileName);

        if(is!=null) {
            response.setContentType("text/css");
            response.setStatus(HttpServletResponse.SC_OK);
            int next;
            while ((next = is.read()) != -1) {
                response.getWriter().write(next);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new HttpHandler(new RaidDao(), new PlayerDao()));

        server.start();
        server.join();
    }
}
