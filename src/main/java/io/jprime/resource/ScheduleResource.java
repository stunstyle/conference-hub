package io.jprime.resource;

import io.jprime.model.*;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import io.jprime.service.AuthService;

@Path("/schedule")
public class ScheduleResource {

    private static final Logger LOG = Logger.getLogger(ScheduleResource.class);

    @Inject
    Template schedule;

    @Inject
    AuthService authService;

    @Inject
    Template schedule_list;

    @Inject
    Template session_detail;

    @Inject
    Template bookmark_btn;

    @Inject
    Template chat_list;

    @Inject
    Template qa_list;

    @Inject
    Template schedule_container;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getSchedule(@CookieParam("client_token") String clientToken) {
        String token = ensureToken(clientToken);
        
        // Default to Day 1
        java.time.LocalDate day1 = java.time.LocalDate.of(2026, 6, 3);
        List<Session> sessions = Session.list("cast(startTime as date) = ?1 order by startTime asc, hallName asc", day1);
        List<Bookmark> bookmarks = Bookmark.list("clientToken = ?1", token);
        List<Long> bookmarkedIds = bookmarks.stream().map(b -> b.sessionId).toList();

        java.util.TreeSet<java.time.LocalTime> allTimes = new java.util.TreeSet<>();
        for (Session s : sessions) {
            allTimes.add(s.startTime.toLocalTime());
            allTimes.add(s.endTime.toLocalTime());
        }
        List<java.time.LocalTime> timeList = new java.util.ArrayList<>(allTimes);

        List<SessionView> sessionViews = new java.util.ArrayList<>();
        for (Session s : sessions) {
            int rowStart = timeList.indexOf(s.startTime.toLocalTime()) + 1;
            int rowEnd = timeList.indexOf(s.endTime.toLocalTime()) + 1;
            int colStart = 2, colEnd = 5;
            if ("Hall A".equals(s.hallName)) { colStart = 2; colEnd = 3; }
            else if ("Hall B".equals(s.hallName)) { colStart = 3; colEnd = 4; }
            else if ("Workshops".equals(s.hallName)) { colStart = 4; colEnd = 5; }
            else {
                boolean overlapsWorkshop = false;
                for (Session other : sessions) {
                    if ("Workshops".equals(other.hallName)) {
                        if (other.startTime.isBefore(s.endTime) && other.endTime.isAfter(s.startTime)) {
                            overlapsWorkshop = true;
                            break;
                        }
                    }
                }
                colEnd = overlapsWorkshop ? 4 : 5;
            }
            sessionViews.add(new SessionView(s, rowStart, rowEnd, colStart, colEnd));
        }

        return schedule
                .data("activePage", "schedule")
                .data("title", "Agenda - JPrime 2026")
                .data("sessionViews", sessionViews)
                .data("timeList", timeList)
                .data("currentDay", 1)
                .data("bookmarkedIds", bookmarkedIds)
                .data("currentUser", authService.getCurrentUser());
    }

    @GET
    @Path("/day")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDaySchedule(@QueryParam("val") int day, @CookieParam("client_token") String clientToken) {
        String token = ensureToken(clientToken);
        
        List<Session> sessions;
        if (day == 0) {
            // Special view: fetch bookmarked sessions only
            List<Bookmark> bookmarks = Bookmark.list("clientToken = ?1", token);
            List<Long> bookmarkedIds = bookmarks.stream().map(b -> b.sessionId).toList();
            if (bookmarkedIds.isEmpty()) {
                sessions = java.util.Collections.emptyList();
            } else {
                sessions = Session.list("id in ?1 order by startTime asc, hallName asc", bookmarkedIds);
            }
        } else {
            java.time.LocalDate targetDate = (day == 1) ? java.time.LocalDate.of(2026, 6, 3) : java.time.LocalDate.of(2026, 6, 4);
            sessions = Session.list("cast(startTime as date) = ?1 order by startTime asc, hallName asc", targetDate);
        }
        
        List<Bookmark> bookmarks = Bookmark.list("clientToken = ?1", token);
        List<Long> bookmarkedIds = bookmarks.stream().map(b -> b.sessionId).toList();

        java.util.TreeSet<java.time.LocalTime> allTimes = new java.util.TreeSet<>();
        for (Session s : sessions) {
            allTimes.add(s.startTime.toLocalTime());
            allTimes.add(s.endTime.toLocalTime());
        }
        List<java.time.LocalTime> timeList = new java.util.ArrayList<>(allTimes);

        List<SessionView> sessionViews = new java.util.ArrayList<>();
        for (Session s : sessions) {
            int rowStart = timeList.indexOf(s.startTime.toLocalTime()) + 1;
            int rowEnd = timeList.indexOf(s.endTime.toLocalTime()) + 1;
            int colStart = 2, colEnd = 5;
            if ("Hall A".equals(s.hallName)) { colStart = 2; colEnd = 3; }
            else if ("Hall B".equals(s.hallName)) { colStart = 3; colEnd = 4; }
            else if ("Workshops".equals(s.hallName)) { colStart = 4; colEnd = 5; }
            else {
                boolean overlapsWorkshop = false;
                for (Session other : sessions) {
                    if ("Workshops".equals(other.hallName)) {
                        if (other.startTime.isBefore(s.endTime) && other.endTime.isAfter(s.startTime)) {
                            overlapsWorkshop = true;
                            break;
                        }
                    }
                }
                colEnd = overlapsWorkshop ? 4 : 5;
            }
            sessionViews.add(new SessionView(s, rowStart, rowEnd, colStart, colEnd));
        }

        return schedule_container
                .data("sessionViews", sessionViews)
                .data("timeList", timeList)
                .data("bookmarkedIds", bookmarkedIds)
                .data("currentDay", day)
                .data("currentUser", authService.getCurrentUser());
    }

