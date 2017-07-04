package org.ostrekalovsky.camock;

import org.ostrekalovsky.camock.streaming.Streamer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Oleg Strekalovsky on 22.08.2016.
 */
public class CamockServlet extends HttpServlet {

    private final String dbPath;
    Streamer streamer = new Streamer();
    private final ImageCollectionRepository collectionRepository;

    public CamockServlet(String dbPath) throws IOException {
        this.dbPath = dbPath;
        collectionRepository = new ImageCollectionRepository(dbPath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String context = req.getRequestURI().substring(req.getContextPath().length());
        ImageRepository repository = new ImageRepository(dbPath);
        if ("/".equals(context) || context.isEmpty()) {
            printDB(resp, req.getRequestURL().toString(), repository);
        } else if (context.startsWith("/view")) {
            String imageName = context.substring("/view/".length());
            getImage(resp, imageName, repository);
        } else if (context.startsWith("/mjpeg")) {
            String imageName = context.substring("/mjpeg/".length());
            System.out.println("imageName = " + imageName);
            getStream(req, resp, imageName, repository, collectionRepository);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getStream(HttpServletRequest req, HttpServletResponse resp, String imageId, ImageRepository repository, ImageCollectionRepository collectionRepository) throws IOException {
        String rotationSt = req.getParameter("rotation");
        int rotation = 0;
        if (rotationSt != null && !rotationSt.isEmpty()) {
            rotation = Integer.parseInt(rotationSt);
            if (!(rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        try {
            streamer.newStreamOf(imageId, rotation, req, resp, repository, collectionRepository);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void getImage(HttpServletResponse resp, String imageName, ImageRepository repository) throws IOException {
        byte[] imageWithRotation = repository.getImageWithRotation(imageName, 0);
        if (imageWithRotation == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            resp.setContentLength(imageWithRotation.length);
            resp.setContentType("image/jpeg");
            resp.getOutputStream().write(imageWithRotation);
            resp.flushBuffer();
        }
    }

    private void printDB(HttpServletResponse resp, String urlPrefix, ImageRepository repository) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.print("<html><body>");
        writer.print("<center>DB content. Files pattern:" + repository.getPattern() + "</center>");
        writer.print("<h1>Single images</h1>");
        writer.print("<table border=\"1\">");
        writer.print("<th>Picture</th><th>Download JPEG URL</th><th>MJPEG Stream URL</th><th>Content</th>");
        for (ImageRepository.ImageInfo info : repository.getImages()) {
            writer.print("<tr>");

            writer.print("<td>");
            writer.print(info.getName());
            writer.print("</td>");

            writer.print("<td>");
            writer.print(buildDownloadUrl(urlPrefix, info.getId()));
            writer.print("</td>");

            writer.print("<td>");
            writer.print(buildMJPEGStreamUrl(urlPrefix, info.getId()));
            writer.print("</td>");

            writer.print("<td>");
            writer.print("<img alt=\"" + info.getName() + "\" src=\"" + buildDownloadUrl(urlPrefix, info.getId()) + "\" />");
            writer.print("</td>");

            writer.print("</tr>");
        }
        writer.print("</table>");
        writer.print("<h1>Collections of images</h1>");
        writer.print("<table border=\"1\">");
        writer.print("<th>Name</th><th>MJPEG Stream URL</th><th>Content</th>");
        for (ImageCollectionRepository.FolderInfo folder : collectionRepository.getCollection()) {
            writer.print("<tr>");
            writer.print("<td>" + folder.getId() + "</td>");
            writer.print("<td>" + buildMJPEGStreamUrl(urlPrefix, folder.getId()) + "</td>");
            writer.print("<td>Files in folder: " + folder.getSize() + "</td>");
            writer.print("</tr>");
        }

        writer.print("<table>");
        writer.print("</body></html>");
    }

    private String buildDownloadUrl(String urlPrefix, String id) {
        return urlPrefix + "view/" + id;
    }

    private String buildMJPEGStreamUrl(String urlPrefix, String id) {
        return urlPrefix + "mjpeg/" + id;
    }
}
