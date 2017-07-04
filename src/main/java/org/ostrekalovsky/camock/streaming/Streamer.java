package org.ostrekalovsky.camock.streaming;

import org.ostrekalovsky.camock.ImageCollectionRepository;
import org.ostrekalovsky.camock.ImageRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * Created by Oleg Strekalovsky on 23.08.2016.
 */
public class Streamer {

    private static final String MAX_FPS_PARAM = "maxFPS";
    public static final int MAX_FPS = 1;

    private int parseMaxFPS(String queryString) {
        if (queryString == null) {
            return MAX_FPS;
        }
        String[] queryParameters = queryString.split("&");
        for (String queryParameter : queryParameters) {
            if (queryParameter.startsWith(MAX_FPS_PARAM + "=")) {
                return Integer.parseInt(queryParameter.substring((MAX_FPS_PARAM + "=").length()));
            }
        }
        return MAX_FPS;
    }

    public void newStreamOf(String imageId, int rotation, HttpServletRequest req, HttpServletResponse response, ImageRepository repository, ImageCollectionRepository collectionRepository) throws IOException, IllegalArgumentException {
        int maxFPS = parseMaxFPS(req.getQueryString());
        System.out.println("Starting stream:");
        System.out.println("maxFPS = " + maxFPS);
        System.out.println("rotation = " + rotation);
        Optional<Iterator<byte[]>> imageResource = repository.getResource(imageId, rotation);
        if (imageResource.isPresent()) {
            System.out.println("imageId = " + imageId);
            Stream stream = new Stream(imageId, rotation, maxFPS, req, response, imageResource.get());
        } else {
            Optional<Iterator<byte[]>> collectionResource = collectionRepository.getResource(imageId, rotation);
            if (collectionResource.isPresent()) {
                System.out.println("collectionId = " + imageId);
                Stream stream = new Stream(imageId, rotation, maxFPS, req, response, collectionResource.get());
            }else{
                response.sendError(404);
            }
        }

    }

}
