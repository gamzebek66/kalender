package com.hellomateo;

public class Termin {

    private String vorname;
    private String nachname;

    private String datum;
    private String uhrzeit;
    private boolean verordnung;
    private String dateiname;

    //Leerer Konstruktor
    public Termin() {

    }

    //Getter

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
    public boolean getVerordnung() {
        return verordnung;
    }
    public String getDateiname() {
        return dateiname;
    }

    //Setter
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
