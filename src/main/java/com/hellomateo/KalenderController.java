package com.hellomateo;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @PostMapping("/buchen")
    public String terminBuchen(@RequestBody TerminRequest request) throws Exception
    {

        // Zum Prüfen kann später gelösct werden wird auch gelöscht
        System.out.println("Datum: " + request.getDatum());
        System.out.println("Uhrzeit: " + request.getUhrzeit());
        System.out.println("Name: " + request.getName());

        return kalenderService.terminBuchen(request);
    }

    @GetMapping("/")
    public String home() {
        return "API läuft 🚀 - nutze /kalender/freietermine";
    }


}