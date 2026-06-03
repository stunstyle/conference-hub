package io.jprime.service;

import io.jprime.model.Attendee;
import io.jprime.model.Speaker;
import io.jprime.model.Session;
import io.jprime.model.Sponsor;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
public class DatabaseBootstrap {

    private static final Logger LOG = Logger.getLogger(DatabaseBootstrap.class);

    @Inject
    @RestClient
    JPrimeClient jprimeClient;

    @Transactional
    public void init(@Observes StartupEvent ev) {
        if (Speaker.count() == 0) {
            LOG.info("Database is empty. Initializing data...");
            try {
                bootstrapFromApi();
            } catch (Exception e) {
                LOG.warn("Failed to bootstrap from API (offline?). Falling back to mock data.", e);
                bootstrapMockData();
            }
        }
    }

    private void bootstrapFromApi() {
        LOG.info("Fetching speakers from API...");
        try {
            List<SpeakerDTO> speakers = jprimeClient.getSpeakers();
            for (SpeakerDTO dto : speakers) {
                Speaker speaker = new Speaker();
                speaker.firstName = dto.firstName;
                speaker.lastName = dto.lastName;
                speaker.company = dto.company;
                speaker.bio = dto.bio;
                speaker.twitter = dto.twitter;
                speaker.email = dto.email;
                speaker.persist();
            }
        } catch (Exception e) {
            LOG.warn("Failed to fetch speakers from API (endpoint protected or offline). Will use pre-seeded speakers.");
            seedRealSpeakers();
        }

        LOG.info("Fetching sessions by hall...");
        String[] halls = {"Hall A", "Hall B", "Workshops"};
        int totalSessions = 0;
        for (String hall : halls) {
            try {
                List<SessionDTO> sessions = jprimeClient.getSessionsByHall(hall);
                for (SessionDTO dto : sessions) {
                    Session session = new Session();
                    session.title = dto.title;
                    session.description = dto.talkDescription;
                    boolean isGlobal = false;
                    if (dto.lectorName == null || dto.lectorName.trim().isEmpty()) {
                        isGlobal = true;
                    }
                    // Some global events might have Nayden as a speaker (Opening/Closing) but they apply globally
                    if (dto.title != null && (dto.title.contains("Opening") || dto.title.contains("Closing") || dto.title.contains("Raffle"))) {
                        isGlobal = true;
                    }

                    if (isGlobal) {
                        session.hallName = null;
                        // Deduplicate global events so they don't appear 3 times
                        long count = Session.count("title = ?1 and startTime = ?2", dto.title, dto.startTime);
                        if (count > 0) {
                            continue;
                        }
                    } else {
                        session.hallName = hall;
                    }

                    session.startTime = dto.startTime;
                    session.endTime = dto.endTime;

                    // Try to match speaker or dynamically create
                    if (!isGlobal || (dto.lectorName != null && !dto.lectorName.trim().isEmpty())) {
                        if (dto.lectorName != null && !dto.lectorName.trim().isEmpty()) {
                            session.speaker = getOrCreateSpeaker(dto.lectorName);
                        }
                        if (dto.coLectorName != null && !dto.coLectorName.trim().isEmpty()) {
                            session.coSpeaker = getOrCreateSpeaker(dto.coLectorName);
                        }
                    }

                    session.persist();
                    totalSessions++;
                }
            } catch (Exception e) {
                LOG.warn("Failed to fetch sessions for " + hall, e);
            }
        }

        if (totalSessions == 0) {
            throw new RuntimeException("0 sessions fetched from API. Failing over to mock data.");
        }

        bootstrapSponsorsAndAttendees();
    }

