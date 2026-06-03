package io.jprime.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient
public interface JPrimeClient {

    @GET
    @Path("/api/speaker")
    @Produces(MediaType.APPLICATION_JSON)
    List<SpeakerDTO> getSpeakers();

    @GET
    @Path("/pwa/findSessionsByHall")
    @Produces(MediaType.APPLICATION_JSON)
    List<SessionDTO> getSessionsByHall(@QueryParam("hallName") String hallName);
}
