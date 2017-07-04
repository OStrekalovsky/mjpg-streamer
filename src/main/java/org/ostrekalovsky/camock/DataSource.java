package org.ostrekalovsky.camock;

import java.util.Iterator;
import java.util.Optional;

/**
 * Created by ostrekalovsky on 04.07.17.
 */
public interface DataSource {

    Optional<Iterator<byte[]>> getResource(String id, int rotation);
}
