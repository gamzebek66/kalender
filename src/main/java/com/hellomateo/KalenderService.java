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

import java.io.ByteArrayInputStream;
import java.time.DayOfWeek;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class KalenderService {

    private static final String APPLICATION_NAME = "HelloMateoSync";
    private static final LocalTime START = LocalTime.of(9,0);
    private static final LocalTime END = LocalTime.of(17,30);
    private static final int SLOT_MIN = 30;

    private static final String CALENDAR_ID =
          /* "94ea7920226aeaad6ad5a0e36aa7995b713681e3a5af4069fdf1a89774cb8dcf@group.calendar.google.com";*/
             "b79dd58cedb1c9b72763f233cd389820552341762edb03a0b87249bd1c2bcc2b@group.calendar.google.com";

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    public List<String> getFreieSlots(LocalDate datum) throws Exception {
        if (datum.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        if (datum.getDayOfWeek() == DayOfWeek.SATURDAY) {

            ZonedDateTime dayStart = ZonedDateTime.of(datum, LocalTime.of(12, 0), ZONE);
            ZonedDateTime dayEnd = ZonedDateTime.of(datum, LocalTime.of(15, 0), ZONE);

            List<String> freieSlots = new ArrayList<>();
            Calendar service = getCalendarService();

            DateTime timeMin = new DateTime(dayStart.toInstant().toEpochMilli());
            DateTime timeMax = new DateTime(dayEnd.toInstant().toEpochMilli());

            Events events = service.events()
                    .list(CALENDAR_ID)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setSingleEvents(true)
                    .execute();

            List<Event> existingEvents = events.getItems();
            ZonedDateTime currentSlot = dayStart;

            while(currentSlot.plusMinutes(SLOT_MIN).isBefore(dayEnd.plusSeconds(1))) {
                ZonedDateTime slotEnd = currentSlot.plusMinutes(SLOT_MIN);
                boolean isFree = true;

                for(Event event : existingEvents) {
                    String status = event.getStatus();
                    if ("tentative".equals(status) || "confirmed".equals(status)) {
                        DateTime eventStartDT = event.getStart().getDateTime();
                        DateTime eventEndDT = event.getEnd().getDateTime();
                        if(eventStartDT == null || eventEndDT == null) continue;

                        ZonedDateTime eventStart = ZonedDateTime.ofInstant(
                                Instant.ofEpochMilli(eventStartDT.getValue()), ZONE);
                        ZonedDateTime eventEnd = ZonedDateTime.ofInstant(
                                Instant.ofEpochMilli(eventEndDT.getValue()), ZONE);

                        if(currentSlot.isBefore(eventEnd) && slotEnd.isAfter(eventStart)) {
                            isFree = false;
                            break;
                        }
                    }
                }

                if(isFree) {
                    freieSlots.add(currentSlot.toLocalTime().toString());
                }
                currentSlot = currentSlot.plusMinutes(SLOT_MIN);
            }

            return freieSlots; // für Samstag direkt zurückgeben

        }

        List<String> freieSlots = new ArrayList<>();
        Calendar service = getCalendarService();

        ZonedDateTime dayStart = ZonedDateTime.of(datum, START, ZONE);
        ZonedDateTime dayEnd = ZonedDateTime.of(datum, END, ZONE);

        DateTime timeMin = new DateTime(dayStart.toInstant().toEpochMilli());
        DateTime timeMax = new DateTime(dayEnd.toInstant().toEpochMilli());

        Events events = service.events()
                .list(CALENDAR_ID)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .execute();

        List<Event> existingEvents = events.getItems();
        ZonedDateTime currentSlot = dayStart;

        while(currentSlot.plusMinutes(SLOT_MIN).isBefore(dayEnd.plusSeconds(1))) {
            ZonedDateTime slotEnd = currentSlot.plusMinutes(SLOT_MIN);
            boolean isFree = true;

            for(Event event : existingEvents) {
                String status = event.getStatus();
                if ("tentative".equals(status) || "confirmed".equals(status)) {
                    DateTime eventStartDT = event.getStart().getDateTime();
                    DateTime eventEndDT = event.getEnd().getDateTime();
                    if(eventStartDT == null || eventEndDT == null) continue;

                    ZonedDateTime eventStart = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(eventStartDT.getValue()), ZONE);
                    ZonedDateTime eventEnd = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(eventEndDT.getValue()), ZONE);

                    if(currentSlot.isBefore(eventEnd) && slotEnd.isAfter(eventStart)) {
                        isFree = false;
                        break;
                    }
                }
            }

            if(isFree) {
                freieSlots.add(currentSlot.toLocalTime().toString());
            }
            currentSlot = currentSlot.plusMinutes(SLOT_MIN);
        }

        return freieSlots;
    }

    private Calendar getCalendarService() throws Exception {
        String credentials = System.getenv("GOOGLE_CREDENTIALS");
        if (credentials == null) throw new RuntimeException("GOOGLE_CREDENTIALS ist NICHT gesetzt!");

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

    public String terminBuchen(TerminRequest request) throws Exception {
        Calendar service = getCalendarService();

        LocalDate date = LocalDate.parse(request.getDatum());
        LocalTime time = LocalTime.parse(request.getUhrzeit());

        ZonedDateTime start = ZonedDateTime.of(date, time, ZONE);
        ZonedDateTime end = start.plusMinutes(30);

        // Doppelbuchungsprüfung
        Events events = service.events()
                .list(CALENDAR_ID)
                .setTimeMin(new DateTime(start.toInstant().toEpochMilli()))
                .setTimeMax(new DateTime(end.toInstant().toEpochMilli()))
                .setSingleEvents(true)
                .execute();

        for (Event e : events.getItems()) {
            String status = e.getStatus();
            if ("tentative".equals(status) || "confirmed".equals(status)) {
                DateTime evStart = e.getStart().getDateTime();
                DateTime evEnd = e.getEnd().getDateTime();
                if (evStart != null && evEnd != null) {
                    ZonedDateTime es = ZonedDateTime.ofInstant(Instant.ofEpochMilli(evStart.getValue()), ZONE);
                    ZonedDateTime ee = ZonedDateTime.ofInstant(Instant.ofEpochMilli(evEnd.getValue()), ZONE);
                    if (start.isBefore(ee) && end.isAfter(es)) {
                        return "Termin ist leider schon vergeben!";
                    }
                }
            }
        }

        // Neues Event erstellen
        Event event = new Event();

        String summaryText;
        String descriptionText;

        if ("Sonstiges".equals(request.getAnliegen()) && request.getBeschreibung() != null && !request.getBeschreibung().isBlank()) {
            summaryText = "ANFRAGE - " + request.getVorname() + " " + request.getNachname() + " (Sonstiges)";
            descriptionText = request.getBeschreibung();
        } else {
            summaryText = "ANFRAGE - " + request.getVorname() + " " + request.getNachname();
            descriptionText =
                    "Name: " + request.getVorname() + " " + request.getNachname() + "\n" +
                            "Telefon: " + request.getTelefon() + "\n" +
                            "Anliegen: " + request.getAnliegen();
        }

        event.setSummary(summaryText);
        event.setDescription(descriptionText);
        event.setStatus("tentative");

        DateTime startDateTime = new DateTime(start.toInstant().toEpochMilli());
        DateTime endDateTime = new DateTime(end.toInstant().toEpochMilli());
        event.setStart(new EventDateTime().setDateTime(startDateTime));
        event.setEnd(new EventDateTime().setDateTime(endDateTime));

        // Event in Google Kalender einfügen
        service.events().insert(CALENDAR_ID, event).execute();

        return "Der Termin wurde erfolgreich gebucht!";
    }

}