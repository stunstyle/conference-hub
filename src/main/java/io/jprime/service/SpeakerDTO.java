package io.jprime.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpeakerDTO {
    public Long id;
    public String firstName;
    public String lastName;
    public String company;
    public String bio;
    public String twitter;
    public String email;
}
