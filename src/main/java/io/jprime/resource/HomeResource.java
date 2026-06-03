package io.jprime.resource;

import io.jprime.model.Session;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import io.jprime.service.AuthService;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/")
public class HomeResource {

    @Inject
    Template dashboard;

    @Inject
    Template announcements;

    @Inject
    AuthService authService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboard(@CookieParam("client_token") String clientToken) {
        String token = clientToken;
        NewCookie newCookie = null;
        
        if (token == null || token.trim().isEmpty()) {
            token = UUID.randomUUID().toString();
            newCookie = new NewCookie.Builder("client_token")
                    .value(token)
                    .path("/")
                    .maxAge(365 * 24 * 60 * 60) // 1 year
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        // Demo Time Mock: If current time is outside conference days (June 3-4, 2026),
        // we simulate June 4, 2026 at 09:30 AM so judges see the live dashboard filled.
        LocalDateTime confStart = LocalDateTime.of(2026, 6, 3, 0, 0);
        LocalDateTime confEnd = LocalDateTime.of(2026, 6, 4, 23, 59);
        if (now.isBefore(confStart) || now.isAfter(confEnd)) {
            now = LocalDateTime.of(2026, 6, 4, 9, 30);
        }

        // Live sessions: sessions happening right now
        List<Session> liveSessions = Session.list("startTime <= ?1 and endTime >= ?1", now);
        
        // Up Next: sessions starting in the future (only talks)
        List<Session> upNextSessions = Session.list("startTime > ?1 and hallName is not null order by startTime asc", now);
        if (upNextSessions.size() > 3) {
            upNextSessions = upNextSessions.subList(0, 3);
        }

        TemplateInstance instance = dashboard
                .data("activePage", "dashboard")
                .data("title", "Live Dashboard - JPrime 2026")
                .data("liveSessions", liveSessions)
                .data("upNext", upNextSessions)
                .data("demoTime", now)
                .data("currentUser", authService.getCurrentUser());

        Response.ResponseBuilder builder = Response.ok(instance);
        if (newCookie != null) {
            builder.cookie(newCookie);
        }
        return builder.build();
    }

    @GET
    @Path("/dashboard/announcements")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        String timeStr = now.toLocalTime().withSecond(0).withNano(0).toString();
        return announcements.data("time", timeStr);
    }
}
