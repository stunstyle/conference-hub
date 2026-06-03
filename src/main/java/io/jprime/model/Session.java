package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Session extends PanacheEntity {
    public String title;
    
    @Column(length = 4000)
    public String description;
    
    public String hallName; // Hall A, Hall B, Workshops, or null (break/registration)
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    
    @ManyToOne
    public Speaker speaker;

    @ManyToOne
    public Speaker coSpeaker;
}
