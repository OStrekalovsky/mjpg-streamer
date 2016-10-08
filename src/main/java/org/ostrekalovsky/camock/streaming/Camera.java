package org.ostrekalovsky.camock.streaming;

import org.ostrekalovsky.camock.ImageRepository;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Oleg Strekalovsky on 23.08.2016.
 */
public class Camera {

    private final ImageRepository repository;
    private final Consumer<byte[]> consumer;
    private int rotation = 0;
    private String imageId;
    private volatile byte[] data = new byte[1];
    private int fps = 20;
    private volatile ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
    private volatile ScheduledFuture<?> scheduledFuture;

    public static Camera getCameraInstance(ImageRepository repository, String imageId, int rotation, int maxFPS, Consumer<byte[]> consumer) {
        Camera camera = new Camera(repository, consumer, imageId, rotation, maxFPS);
        camera.scheduledFuture = camera.executorService.scheduleAtFixedRate(() -> consumer.accept(camera.data), 1000, 1000 / camera.fps, TimeUnit.MILLISECONDS);
        return camera;
    }

    private Camera(ImageRepository repository, Consumer<byte[]> consumer, String imageId, int rotation, int maxFPS) {
        this.repository = repository;
        this.consumer = consumer;
        this.imageId = imageId;
        this.rotation = rotation;
        this.fps = maxFPS;
        byte[] imageWithRotation = repository.getImageWithRotation(imageId, rotation);
        if (imageWithRotation == null) {
            throw new IllegalArgumentException("Image with id=" + imageId + " not found");
        }
        this.data = imageWithRotation;
    }

    public synchronized void setImage(String imageId) throws IllegalArgumentException {
        if (this.imageId.equals(imageId)) {
            return;
        }
        byte[] imageWithRotation = repository.getImageWithRotation(imageId, rotation);
        if (imageWithRotation == null) {
            throw new IllegalArgumentException("Image with id=" + imageId + " not found");
        }
        this.imageId = imageId;
        this.data = imageWithRotation;
    }

    public synchronized void setRotation(int rotation) {
        if (rotation == this.rotation) {
            return;
        }
        byte[] imageWithRotation = repository.getImageWithRotation(imageId, rotation / 90);
        if (imageWithRotation == null) {
            throw new IllegalArgumentException("Image with id=" + imageId + " not found");
        }
        this.rotation = rotation;
        this.data = imageWithRotation;
    }

    public synchronized void setMaxFps(int fps) {
        if (fps == this.fps) {
            return;
        }
        this.fps = fps;
        scheduledFuture.cancel(true);
        scheduledFuture = executorService.scheduleAtFixedRate(() -> consumer.accept(data), 1000, 1000 / fps, TimeUnit.MILLISECONDS);
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
