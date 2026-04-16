package com.spiid.login.service.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.spiid.login.service.domain.model.GoogleUserInfo;
import com.spiid.login.service.domain.port.out.GoogleTokenVerifierPort;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Se implementa un adaptador para la infraestructura
 */

@Component
public class GoogleTokenVerifierAdapter implements GoogleTokenVerifierPort {
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierAdapter() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList("TU_CLIENT_ID"))
                .build();
    }
    /**
     * @param idToken
     * @return
     */
    @Override
    public GoogleUserInfo verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);

            if (token == null) {
                throw new RuntimeException("Invalid Google token");
            }

            var payload = token.getPayload();

            return new GoogleUserInfo(
                    payload.getEmail(),
                    (String) payload.get("name")
            );

        } catch (Exception e) {
            throw new RuntimeException("Error validating Google token", e);
        }
    }
}
