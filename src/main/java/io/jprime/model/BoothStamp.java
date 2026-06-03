package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class BoothStamp extends PanacheEntity {
    public String clientToken;
    public Long sponsorId;
}
