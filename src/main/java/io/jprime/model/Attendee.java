package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Attendee extends PanacheEntity {
    public String email;
    public String firstName;
    public String lastName;
    public String company;
    public String ticketCode;
    public boolean checkedIn; // Checked-in for closing ceremony raffle
    public boolean isAdmin;
    public String currentSessionToken;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
