package org.wildfly.blog.reactive.messaging.user.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.wildfly.blog.reactive.messaging.common.PageVisit;

import io.smallrye.reactive.messaging.kafka.api.KafkaMetadataUtil;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
@ApplicationScoped
public class MessagingBean {
    @Inject
    @Channel("special")
    Emitter<PageVisit> special;

    @Incoming("from-filter")
    @Outgoing("kafka-visits")
    public Message<PageVisit> fromFilter(PageVisit pageVisit) {
        if (pageVisit.getPage().equals("3.html")) {
            special.send(pageVisit);
        }
        Message<PageVisit> msg = Message.of(pageVisit);
        msg = KafkaMetadataUtil.writeOutgoingKafkaMetadata(
                msg,
                OutgoingKafkaRecordMetadata
                        .<String>builder()
                        .withKey(pageVisit.getUserName())
                        .build());
        return msg;
    }

    @Incoming("special")
    public void special(PageVisit pageVisit) {
        System.out.println("===> " + pageVisit.getUserName() + " visited " + pageVisit.getPage());
    }
}
