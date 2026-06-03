package io.jprime.resource;

import io.jprime.model.Attendee;
import io.jprime.service.AuthService;
import io.jprime.service.QrCodeService;
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

@Path("/badge")
public class BadgeResource {

    private static final Logger LOG = Logger.getLogger(BadgeResource.class);
    private static final String CLOSING_CODE = "JPRIME2026"; // Code shown on stage

    @Inject
    Template badge;

    @Inject
    QrCodeService qrCodeService;

    @Inject
    AuthService authService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getBadge() {
        Attendee attendee = authService.getCurrentUser();
        
        if (attendee == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(attendee.ticketCode, 200, 200);

        TemplateInstance instance = badge
                .data("activePage", "badge")
                .data("title", "My Ticket Badge - JPrime 2026")
                .data("attendee", attendee)
                .data("qrCode", qrCodeBase64)
                .data("errorMsg", null)
                .data("currentUser", attendee);
                
        return Response.ok(instance).build();
    }

    @POST
    @Path("/checkin")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response checkInRaffle(@FormParam("stageCode") String stageCode) {
        Attendee attendee = authService.getCurrentUser();
        if (attendee == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        if (CLOSING_CODE.equalsIgnoreCase(stageCode.trim())) {
            attendee.checkedIn = true;
            attendee.persist();
        } else {
            // Invalid code
            String qrCodeBase64 = qrCodeService.generateQrCodeBase64(attendee.ticketCode, 200, 200);
            TemplateInstance errInstance = badge
                    .data("activePage", "badge")
                    .data("title", "My Ticket Badge - JPrime 2026")
                    .data("attendee", attendee)
                    .data("qrCode", qrCodeBase64)
                    .data("errorMsg", "Invalid Stage Check-in Code. Please double check the screen!")
                    .data("currentUser", attendee);
            return Response.ok(errInstance).build();
        }

        return Response.seeOther(URI.create("/badge")).build();
    }
}
