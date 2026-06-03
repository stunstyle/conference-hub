package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Sponsor extends PanacheEntity {
    public String name;
    public String level; // Gold, Silver, Bronze
    public String logoFileName;
    public String secretCode; // Code that attendees enter to get their stamp
}