    private void seedRealSpeakers() {
        LOG.info("Seeding real speaker profiles...");
        createSpeaker("Gerrit", "Grunwald", "Java Lover", "Expert speaker at JPrime 2026", "hansolo_", "gerrit@example.com", "/image/speaker/5");
        createSpeaker("Milen", "Dyankov", "The Vector Whisperer", "Expert speaker at JPrime 2026", "milendyankov", "milen@example.com", "/image/speaker/9");
        createSpeaker("Victor", "Rentea", "The Training Guy", "Expert speaker at JPrime 2026", "victorrentea", "victor@example.com", "/image/speaker/122");
        createSpeaker("Roberto", "Cortez", "The Coimbra Drummer", "Expert speaker at JPrime 2026", "radcortez", "roberto@example.com", "/image/speaker/139");
        createSpeaker("Ioannis", "Kolaxis", "Reinventing software engineering", "Expert speaker at JPrime 2026", "IoannisKolaxis", "ioannis@example.com", "/image/speaker/326");
        createSpeaker("Jonathan", "Vila López", "A community advocate", "Expert speaker at JPrime 2026", "vilojona", "jonathan@example.com", "/image/speaker/547");
        createSpeaker("Venkat", "Subramaniam", "Programmer and Author", "Expert speaker at JPrime 2026", "venkat_s", "venkat@example.com", "/image/speaker/591");
        createSpeaker("Marit", "van Dijk", "Developer Advocate at JetBrains", "Expert speaker at JPrime 2026", "MaritvanDijk77", "marit@example.com", "/image/speaker/603");
        createSpeaker("Stefan", "Angelov", "The Java guy from pLoVEdiv", "Expert speaker at JPrime 2026", "cefothe", "stefan@example.com", "/image/speaker/610");
        createSpeaker("Ivan", "Yonkov", "Javarchaeologist", "Expert speaker at JPrime 2026", null, "ivan@example.com", "/image/speaker/631");
        createSpeaker("Cay", "Horstmann", "The very same CAY HORSTMANN", "Expert speaker at JPrime 2026", "cayhorstmann", "cay@example.com", "/image/speaker/677");
        createSpeaker("Piotr", "Przybył", "Senior Developer Advocate", "Expert speaker at JPrime 2026", "piotrprz", "piotr@example.com", "/image/speaker/678");
        createSpeaker("Johannes", "Bechberger", "OpenJDK Hacker and Tools Developer", "Expert speaker at JPrime 2026", "parttimen3rd", "johannes@example.com", "/image/speaker/686");
        createSpeaker("Hinse", "ter Schuur", "Trade-off Juggler", "Expert speaker at JPrime 2026", "coduinix", "hinse@example.com", "/image/speaker/691");
        createSpeaker("Christian", "Heitzmann", "The Not-So-Neutral Swiss Guy", "Expert speaker at JPrime 2026", null, "christian@example.com", "/image/speaker/733");
        createSpeaker("Willem Jan", "Glerum", "Quarkus Giant", "Expert speaker at JPrime 2026", "wjglerum", "willem@example.com", "/image/speaker/747");
        createSpeaker("Marcin", "Chrost", "A humble developer", "Expert speaker at JPrime 2026", null, "marcin@example.com", "/image/speaker/770");
        createSpeaker("Vadym", "Kazulkin", "AWS Serverless geek", "Expert speaker at JPrime 2026", "VKazulkin", "vadym@example.com", "/image/speaker/772");
        createSpeaker("Emanuel", "Trandafir", "Baeldung Author & Rock Climber", "Expert speaker at JPrime 2026", null, "emanuel@example.com", "/image/speaker/775");
        createSpeaker("Arnaud", "Jean", "The Witcherish", "Expert speaker at JPrime 2026", "thewitcherish", "arnaud@example.com", "/image/speaker/777");
        createSpeaker("Panche", "Chavkovski", "JUGMK Helmsman", "Expert speaker at JPrime 2026", null, "panche@example.com", "/image/speaker/786");
        createSpeaker("Kristiyan", "Stoyanov", "Multi-Agent Troublemaker", "Expert speaker at JPrime 2026", null, "kristiyan@example.com", "/image/speaker/789");
        createSpeaker("Lyubomir", "Bozhinov", "Technology Leader", "Expert speaker at JPrime 2026", null, "lyubomir@example.com", "/image/speaker/792");
        createSpeaker("Viktoriya", "Kutsarova", "Redis Ecosystem Advocate", "Expert speaker at JPrime 2026", null, "viktoriya@example.com", "/image/speaker/794");
        createSpeaker("Kevin", "Dubois", "Sr Principal AI Wrangler", "Expert speaker at JPrime 2026", null, "kevin@example.com", "/image/speaker/799");
        createSpeaker("Jordan", "Jovkov", "Old School Java Guy, AI Geek", "Expert speaker at JPrime 2026", null, "jordan@example.com", "/image/speaker/801");
        createSpeaker("François", "Martin", "The Hallway Track Engineer", "Expert speaker at JPrime 2026", "fmartin_", "françois@example.com", "/image/speaker/804");
        createSpeaker("Thanos", "Stratikopoulos", "A Java GPU Alchemist", "Expert speaker at JPrime 2026", "thanos_str", "thanos@example.com", "/image/speaker/806");
        createSpeaker("Sergi", "Almar", "Spring Sensei", "Expert speaker at JPrime 2026", "sergialmar", "sergi@example.com", "/image/speaker/807");
        createSpeaker("Nayden", "Gochev", "+359 887 749 325", "Expert speaker at JPrime 2026", "gochev", "nayden@example.com", "/image/speaker/810");
    }

