package io.jprime.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionDTO {
    public Long id;
    public String hallName;
    public String title;
    public String lectorName;
    public String coLectorName;
    public String talkDescription;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
}
