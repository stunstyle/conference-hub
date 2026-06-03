package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Feedback extends PanacheEntity {
    public Long sessionId;
    public int rating; // 1 to 5 stars
    public String comment;
}
