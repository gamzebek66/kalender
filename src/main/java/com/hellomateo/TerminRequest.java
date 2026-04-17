package com.hellomateo;

public class TerminRequest {

    private String datum;
    private String vorname;
    private String nachname;
    private String uhrzeit;
    //private String name;
    private String telefon;
    private String anliegen;
    private String beschreibung;

    //Neu für PDF/Bilder
    private String attestUrl;



    //auch neu für Bilder
    public String getAttestUrl() {
        return attestUrl;
    }

    public void setAttestUrl(String attestUrl) {
        this.attestUrl = attestUrl;
    }




    public TerminRequest() {
    }

    public String getVorname(){
        return vorname;
    }

    public String getNachname(){
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }




    public String getDatum() {
        return datum;
    }

    public String getTelefon() {
        return telefon;
    }
    public String getAnliegen() {
        return anliegen;
    }
    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public void setAnliegen(String anliegen) {
        this.anliegen = anliegen;
    }



    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getUhrzeit() {
        return uhrzeit;
    }

    public void setUhrzeit(String uhrzeit) {
        this.uhrzeit = uhrzeit;
    }

    public void setVorname(String vorname)
    {
        this.vorname = vorname;
    }

    public String getBeschreibung(){
        return beschreibung;
    }
    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }
}