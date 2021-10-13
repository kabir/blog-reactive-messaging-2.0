package org.wildfly.blog.kafka.streams.app;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.wildfly.blog.kafka.streams.ConfigSupplier;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
@ApplicationScoped
public class MpConfigConfigSupplier implements ConfigSupplier {
    @Inject
    Config config;

    @Override
    public String getBootstrapServers() {
        return config.getValue("kafka.bootstrap.servers", String.class);
    }

    @Override
    public String getTopicName() {
        return config.getValue("kafka.topic", String.class);
    }
}
