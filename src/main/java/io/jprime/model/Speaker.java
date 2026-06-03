package io.jprime.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class Speaker extends PanacheEntity {
    public String firstName;
    public String lastName;
    public String company;
    
    @Column(length = 2000)
    public String bio;
    public String twitter;
    public String email;
    public String pictureUrl;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
