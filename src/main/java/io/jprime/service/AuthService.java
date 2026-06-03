package io.jprime.service;

import io.jprime.model.Attendee;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
import jakarta.inject.Inject;
import java.util.Map;

@RequestScoped
public class AuthService {

    @Context
    HttpHeaders headers;

    public String getClientToken() {
        if (headers == null) return null;
        Map<String, Cookie> cookies = headers.getCookies();
        if (cookies != null && cookies.containsKey("client_token")) {
            return cookies.get("client_token").getValue();
        }
        return null;
    }

    public Attendee getCurrentUser() {
        String token = getClientToken();
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return Attendee.find("currentSessionToken", token).firstResult();
    }
}
