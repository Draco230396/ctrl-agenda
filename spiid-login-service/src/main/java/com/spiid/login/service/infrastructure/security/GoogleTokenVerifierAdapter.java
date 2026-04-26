package com.spiid.login.service.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.spiid.login.service.application.dto.GoogleUserInfo;
import com.spiid.login.service.domain.port.out.GoogleTokenVerifierPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Se implementa un adaptador para la infraestructura
 */

@Component
public class GoogleTokenVerifierAdapter implements GoogleTokenVerifierPort {
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierAdapter(
            NetHttpTransport transport,
            @Value("${google.client-id}") String clientId
    ) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                transport,JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .setIssuer("https://accounts.google.com")
                .build();
    }

    @Override
    public GoogleUserInfo verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);

            if (token == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            var payload = token.getPayload();

            if (payload.getEmailVerified() == null || !payload.getEmailVerified()) {
                throw new IllegalArgumentException("Email not verified");
            }

            return new GoogleUserInfo(
                    payload.getEmail(),
                    (String) payload.get("name"),
                    payload.getSubject()
            );

        } catch (Exception e) {
            throw new IllegalStateException("Error validating Google token", e);
        }
    }
}