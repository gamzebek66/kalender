package com.hellomateo;

public class TerminRequest {

    private String datum;
    private String uhrzeit;
    private String name;
    private String telefon;
    private String anliegen;

    // 🔥 WICHTIG
    public TerminRequest() {
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
    public void setTelefon(String datum) {
        this.telefon = telefon;
    }

    public void setAn(String datum) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}