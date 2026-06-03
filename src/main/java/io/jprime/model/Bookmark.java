package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Bookmark extends PanacheEntity {
    public Long sessionId;
    public String clientToken; // Browser session token (stored in cookie)
}
