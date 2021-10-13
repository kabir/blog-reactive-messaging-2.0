package org.wildfly.blog.kafka.streams;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public interface ConfigSupplier {
    String getBootstrapServers();

    String getTopicName();
}
