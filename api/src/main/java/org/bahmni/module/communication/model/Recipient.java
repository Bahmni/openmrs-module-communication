package org.bahmni.module.communication.model;

public class Recipient {
    private String name;
    private String email;

    public Recipient(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Recipient() {}

    public String getName() { return name; }

    public String getEmail() { return email; }
}
