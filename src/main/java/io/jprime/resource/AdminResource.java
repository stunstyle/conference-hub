package io.jprime.resource;

import io.jprime.model.Attendee;
import io.jprime.model.Session;
import io.jprime.model.Speaker;
import io.jprime.service.AuthService;
import io.jprime.model.Sponsor;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Path("/admin")
public class AdminResource {

    private static final Logger LOG = Logger.getLogger(AdminResource.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Inject
    Template admin;

    @Inject
    Template admin_raffle;

    @Inject
    AuthService authService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAdminDashboard() {
        List<Speaker> speakers = Speaker.listAll();
        List<Session> sessions = Session.listAll();
        List<Sponsor> sponsors = Sponsor.listAll();
        long checkedInCount = Attendee.count("checkedIn = true");
        long totalAttendees = Attendee.count();

        return admin
                .data("activePage", "admin")
                .data("title", "Admin Control Panel - JPrime 2026")
                .data("speakers", speakers)
                .data("sessions", sessions)
                .data("sponsors", sponsors)
                .data("checkedInCount", checkedInCount)
                .data("totalAttendees", totalAttendees)
                .data("currentUser", authService.getCurrentUser());
    }

    // SPEAKER CRUD
    @POST
    @Path("/speakers")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response saveSpeaker(@FormParam("id") Long id,
                                @FormParam("firstName") String firstName,
                                @FormParam("lastName") String lastName,
                                @FormParam("company") String company,
                                @FormParam("bio") String bio,
                                @FormParam("twitter") String twitter,
                                @FormParam("email") String email) {
        Speaker speaker;
        if (id != null) {
            speaker = Speaker.findById(id);
        } else {
            speaker = new Speaker();
        }

        speaker.firstName = firstName;
        speaker.lastName = lastName;
        speaker.company = company;
        speaker.bio = bio;
        speaker.twitter = twitter;
        speaker.email = email;
        speaker.persist();

        return Response.seeOther(URI.create("/admin")).build();
    }

    @GET
    @Path("/speakers/{id}/delete")
    @Transactional
    public Response deleteSpeaker(@PathParam("id") Long id) {
        Speaker speaker = Speaker.findById(id);
        if (speaker != null) {
            // Unlink from sessions first
            Session.update("speaker = null where speaker = ?1", speaker);
            Session.update("coSpeaker = null where coSpeaker = ?1", speaker);
            speaker.delete();
        }
        return Response.seeOther(URI.create("/admin")).build();
    }

    // SESSION CRUD
    @POST
    @Path("/sessions")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response saveSession(@FormParam("id") Long id,
                                @FormParam("title") String title,
                                @FormParam("description") String description,
                                @FormParam("hallName") String hallName,
                                @FormParam("startTime") String startTimeStr,
                                @FormParam("endTime") String endTimeStr,
                                @FormParam("speakerId") Long speakerId,
                                @FormParam("coSpeakerId") Long coSpeakerId) {
        Session session;
        if (id != null) {
            session = Session.findById(id);
        } else {
            session = new Session();
        }

        session.title = title;
        session.description = description;
        session.hallName = (hallName == null || hallName.trim().isEmpty()) ? null : hallName;
        
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            session.startTime = LocalDateTime.parse(startTimeStr, FORMATTER);
        }
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            session.endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
        }

        if (speakerId != null) {
            session.speaker = Speaker.findById(speakerId);
        } else {
            session.speaker = null;
        }

        if (coSpeakerId != null) {
            session.coSpeaker = Speaker.findById(coSpeakerId);
        } else {
            session.coSpeaker = null;
        }

        session.persist();
        return Response.seeOther(URI.create("/admin")).build();
    }

    @GET
    @Path("/sessions/{id}/delete")
    @Transactional
    public Response deleteSession(@PathParam("id") Long id) {
        Session session = Session.findById(id);
        if (session != null) {
            session.delete();
        }
        return Response.seeOther(URI.create("/admin")).build();
    }

    // SPONSOR CRUD
    @POST
    @Path("/sponsors")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response saveSponsor(@RestForm("id") Long id,
                                @RestForm("name") String name,
                                @RestForm("level") String level,
                                @RestForm("secretCode") String secretCode,
                                @RestForm("logoFile") FileUpload logoFile) {
        Sponsor sponsor;
        if (id != null) {
            sponsor = Sponsor.findById(id);
        } else {
            sponsor = new Sponsor();
        }

        sponsor.name = name;
        sponsor.level = level;
        sponsor.secretCode = secretCode;

        if (logoFile != null && logoFile.fileName() != null && !logoFile.fileName().trim().isEmpty()) {
            sponsor.logoFileName = logoFile.fileName();
        } else if (sponsor.logoFileName == null) {
            sponsor.logoFileName = "default.png";
        }

        sponsor.persist();
        return Response.seeOther(URI.create("/admin")).build();
    }

    @GET
    @Path("/sponsors/{id}/delete")
    @Transactional
    public Response deleteSponsor(@PathParam("id") Long id) {
        Sponsor sponsor = Sponsor.findById(id);
        if (sponsor != null) {
            sponsor.delete();
        }
        return Response.seeOther(URI.create("/admin")).build();
    }

    // RAFFLE CONTROL
    @GET
    @Path("/raffle")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getRaffleView() {
        long checkedInCount = Attendee.count("checkedIn = true");
        return admin_raffle
                .data("activePage", "admin")
                .data("title", "Raffle Draw Stage - JPrime 2026")
                .data("checkedInCount", checkedInCount)
                .data("currentUser", authService.getCurrentUser());
    }

    @GET
    @Path("/raffle/draw-step")
    @Produces(MediaType.TEXT_HTML)
    public String drawStep(@QueryParam("step") int step) {
        // Draw prioritizing checked-in attendees
        List<Attendee> list = Attendee.list("checkedIn = true");
        if (list.isEmpty()) {
            // Fallback to all attendees so the demo draws names even if no one checked in.
            list = Attendee.listAll();
        }

        if (list.isEmpty()) {
            return "<div class='raffle-name' style='color:var(--neon-pink);'>No attendees found in DB!</div>";
        }

        if (step < 30) {
            // Rapid rolling name phase
            int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
            Attendee randomAttendee = list.get(randomIndex);

            return "<div id='raffle-box' class='raffle-roll-container' hx-get='/admin/raffle/draw-step?step=" + (step + 1) + "' hx-trigger='every 100ms' hx-swap='outerHTML'>" +
                   "  <div class='raffle-name'>" + randomAttendee.getFullName() + "</div>" +
                   "</div>";
        } else {
            // Secure final draw phase
            SecureRandom secureRandom = new SecureRandom();
            int winnerIndex = secureRandom.nextInt(list.size());
            Attendee winner = list.get(winnerIndex);

            return "<div id='raffle-box' class='winner-overlay'>" +
                   "  <div style='font-size: 1rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 700; margin-bottom: 8px;'>🎉 WINNER DRAWN 🎉</div>" +
                   "  <div class='raffle-winner'>" + winner.getFullName() + "</div>" +
                   "  <div style='color: var(--neon-blue); font-weight: 600; margin-top: 10px; font-size: 1.1rem;'>" + winner.company + "</div>" +
                   "  <div style='font-size: 0.8rem; color: var(--text-secondary); margin-top: 6px;'>TICKET: " + winner.ticketCode + "</div>" +
                   "  <button hx-get='/admin/raffle/draw-step?step=1' hx-target='#raffle-box' hx-swap='outerHTML' class='btn' style='background: var(--secondary-grad); margin-top: 20px;'>Draw Another</button>" +
                   "</div>";
        }
    }
}
