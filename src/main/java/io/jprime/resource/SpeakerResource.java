package io.jprime.resource;

import io.jprime.model.Speaker;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.jprime.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/speakers")
public class SpeakerResource {

    @Inject
    Template speakers;

    @Inject
    AuthService authService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getSpeakers() {
        List<Speaker> speakerList = Speaker.list("order by firstName asc");
        return speakers
                .data("activePage", "speakers")
                .data("title", "Speakers - JPrime 2026")
                .data("speakers", speakerList)
                .data("currentUser", authService.getCurrentUser());
    }
}
