package org.wildfly.blog.kafka.streams.app;


import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.wildfly.blog.kafka.streams.DataStoreWrapper;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class StreamsEndpoint {
    @Inject
    DataStoreWrapper wrapper;

    @GET
    @Path("/last-visited")
    public Map<String, String> getLastVisited() {
        return wrapper.readLastVisitedPageByUsers();
    }
}
