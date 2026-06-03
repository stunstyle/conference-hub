# JPrime 2026 Conference Hub 🏆

A premium, all-in-one conference companion web application built for the **JPrime 2026 Hackathon**. 

This application provides everything attendees, sponsors, and organizers need to manage, explore, and interact with the conference in real time.

---

## 🚀 Key Features

1. **Live Dashboard ("Happening Now" & "Up Next"):** Displays live talks, hall schedules, local caterings, and WiFi info with fallbacks for off-hours testing.
2. **Interactive Agenda (HTMX-driven):** Timeline layouts that allow users to filter by Hall (Hall A, Hall B, Workshops) and customize a personal **My Schedule** folder.
3. **Ticket Finder & Digital Badge:** Allows users to find their registration ticket by email and generates an inline check-in QR code dynamically using ZXing (no external API calls).
4. **Sponsor Passport Stamp Game:** Attendees visit sponsor booths, input validation codes (e.g. `TIDE2026`, `GW2026`), and unlock badges on a digital stamp sheet. Unlocking all badges enters them in the closing draw.
5. **Real-time Session Chat & Q&A Board:** Dynamic shared chat walls and Q&A boards where attendees can post and upvote questions. Employs 3s HTMX short-polling (no custom JS/TS).
6. **Physical Raffle & Animated Juggler Screen:** Big-screen raffle stage that rolls through attendee names in a slot-machine roll simulation, selecting the winner via secure cryptographical generation (`java.security.SecureRandom`).
7. **Full Admin Control Dashboard:** Secure CRUD dashboard at `/admin` to add/edit/delete Speakers, Sessions/Agenda items, and Sponsors on the fly.
8. **Survival Guide (FAQ):** Access maps, catering details, and parking guidelines (highlighting locations for paid parking and free crossing bridges).

---

## 🛠️ Technology Stack

* **Language & Framework:** Java 25 & Quarkus (extremely fast, tiny memory footprint suitable for free-tier cloud environments).
* **UI Templating:** Quarkus Qute (type-safe server-side HTML rendering).
* **Interactions:** HTMX (AJAX and partial DOM updates directly from HTML attributes; **zero lines of custom JS/TS** written, maximizing hackathon rules compliance!).
* **Styling:** Custom Vanilla CSS (Dark theme with glowing neon accents, glassmorphic cards, and smooth CSS transitions).
* **QR Codes:** ZXing library (100% Java QR generator).
* **Database:** H2 Database (local file-based db so data persists across app rebuilds).

---

## 🏃 How to Run the App

### Prerequisites
* Java 21 or Java 25 installed.
* Maven installed (or use your local configuration).

### Start Dev Server (with Live Reload)
Run the following command in the project directory:
```bash
mvn compile quarkus:dev
```
Once started, open your browser and navigate to:
```
http://localhost:8080
```
*Note: Quarkus supports hot-reload out of the box. Any changes to Java files, templates, or CSS are compiled and refreshed instantly upon page reload!*

### Running Tests
To run the automated integration and unit tests:
```bash
mvn clean test
```

---

## 🧑‍💻 Testing Accounts (Mock Data)

The database automatically seeds mock data if empty (including speakers, sessions, sponsors, and attendees) so you can test all features offline.

* **Seeded Attendee Email Finder:**
  * `gocata@jprime.io`
  * `ivan@jprime.io`
  * `maria@jprime.io`
* **Raffle Check-In Stage Code:** `JPRIME2026` (Enter this on your badge card to check in for the draw!).
* **Sponsor Stamp Passphrases:**
  * Tide: `TIDE2026`
  * GraphWise: `GW2026`
  * Delasport: `DS2026`
  * EGT Digital: `EGT2026`
  * Coherent Solutions: `CS2026`

---

## 📄 License
This project is licensed under the permissive **MIT License**.
