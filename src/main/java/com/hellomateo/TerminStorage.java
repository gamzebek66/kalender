package com.hellomateo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TerminStorage {

    private static final String FILE_NAME = "termine.json";

    // Jackson Mapper
    private static final ObjectMapper mapper = new ObjectMapper();

    // Alle Termine laden
    public static List<Termin> ladeAlleTermine() {

        try {

            File file = new File(FILE_NAME);

            // Falls Datei noch nicht existiert
            if (!file.exists()) {
                return new ArrayList<>();
            }

            // JSON lesen
            return mapper.readValue(
                    file,
                    mapper.getTypeFactory()
                            .constructCollectionType(List.class, Termin.class)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    // Alle Termine speichern
    public static void speichereTermine(List<Termin> termine) {

        try {

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE_NAME), termine);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


}
