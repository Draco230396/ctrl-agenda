package com.spiid.iam.service.domain.port.out;

import com.spiid.iam.service.application.dto.GoogleUserInfo;

/**
 * Se crea puerto de salida
 */
public interface GoogleTokenVerifierPort {

    GoogleUserInfo verify(String idToken);

}
