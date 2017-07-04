package org.ostrekalovsky.camock.streaming;

import org.ostrekalovsky.camock.DataSource;
import org.ostrekalovsky.camock.ImageRepository;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Oleg Strekalovsky on 23.08.2016.
 */
public class Camera {

    private final Consumer<byte[]> consumer;
    private int rotation = 0;
    private String imageId;
    private int fps = 20;
    private volatile ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

    public static Camera getCameraInstance(Iterator<byte[]> content, String imageId, int rotation, int maxFPS, Consumer<byte[]> consumer) {
        Camera camera = new Camera(consumer, imageId, rotation, maxFPS);
        camera.executorService.scheduleAtFixedRate(() ->
                consumer.accept(content.next()), 1000, 1000 / camera.fps, TimeUnit.MILLISECONDS);
        return camera;
    }

    private Camera(Consumer<byte[]> consumer, String imageId, int rotation, int maxFPS) {
        this.consumer = consumer;
        this.imageId = imageId;
        this.rotation = rotation;
        this.fps = maxFPS;
    }

    public void stop() {
        System.out.println("Stop the camera:" + this);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)){
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    @Override
    public String toString() {
        return "Camera{" +
                "imageId='" + imageId + '\'' +
                ", rotation=" + rotation +
                ", fps=" + fps +
                '}';
    }
}
