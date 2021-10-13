package org.wildfly.blog.reactive.messaging.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.kafka.common.serialization.Serializer;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class PageVisitsSerializer implements Serializer<PageVisit> {
    @Override
    public byte[] serialize(String topic, PageVisit pv) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(baos)) {
                out.writeUTF(pv.getUserName());
                out.writeUTF(pv.getAddress());
                out.writeUTF(pv.getPage());
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
