package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class Question extends PanacheEntity {
    public Long sessionId;
    public String sender;
    public String questionText;
    public int upvotes;
    public LocalDateTime timestamp;
}
