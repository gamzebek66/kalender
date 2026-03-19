package com.hellomateo;

public class TerminRequest {

    private String datum;
    private String uhrzeit;
    private String name;

    // 🔥 WICHTIG
    public TerminRequest() {
    }


    public String getDatum() {
        return datum;
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