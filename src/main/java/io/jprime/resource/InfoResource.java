package io.jprime.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.jprime.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/info")
public class InfoResource {

    @Inject
    Template info;

    @Inject
    AuthService authService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getInfo() {
        return info
                .data("activePage", "info")
                .data("title", "Conference Guide - JPrime 2026")
                .data("currentUser", authService.getCurrentUser());
    }
}
