package org.wildfly.blog.kafka.streams;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.wildfly.blog.reactive.messaging.common.PageVisit;
import org.wildfly.blog.reactive.messaging.common.PageVisitsSerializer;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class PageVisitSerde implements Serde<PageVisit> {
    @Override
    public Serializer<PageVisit> serializer() {
        return (topic, data) -> new PageVisitsSerializer().serialize(topic, data);
    }

    @Override
    public Deserializer<PageVisit> deserializer() {
        return (topic, data) -> {
            try {
                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
                    return new PageVisit(
                            in.readUTF(),
                            in.readUTF(),
                            in.readUTF());
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }
}
