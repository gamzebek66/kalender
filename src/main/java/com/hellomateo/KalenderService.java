package com.hellomateo;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.time.*;
import java.util.*;

@Service
public class KalenderService {

    private final TerminRepository terminRepository;

    public KalenderService(TerminRepository terminRepository) {
        this.terminRepository = terminRepository;
    }

    private static final String APPLICATION_NAME = "HelloMateoSync";
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(17, 30);
    private static final int SLOT_MIN = 60;

    private static final String CALENDAR_ID =
            "b79dd58cedb1c9b72763f233cd389820552341762edb03a0b87249bd1c2bcc2b@group.calendar.google.com";

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    // -------------------------
    // FREIE TERMINE (UNVERÄNDERT)
    // -------------------------
    public List<String> getFreieSlots(LocalDate datum) throws Exception {

        if (datum.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        Calendar service = getCalendarService();

        ZonedDateTime dayStart;
        ZonedDateTime dayEnd;

        if (datum.getDayOfWeek() == DayOfWeek.SATURDAY) {
            dayStart = ZonedDateTime.of(datum, LocalTime.of(12, 0), ZONE);
            dayEnd = ZonedDateTime.of(datum, LocalTime.of(15, 0), ZONE);
        } else {
            dayStart = ZonedDateTime.of(datum, START, ZONE);
            dayEnd = ZonedDateTime.of(datum, END, ZONE);
        }

        DateTime timeMin = new DateTime(dayStart.toInstant().toEpochMilli());
        DateTime timeMax = new DateTime(dayEnd.toInstant().toEpochMilli());

        Events events = service.events()
                .list(CALENDAR_ID)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .execute();

        List<Event> existingEvents = events.getItems();
        List<String> freieSlots = new ArrayList<>();

        ZonedDateTime currentSlot = dayStart;

        while (currentSlot.plusMinutes(SLOT_MIN).isBefore(dayEnd.plusSeconds(1))) {

            ZonedDateTime slotEnd = currentSlot.plusMinutes(SLOT_MIN);
            boolean isFree = true;

            for (Event event : existingEvents) {

                if (!"confirmed".equals(event.getStatus()) &&
                        !"tentative".equals(event.getStatus())) continue;

                if (event.getStart() == null || event.getEnd() == null) continue;

                DateTime startDT = event.getStart().getDateTime();
                DateTime endDT = event.getEnd().getDateTime();

                if (startDT == null || endDT == null) continue;

                ZonedDateTime eventStart = ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(startDT.getValue()), ZONE);

                ZonedDateTime eventEnd = ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(endDT.getValue()), ZONE);

                if (currentSlot.isBefore(eventEnd) && slotEnd.isAfter(eventStart)) {
                    isFree = false;
                    break;
                }
            }

            if (isFree) {
                freieSlots.add(currentSlot.toLocalTime().toString());
            }

            currentSlot = currentSlot.plusMinutes(SLOT_MIN);
        }

        return freieSlots;
    }

    // -------------------------
    // TERMIN BUCHEN + MONGO SPEICHERN
    // -------------------------
    public String terminBuchen(
            String datum,
            String uhrzeit,
            String vorname,
            String nachname,
            String telefon,
            String anliegen,
            String beschreibung,
            MultipartFile verordnung
    ) throws Exception {

        Calendar service = getCalendarService();

        LocalDate date = LocalDate.parse(datum);
        LocalTime time = LocalTime.parse(uhrzeit);

        ZonedDateTime start = ZonedDateTime.of(date, time, ZONE);

        int duration = "Einlagen".equalsIgnoreCase(anliegen) ? 30 : 60;

        ZonedDateTime end = start.plusMinutes(duration);

        // Google Calendar check
        Events events = service.events()
                .list(CALENDAR_ID)
                .setTimeMin(new DateTime(start.toInstant().toEpochMilli()))
                .setTimeMax(new DateTime(end.toInstant().toEpochMilli()))
                .setSingleEvents(true)
                .execute();

        if (!events.getItems().isEmpty()) {
            return "Termin ist leider schon vergeben!";
        }

        String dateiname = null;

        // ---------------- FILE UPLOAD ----------------
        if (verordnung != null && !verordnung.isEmpty()) {

            dateiname = System.currentTimeMillis() + "_" + verordnung.getOriginalFilename();

            Path uploadPfad = Paths.get("uploads");
            if (!Files.exists(uploadPfad)) {
                Files.createDirectories(uploadPfad);
            }

            Files.copy(
                    verordnung.getInputStream(),
                    uploadPfad.resolve(dateiname),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        // ---------------- GOOGLE CALENDAR EVENT ----------------
        Event event = new Event();

        event.setSummary("TERMIN - " + vorname + " " + nachname);

        event.setDescription(
                "Name: " + vorname + " " + nachname + "\n" +
                        "Telefon: " + telefon + "\n" +
                        "Anliegen: " + anliegen + "\n" +
                        "Verordnung: " + (dateiname != null ? "JA" : "NEIN")
        );

        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(start.toInstant().toEpochMilli())));

        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(end.toInstant().toEpochMilli())));

        service.events().insert(CALENDAR_ID, event).execute();

        // ---------------- MONGO SPEICHERN ----------------
        Termin termin = new Termin();
        termin.setVorname(vorname);
        termin.setNachname(nachname);
        termin.setDatum(datum);
        termin.setUhrzeit(uhrzeit);
        termin.setVerordnung(dateiname != null);
        termin.setDateiname(dateiname);

        terminRepository.save(termin);

        return "Der Termin wurde erfolgreich gebucht!";
    }

    // -------------------------
    // ADMIN DATEN AUS MONGO
    // -------------------------
    public List<Termin> getAlleTermine() {
        return terminRepository.findAll();
    }

    // -------------------------
    // GOOGLE AUTH
    // -------------------------
    private Calendar getCalendarService() throws Exception {

        String credentials = System.getenv("GOOGLE_CREDENTIALS");

        if (credentials == null || credentials.isBlank()) {
            throw new RuntimeException("GOOGLE_CREDENTIALS fehlt!");
        }

        credentials = credentials.replace("\\n", "\n");

        InputStream in = new ByteArrayInputStream(credentials.getBytes());

        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}