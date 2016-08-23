package org.ostrekalovsky.camock;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by Oleg Strekalovsky on 22.08.2016.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        final String dbPath;
        if (args.length == 0) {
            dbPath = "./images";
        } else {
            dbPath = args[0];
        }
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/camock");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new CamockServlet(dbPath)), "/*");
        server.start();
        server.dumpStdErr();
        server.join();
    }

}
