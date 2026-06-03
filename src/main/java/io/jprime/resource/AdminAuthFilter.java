package io.jprime.resource;

import io.jprime.model.Attendee;
import io.jprime.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.net.URI;

@Provider
public class AdminAuthFilter implements ContainerRequestFilter {

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("/admin")) {
            Attendee currentUser = authService.getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin) {
                requestContext.abortWith(Response.seeOther(URI.create("/")).build());
            }
        }
    }
}
