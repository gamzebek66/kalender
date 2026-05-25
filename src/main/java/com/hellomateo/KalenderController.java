package com.hellomateo;

import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/kalender")
public class KalenderController {

    private final KalenderService kalenderService;

    public KalenderController(KalenderService kalenderService)
    {
        this.kalenderService = kalenderService;
    }

    @GetMapping("/freietermine")
    public List<String> freieTermine(@RequestParam String datum) throws Exception
    {

        LocalDate date = LocalDate.parse(datum);

        return kalenderService.getFreieSlots(date);
    }

    @PostMapping(value = "/buchen", consumes = "multipart/form-data")
    public String terminBuchen(
            @RequestParam String datum,
            @RequestParam String uhrzeit,
            @RequestParam String vorname,
            @RequestParam String nachname,
            @RequestParam String telefon,
            @RequestParam String anliegen,
            @RequestParam(required = false) String beschreibung,
            @RequestParam(required = false) MultipartFile verordnung
    ) throws Exception {

        System.out.println("👉 Neue Buchung:");
        System.out.println("Datum: " + datum);
        System.out.println("Uhrzeit: " + uhrzeit);
        System.out.println("Name: " + vorname + " " + nachname);
        System.out.println("Telefon: " + telefon);
        System.out.println("Anliegen: " + anliegen);

        if (verordnung != null) {
            System.out.println("Verordnung erhalten: " + verordnung.getOriginalFilename());
        }

        return kalenderService.terminBuchen(
                datum,
                uhrzeit,
                vorname,
                nachname,
                telefon,
                anliegen,
                beschreibung,
                verordnung
        );
    }

    @GetMapping("/")
    public String home() {
        return "API läuft 🚀 - nutze /kalender/freietermine";
    }

}