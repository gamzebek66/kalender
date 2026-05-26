package com.hellomateo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "termine")
public class Termin {

    @Id
    private String id;

    private String vorname;
    private String nachname;

    private String datum;
    private String uhrzeit;

    private boolean verordnung;
    private String dateiname;

    public Termin() {}

    // Getter
    public String getId() {
        return id;
    }

    public String getVorname() {
        return vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public String getDatum() {
        return datum;
    }

    public String getUhrzeit() {
        return uhrzeit;
    }

    public boolean isVerordnung() {
        return verordnung;
    }

    public String getDateiname() {
        return dateiname;
    }

    // Setter
    public void setId(String id) {
        this.id = id;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public void setUhrzeit(String uhrzeit) {
        this.uhrzeit = uhrzeit;
    }

    public void setVerordnung(boolean verordnung) {
        this.verordnung = verordnung;
    }

    public void setDateiname(String dateiname) {
        this.dateiname = dateiname;
    }
}