    private Speaker getOrCreateSpeaker(String fullName) {
        Speaker s = findSpeakerByName(fullName);
        if (s == null) {
            s = new Speaker();
            String[] parts = fullName.split(" ", 2);
            if (parts.length == 2) {
                s.firstName = parts[0];
                s.lastName = parts[1];
            } else {
                s.firstName = fullName;
                s.lastName = "";
            }
            s.company = "JPrime Speaker";
            s.bio = "Speaker at JPrime";
            s.persist();
        }
        return s;
    }

    private Speaker findSpeakerByName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        if (parts.length == 2) {
            return Speaker.find("firstName = ?1 and lastName = ?2", parts[0], parts[1]).firstResult();
        } else {
            return Speaker.find("firstName = ?1 or lastName = ?1", fullName).firstResult();
        }
    }

    private void bootstrapMockData() {
        LOG.info("Bootstrapping mock speakers...");
        
        Speaker willem = createSpeaker("Willem Jan", "Glerum", "NL-JUG", 
            "Willem Jan Glerum is a software engineer and Java Developer Advocate. He is a frequent speaker at JPrime, discussing Quarkus, JVM security, and concurrency.", "wjglerum", "willem@example.com", "/image/speaker/747");
        
        Speaker francois = createSpeaker("François", "Martin", "Swiss JVM Performance Group", 
            "François Martin is a JVM performance engineer, benchmark hacker, and contributor to several JVM concurrency libraries.", "fmartin", "francois@example.com", "/image/speaker/804");
            
        Speaker heinz = createSpeaker("Heinz", "Kabutz", "JavaSpecialists.eu", 
            "Dr. Heinz Kabutz is the author of The Java Specialists' Newsletter, read by Java developers in over 145 countries.", "heinzkabutz", "heinz@example.com", null);
            
        Speaker nayden = createSpeaker("Nayden", "Gochev", "Bulgarian Java User Group", 
            "Nayden is an organizer of the Bulgarian JUG and jPrime conference. He is a tech lead and senior JVM developer.", "gochev", "nayden@example.com", "/image/speaker/810");

        LOG.info("Bootstrapping mock sessions...");
        LocalDate day1 = LocalDate.of(2026, 6, 3);
        LocalDate day2 = LocalDate.of(2026, 6, 4);

        // Day 1
        createSession("Registration & Coffee", "Pick up your badges, bags of surprises, and enjoy first breakfast.", null, 
            day1.atTime(8, 0), day1.atTime(9, 0), null, null);
            
        createSession("Opening Ceremony", "Welcome to jPrime 2026! Kick off the conference and hear the hackathon details.", "Hall A", 
            day1.atTime(9, 0), day1.atTime(9, 20), nayden, null);

        createSession("Practical MCP Security in Action", "A deep dive into model context protocol security, secure integrations, and oauth2 protection patterns.", "Hall A", 
            day1.atTime(10, 0), day1.atTime(10, 50), willem, null);

        createSession("Deep Dive into Java Memory Model", "An advanced look at how fields are accessed across threads under JVM specs.", "Hall B", 
            day1.atTime(10, 0), day1.atTime(10, 50), heinz, null);

        createSession("Workshop: Advanced Concurrency in Practice", "Bring your laptops for a practical workshop coding with structured concurrency APIs.", "Workshops", 
            day1.atTime(11, 0), day1.atTime(12, 30), heinz, nayden);

        // Day 2 (Current Date)
        createSession("Coffee & Breakfast", "Start Day 2 with catering and networking.", null, 
            day2.atTime(8, 0), day2.atTime(9, 0), null, null);

        createSession("Concurrency Crossroads: Reactive vs Virtual Threads", "Choosing between reactive programming and loom fibers inside a Quarkus runtime context.", "Hall A", 
            day2.atTime(9, 0), day2.atTime(9, 50), willem, null);

        createSession("My code is faster than yours... let me prove it!", "A live performance benchmarking battle showing micro-optimizations, JMH tips, and JVM compilers internals.", "Hall B", 
            day2.atTime(9, 0), day2.atTime(9, 50), francois, null);

        createSession("Hackathon Final Submissions & Demos", "Showcase your companion app hacks to the jury! 1000 euros is on the line.", "Workshops", 
            day2.atTime(10, 10), day2.atTime(12, 0), nayden, null);

        createSession("Closing Ceremony & 1000-Euro Raffle", "Closing remarks, announcing hackathon winners, and sponsor prize drawings.", "Hall A", 
            day2.atTime(16, 0), day2.atTime(16, 45), nayden, null);

        bootstrapSponsorsAndAttendees();
    }

    private Speaker createSpeaker(String first, String last, String company, String bio, String twitter, String email, String pictureUrl) {
        Speaker s = new Speaker();
        s.firstName = first;
        s.lastName = last;
        s.company = company;
        s.bio = bio;
        s.twitter = twitter;
        s.email = email;
        if (pictureUrl != null && !pictureUrl.startsWith("http")) {
            s.pictureUrl = "https://jprime.io" + pictureUrl;
        } else {
            s.pictureUrl = pictureUrl;
        }
        s.persist();
        return s;
    }

    private void createSession(String title, String desc, String hall, LocalDateTime start, LocalDateTime end, Speaker main, Speaker co) {
        Session s = new Session();
        s.title = title;
        s.description = desc;
        s.hallName = hall;
        s.startTime = start;
        s.endTime = end;
        s.speaker = main;
        s.coSpeaker = co;
        s.persist();
    }

    private void bootstrapSponsorsAndAttendees() {
        LOG.info("Bootstrapping sponsors...");
        createSponsor("Tide", "Gold", "tide.png", "TIDE2026");
        createSponsor("GraphWise", "Gold", "graphwise.png", "GW2026");
        createSponsor("Delasport", "Gold", "delasport.png", "DS2026");
        createSponsor("EGT Digital", "Silver", "egt.png", "EGT2026");
        createSponsor("Coherent Solutions", "Silver", "coherent.png", "CS2026");

        LOG.info("Bootstrapping attendees...");
        createAttendee("ivan@jprime.io", "Ivan", "Ivanov", "Tide", "JP26-IVAN", false);
        createAttendee("georgi@jprime.io", "Georgi", "Georgiev", "GraphWise", "JP26-GEORG", false);
        createAttendee("maria@jprime.io", "Maria", "Petrova", "Coherent Solutions", "JP26-MARIA", false);
        createAttendee("dimitar@jprime.io", "Dimitar", "Dimitrov", "Delasport", "JP26-DIMIT", false);
        createAttendee("elena@jprime.io", "Elena", "Vasileva", "EGT Digital", "JP26-ELENA", false);
        createAttendee("gocata@jprime.io", "Gocata", "Dev", "Hackathon Team", "JP26-HACK", false);
        createAttendee("nayden@jprime.io", "Nayden", "Gochev", "Bulgarian JUG", "JP26-ADMIN", true);
    }

    private void createSponsor(String name, String level, String logo, String code) {
        Sponsor s = new Sponsor();
        s.name = name;
        s.level = level;
        s.logoFileName = logo;
        s.secretCode = code;
        s.persist();
    }

    private void createAttendee(String email, String first, String last, String company, String ticket, boolean isAdmin) {
        Attendee a = new Attendee();
        a.email = email;
        a.firstName = first;
        a.lastName = last;
        a.company = company;
        a.ticketCode = ticket;
        a.checkedIn = false;
        a.isAdmin = isAdmin;
        a.persist();
    }
}
