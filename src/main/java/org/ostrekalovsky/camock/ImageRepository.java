package org.ostrekalovsky.camock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Oleg Strekalovsky on 22.08.2016.
 */
public class ImageRepository implements DataSource {

    public static final Pattern pattern = Pattern.compile("[A-Za-z0-9\\-_\\.]+\\.(jpeg|jpg)");

    @Override
    public Optional<Iterator<byte[]>> getResource(String id, int rotation) {
        return Optional.ofNullable(getImageWithRotation(id, rotation)).map(data -> new Iterator<byte[]>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public byte[] next() {
                return data;
            }
        });
    }

    public static class ImageInfo {
        private final String id;

        private final String name;
        private final byte[] data;

        public ImageInfo(String name, String id, byte[] data) {
            this.name = Objects.requireNonNull(name, "file name should't be null");
            this.id = Objects.requireNonNull(id, "id shouldn't be null");
            this.data = Objects.requireNonNull(data, "image data shouldn't be null");
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public byte[] getData() {
            return data;
        }
    }

    public String getPattern() {
        return pattern.pattern();
    }

    private final HashMap<String, ImageInfo> idToImages = new HashMap<>();

    public ImageRepository(String dbPath) throws IOException {
        File[] jpegs = new File(dbPath).listFiles((dir, name) -> pattern.matcher(name).matches());
        if (jpegs != null) {
            for (File jpeg : jpegs) {
                ImageInfo imageInfo = new ImageInfo(jpeg.getName(), jpeg.toPath().getFileName().toString(), Files.readAllBytes(jpeg.toPath()));
                idToImages.put(imageInfo.getId(), imageInfo);
            }
        }
    }

    public List<ImageInfo> getImages() {
        return new ArrayList<>(idToImages.values());
    }

    public byte[] getImageWithRotation(String imageId, int nClockwiseRotations) {
        if (!(0 <= nClockwiseRotations && nClockwiseRotations <= 3)) {
            throw new IllegalStateException("number of clockwise rotation should be [0,3]");
        }
        ImageInfo imageInfo = idToImages.get(imageId);
        if (imageInfo == null) {
            return null;
        }
        byte[] data = imageInfo.getData();
        Image image = new Image(data);
        for (int i = 0; i < nClockwiseRotations; i++) {
            image.rotateClockwise();
        }
        return image.getByteArray();
    }
}
