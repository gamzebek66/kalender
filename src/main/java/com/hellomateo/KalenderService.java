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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.util.*;

@Service
public class KalenderService {

    private static final String APPLICATION_NAME = "HelloMateoSync";
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(17, 30);
    private static final int SLOT_MIN = 60;

    private static final String CALENDAR_ID =
            "b79dd58cedb1c9b72763f233cd389820552341762edb03a0b87249bd1c2bcc2b@group.calendar.google.com";

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    private static List<Termin> termine =  //eine private Liste von Terminen
            TerminStorage.ladeAlleTermine(); //Hier wird alle Temrine aus dem JSON in eine Liste von Terminen gespeichert

    public List<String> getFreieSlots(LocalDate datum) throws Exception {

        System.out.println("👉 getFreieSlots gestartet: " + datum);

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
                        !"tentative".equals(event.getStatus())) {
                    continue;
                }

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

        int duration = 60;
        String dateiname = null;

        if ("Einlagen".equalsIgnoreCase(anliegen)) {
            duration = 30;
        }

        ZonedDateTime end = start.plusMinutes(duration);

        // Prüfen ob frei
        Events events = service.events()
                .list(CALENDAR_ID)
                .setTimeMin(new DateTime(start.toInstant().toEpochMilli()))
                .setTimeMax(new DateTime(end.toInstant().toEpochMilli()))
                .setSingleEvents(true)
                .execute();

        for (Event e : events.getItems()) {

            if (!"confirmed".equals(e.getStatus()) &&
                    !"tentative".equals(e.getStatus())) {
                continue;
            }

            if (e.getStart() == null || e.getEnd() == null) continue;

            DateTime evStart = e.getStart().getDateTime();
            DateTime evEnd = e.getEnd().getDateTime();

            if (evStart == null || evEnd == null) continue;

            ZonedDateTime es = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(evStart.getValue()), ZONE);

            ZonedDateTime ee = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(evEnd.getValue()), ZONE);

            if (start.isBefore(ee) && end.isAfter(es)) {
                return "Termin ist leider schon vergeben!";
            }
        }

        Event event = new Event();

        String summary = "TERMIN - " + vorname + " " + nachname;

        String descriptionText =
                "Name: " + vorname + " " + nachname + "\n" +
                        "Telefon: " + telefon + "\n" +
                        "Anliegen: " + anliegen + "\n";

        if (beschreibung != null && !beschreibung.isBlank()) {
            descriptionText += "Beschreibung: " + beschreibung + "\n";
        }


        //neuer Codeteil JSON überarbeitung

        


        if (verordnung != null && !verordnung.isEmpty()) {

            dateiname =
                    System.currentTimeMillis() + "_" +
                            verordnung.getOriginalFilename();

            Path uploadPfad = Paths.get("uploads");

            if (!Files.exists(uploadPfad)) {
                Files.createDirectories(uploadPfad);
            }

            Path dateiPfad = uploadPfad.resolve(dateiname);

            Files.copy(
                    verordnung.getInputStream(),
                    dateiPfad,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("Datei gespeichert: " + dateiPfad);

            descriptionText += "Verordnung: JA\n";
            descriptionText += "Datei: " + dateiname + "\n";

        } else {

            descriptionText += "Verordnung: NEIN\n";
        }



        Termin termin = new Termin();

        termin.setVorname(vorname);
        termin.setNachname(nachname);
        termin.setDatum(datum);
        termin.setUhrzeit(uhrzeit);

        boolean hatVerordnung =
                verordnung != null && !verordnung.isEmpty();

        termin.setVerordnung(hatVerordnung);

        if (hatVerordnung) {
            termin.setDateiname(dateiname);
        }

        /*
        termine.add(termin);
        TerminStorage.speichereTermine(termine);

         */

        List<Termin> termine = TerminStorage.ladeAlleTermine();

        termine.add(termin);

        TerminStorage.speichereTermine(termine);


        event.setSummary(summary);
        event.setDescription(descriptionText);
        event.setStatus("tentative");

        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(start.toInstant().toEpochMilli())));

        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(end.toInstant().toEpochMilli())));

        service.events().insert(CALENDAR_ID, event).execute();

        return "Der Termin wurde erfolgreich gebucht!";
    }

    private Calendar getCalendarService() throws Exception {

        String credentials = System.getenv("GOOGLE_CREDENTIALS");

        if (credentials == null || credentials.isBlank()) {
            throw new RuntimeException("GOOGLE_CREDENTIALS ist NICHT gesetzt!");
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
