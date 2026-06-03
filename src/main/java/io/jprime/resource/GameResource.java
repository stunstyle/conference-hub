package io.jprime.resource;

import io.jprime.model.BoothStamp;
import io.jprime.model.Sponsor;
import io.jprime.service.AuthService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/passport")
public class GameResource {

    private static final Logger LOG = Logger.getLogger(GameResource.class);

    @Inject
    Template passport;

    @Inject
    Template passport_container;

    @Inject
    AuthService authService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getPassport(@CookieParam("client_token") String clientToken) {
        String token = clientToken;
        NewCookie newCookie = null;
        
        if (token == null || token.trim().isEmpty()) {
            token = UUID.randomUUID().toString();
            newCookie = new NewCookie.Builder("client_token")
                    .value(token)
                    .path("/")
                    .maxAge(365 * 24 * 60 * 60)
                    .build();
        }

        List<Sponsor> sponsors = Sponsor.listAll();
        List<BoothStamp> stamps = BoothStamp.list("clientToken = ?1", token);
        List<Long> unlockedSponsorIds = stamps.stream().map(s -> s.sponsorId).toList();

        TemplateInstance instance = passport
                .data("activePage", "passport")
                .data("title", "Stamp Passport - JPrime 2026")
                .data("sponsors", sponsors)
                .data("unlockedIds", unlockedSponsorIds)
                .data("successMsg", null)
                .data("errorMsg", null)
                .data("currentUser", authService.getCurrentUser());

        Response.ResponseBuilder builder = Response.ok(instance);
        if (newCookie != null) {
            builder.cookie(newCookie);
        }
        return builder.build();
    }

    @POST
    @Path("/stamp")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public TemplateInstance submitStampCode(@FormParam("code") String code, @CookieParam("client_token") String clientToken) {
        String token = clientToken;
        if (token == null || token.trim().isEmpty()) {
            token = UUID.randomUUID().toString();
        }

        List<Sponsor> sponsors = Sponsor.listAll();
        List<BoothStamp> stamps = BoothStamp.list("clientToken = ?1", token);
        List<Long> unlockedIds = stamps.stream().map(s -> s.sponsorId).toList();

        if (code == null || code.trim().isEmpty()) {
            return passport_container
                    .data("sponsors", sponsors)
                    .data("unlockedIds", unlockedIds)
                    .data("successMsg", null)
                    .data("errorMsg", "Please enter a stamp code.");
        }

        Sponsor sponsor = Sponsor.find("lower(secretCode) = ?1", code.trim().toLowerCase()).firstResult();
        if (sponsor == null) {
            return passport_container
                    .data("sponsors", sponsors)
                    .data("unlockedIds", unlockedIds)
                    .data("successMsg", null)
                    .data("errorMsg", "Incorrect stamp code. Visit the sponsor's booth to get the code!");
        }

        if (unlockedIds.contains(sponsor.id)) {
            return passport_container
                    .data("sponsors", sponsors)
                    .data("unlockedIds", unlockedIds)
                    .data("successMsg", null)
                    .data("errorMsg", "You have already collected the stamp for " + sponsor.name + "!");
        }

        // Add stamp
        BoothStamp newStamp = new BoothStamp();
        newStamp.clientToken = token;
        newStamp.sponsorId = sponsor.id;
        newStamp.persist();

        // Refresh list
        stamps = BoothStamp.list("clientToken = ?1", token);
        unlockedIds = stamps.stream().map(s -> s.sponsorId).toList();

        return passport_container
                .data("sponsors", sponsors)
                .data("unlockedIds", unlockedIds)
                .data("successMsg", "Congratulations! You collected the stamp for " + sponsor.name + "! 🎉")
                .data("errorMsg", null);
    }
}
