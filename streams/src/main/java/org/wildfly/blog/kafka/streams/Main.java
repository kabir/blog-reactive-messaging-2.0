package org.wildfly.blog.kafka.streams;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.streams.errors.InvalidStateStoreException;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class Main {
    public static void main(String[] args) throws Exception {
        try (DataStoreWrapper dsw = new DataStoreWrapper()) {
            dsw.init();
            Map<String, String> lastPagesByUser = Collections.emptyMap();
            try {
                dsw.readLastVisitedPageByUsers();
            } catch (InvalidStateStoreException e) {
            }
            if (lastPagesByUser.size() == 0) {
                // It seems that although the stream is reported as RUNNING
                // in dsw.init() it still needs some time to settle. Until that
                // happens there is no data or we get InvalidStateStoreException
                Thread.sleep(4000);
                lastPagesByUser = dsw.readLastVisitedPageByUsers();
            }
            System.out.println("Last pages visited:\n" + lastPagesByUser);
        }
    }
}
