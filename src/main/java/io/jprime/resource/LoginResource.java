package io.jprime.resource;

import io.jprime.model.Attendee;
import io.jprime.model.Bookmark;
import io.jprime.service.AuthService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/login")
public class LoginResource {

    @Inject
    Template login;

    @Inject
    AuthService authService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLogin() {
        return login.data("activePage", "login")
                    .data("title", "Login - JPrime 2026")
                    .data("error", null)
                    .data("currentUser", authService.getCurrentUser());
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response doLogin(@FormParam("email") String email,
                            @FormParam("ticketCode") String ticketCode,
                            @CookieParam("client_token") String existingToken) {
        
        Attendee attendee = Attendee.find("email = ?1 and ticketCode = ?2", email, ticketCode).firstResult();
        
        if (attendee == null) {
            return Response.ok(login.data("activePage", "login")
                                    .data("title", "Login - JPrime 2026")
                                    .data("error", "Invalid Email or Ticket Code. Please check your registration email.")
                                    .data("currentUser", authService.getCurrentUser()))
                           .build();
        }

        // Generate new session token
        String newToken = UUID.randomUUID().toString();
        attendee.currentSessionToken = newToken;
        attendee.persist();

        // Migrate any anonymous bookmarks to the new token if they existed
        if (existingToken != null && !existingToken.trim().isEmpty()) {
            Bookmark.update("clientToken = ?1 where clientToken = ?2", newToken, existingToken);
        }

        NewCookie authCookie = new NewCookie.Builder("client_token")
                .value(newToken)
                .path("/")
                .maxAge(365 * 24 * 60 * 60) // 1 year
                .build();

        return Response.seeOther(URI.create("/"))
                       .cookie(authCookie)
                       .build();
    }
    
    @GET
    @Path("/logout")
    @Transactional
    public Response doLogout(@CookieParam("client_token") String clientToken) {
        if (clientToken != null) {
            Attendee attendee = Attendee.find("currentSessionToken", clientToken).firstResult();
            if (attendee != null) {
                attendee.currentSessionToken = null;
                attendee.persist();
            }
        }
        
        NewCookie deleteCookie = new NewCookie.Builder("client_token")
                .value("")
                .path("/")
                .maxAge(0) // delete cookie
                .build();
                
        return Response.seeOther(URI.create("/"))
                       .cookie(deleteCookie)
                       .build();
    }
}