    public static class SessionView {
        public Session session;
        public int rowStart;
        public int rowEnd;
        public int colStart;
        public int colEnd;
        
        public SessionView(Session session, int rowStart, int rowEnd, int colStart, int colEnd) {
            this.session = session;
            this.rowStart = rowStart;
            this.rowEnd = rowEnd;
            this.colStart = colStart;
            this.colEnd = colEnd;
        }
    }

    @POST
    @Path("/bookmark/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance addBookmark(@PathParam("id") Long id, @CookieParam("client_token") String clientToken) {
        String token = ensureToken(clientToken);
        
        Bookmark existing = Bookmark.find("sessionId = ?1 and clientToken = ?2", id, token).firstResult();
        if (existing == null) {
            Bookmark b = new Bookmark();
            b.sessionId = id;
            b.clientToken = token;
            b.persist();
        }

        return bookmark_btn
                .data("sessionId", id)
                .data("isBookmarked", true);
    }

    @DELETE
    @Path("/bookmark/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance removeBookmark(@PathParam("id") Long id, @CookieParam("client_token") String clientToken) {
        String token = ensureToken(clientToken);
        
        Bookmark.delete("sessionId = ?1 and clientToken = ?2", id, token);

        return bookmark_btn
                .data("sessionId", id)
                .data("isBookmarked", false);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getSessionDetail(@PathParam("id") Long id, @CookieParam("client_token") String clientToken) {
        String token = ensureToken(clientToken);
        
        Session sessionEntity = Session.findById(id);
        if (sessionEntity == null) {
            throw new NotFoundException("Session not found");
        }

        Bookmark bookmark = Bookmark.find("sessionId = ?1 and clientToken = ?2", id, token).firstResult();
        boolean isBookmarked = (bookmark != null);

        // Fetch chat messages
        List<ChatMessage> messages = ChatMessage.list("sessionId = ?1 order by timestamp asc", id);

        // Fetch questions, sorted by upvotes desc, then timestamp asc
        List<Question> questions = Question.list("sessionId = ?1 order by upvotes desc, timestamp asc", id);

        return session_detail
                .data("activePage", "schedule")
                .data("title", sessionEntity.title + " - JPrime 2026")
                .data("session", sessionEntity)
                .data("isBookmarked", isBookmarked)
                .data("messages", messages)
                .data("questions", questions)
                .data("clientToken", token)
                .data("currentUser", authService.getCurrentUser());
    }

    @POST
    @Path("/{id}/chat")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public TemplateInstance postChatMessage(@PathParam("id") Long id, 
                                            @FormParam("sender") String sender, 
                                            @FormParam("message") String message,
                                            @CookieParam("client_token") String clientToken) {
        String token = ensureToken(clientToken);
        
        ChatMessage msg = new ChatMessage();
        msg.sessionId = id;
        msg.sender = (sender == null || sender.trim().isEmpty()) ? "Anonymous" : sender;
        msg.message = message;
        msg.timestamp = LocalDateTime.now();
        msg.persist();

        List<ChatMessage> messages = ChatMessage.list("sessionId = ?1 order by timestamp asc", id);
        return chat_list.data("messages", messages).data("clientToken", token);
    }

    @GET
    @Path("/{id}/chat")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getChatList(@PathParam("id") Long id, @CookieParam("client_token") String clientToken) {
        String token = ensureToken(clientToken);
        List<ChatMessage> messages = ChatMessage.list("sessionId = ?1 order by timestamp asc", id);
        return chat_list.data("messages", messages).data("clientToken", token);
    }

    @POST
    @Path("/{id}/qa")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public TemplateInstance postQuestion(@PathParam("id") Long id, 
                                         @FormParam("sender") String sender, 
                                         @FormParam("questionText") String text) {
        Question q = new Question();
        q.sessionId = id;
        q.sender = (sender == null || sender.trim().isEmpty()) ? "Anonymous" : sender;
        q.questionText = text;
        q.upvotes = 0;
        q.timestamp = LocalDateTime.now();
        q.persist();

        List<Question> questions = Question.list("sessionId = ?1 order by upvotes desc, timestamp asc", id);
        return qa_list.data("questions", questions);
    }

    @POST
    @Path("/qa/{questionId}/upvote")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance upvoteQuestion(@PathParam("questionId") Long questionId) {
        Question q = Question.findById(questionId);
        if (q != null) {
            q.upvotes += 1;
            q.persist();
        }

        List<Question> questions = Question.list("sessionId = ?1 order by upvotes desc, timestamp asc", q.sessionId);
        return qa_list.data("questions", questions);
    }

    @GET
    @Path("/{id}/qa")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getQaList(@PathParam("id") Long id) {
        List<Question> questions = Question.list("sessionId = ?1 order by upvotes desc, timestamp asc", id);
        return qa_list.data("questions", questions);
    }

    @POST
    @Path("/{id}/feedback")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response submitFeedback(@PathParam("id") Long id, 
                                   @FormParam("rating") int rating, 
                                   @FormParam("comment") String comment) {
        Feedback f = new Feedback();
        f.sessionId = id;
        f.rating = rating;
        f.comment = comment;
        f.persist();

        // Return a simple thank you message that swaps into the feedback card
        String html = "<div style='text-align:center; padding:10px; color:var(--neon-green); font-weight:600;'>" +
                      "<i class='fa-solid fa-circle-check'></i> Thank you for your feedback!</div>";
        return Response.ok(html).build();
    }

    private String ensureToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return token;
    }
}
