package org.wildfly.blog.kafka.streams;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.wildfly.blog.reactive.messaging.common.PageVisit;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
@ApplicationScoped
public class DataStoreWrapper implements Closeable {
    private volatile KafkaStreams streams;

    @Inject
    private ConfigSupplier configSupplier = new ConfigSupplier() {
        @Override
        public String getBootstrapServers() {
            return "localhost:9092";
        }

        @Override
        public String getTopicName() {
            return "page-visits";
        }
    };

    DataStoreWrapper() {
    }

    @PostConstruct
    void init() {
        try {

            Properties props = new Properties();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, configSupplier.getBootstrapServers());    // assuming that the Kafka broker this application is talking to runs on local machine with port 9092
            props.putIfAbsent(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
            props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
            props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, PageVisitSerde.class.getName());
            // For this we want to read all the data
            props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            final StreamsBuilder builder = new StreamsBuilder();
            KeyValueBytesStoreSupplier stateStore = Stores.inMemoryKeyValueStore("test-store");

            KTable<String, PageVisit> source = builder.table(
                    configSupplier.getTopicName(),
                    Materialized.<String, PageVisit>as(stateStore)
                            .withKeySerde(Serdes.String())
                            .withValueSerde(new PageVisitSerde()));
            final Topology topology = builder.build();
            this.streams = new KafkaStreams(topology, props);
            final CountDownLatch startLatch = new CountDownLatch(1);
            final AtomicReference<KafkaStreams.State> state = new AtomicReference<>();

            streams.setStateListener((newState, oldState) -> {
                state.set(newState);
                switch (newState) {
                    case RUNNING:
                    case ERROR:
                    case PENDING_SHUTDOWN:
                        startLatch.countDown();
                }
            });
            this.streams.start();
            startLatch.await(10, TimeUnit.SECONDS);
            System.out.println("Stream started");

            if (state.get() != KafkaStreams.State.RUNNING) {
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            if (this.streams != null) {
                this.streams.close();
            }
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> readLastVisitedPageByUsers() {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, PageVisit>> sqp = StoreQueryParameters.fromNameAndType("test-store", QueryableStoreTypes.keyValueStore());
        final ReadOnlyKeyValueStore<String, PageVisit> store = this.streams.store(sqp);

        Map<String, String> lastPageByUser = new HashMap<>();
        KeyValueIterator<String, PageVisit> it = store.all();
        it.forEachRemaining(keyValue -> lastPageByUser.put(keyValue.key, keyValue.value.getPage()));
        return lastPageByUser;
    }

    @PreDestroy
    public void close() {
        this.streams.close();
    }

}
