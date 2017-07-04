package org.ostrekalovsky.camock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by ostrekalovsky on 04.07.17.
 */
public class ImageCollectionRepository implements DataSource {

    public static final Pattern pattern = Pattern.compile("[A-Za-z0-9\\-_\\.]+\\.(jpeg|jpg)");

    @Override
    public Optional<Iterator<byte[]>> getResource(String id, int rotation) {
        return Optional.ofNullable(idToCollection.get(id)).map(folderInfo -> new Iterator<byte[]>() {

            private int fileIdx = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public byte[] next() {
                byte[] bytes = folderInfo.collection[fileIdx];
                if (fileIdx == folderInfo.collection.length - 1) {
                    fileIdx = 0;
                } else {
                    fileIdx++;
                }
                return bytes;
            }
        });
    }


    public static class FolderInfo {

        private final String name;
        private final byte collection[][];

        public FolderInfo(String collectionName, Path path) throws IOException {
            this.name = collectionName;
            File[] jpegs = path.toFile().listFiles((dir, name) -> pattern.matcher(name).matches());
            if (jpegs != null) {
                Arrays.sort(jpegs, Comparator.comparing(File::getName));
                int idx = 0;
                collection = new byte[jpegs.length][];
                for (File jpeg : jpegs) {
                    collection[idx] = Files.readAllBytes(jpeg.toPath());
                    idx++;
                }
            } else {
                collection = new byte[0][0];
            }
        }

        public int getSize(){
            return collection.length;
        }

        public String getId() {
            return name;
        }
    }

    private final HashMap<String, ImageCollectionRepository.FolderInfo> idToCollection = new HashMap<>();


    public ImageCollectionRepository(String dbPath) throws IOException {
        File[] files = new File(dbPath).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.canExecute() && file.canRead()) {
                    ImageCollectionRepository.FolderInfo collection = new ImageCollectionRepository.FolderInfo(file.getName(), file.toPath());
                    idToCollection.put(collection.getId(), collection);
                }
            }
        }
    }

    public Collection<FolderInfo> getCollection() {
        return idToCollection.values();
    }
}

