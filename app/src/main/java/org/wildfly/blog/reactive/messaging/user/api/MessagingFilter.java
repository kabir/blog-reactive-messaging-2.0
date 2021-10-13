package org.wildfly.blog.reactive.messaging.user.api;

import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.wildfly.blog.reactive.messaging.common.PageVisit;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
@WebFilter(value = "/*")
public class MessagingFilter extends HttpFilter {
    @Inject
    @Channel("from-filter")
    Emitter<PageVisit> messagingEmitter;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String user = getUsername(request);
        String address = request.getRemoteAddr();
        String page = Paths.get(request.getRequestURI()).getFileName().toString();


        PageVisit pv = new PageVisit(user, address, page);
        messagingEmitter.send(pv);

        // Disable caching for the html pages
        ((HttpServletResponse)response).addHeader("Cache-control", "no-store");
        ((HttpServletResponse)response).addHeader("Pragma", "no-cache");

        filterChain.doFilter(request, response);
    }

    private String getUsername(HttpServletRequest servletRequest) {
        // Pretend we're looking up the authenticated user
        switch ((int)Math.round(Math.random() * 3)) {
            case 0:
                return "bob";
            case 1:
                return "emma";
            case 2:
                return "frank";
            case 3:
                return "linda";
        }
        return null;
    }
}
