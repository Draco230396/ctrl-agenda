package com.spiid.login.service.domain.port.out;

import com.spiid.login.service.domain.model.GoogleUserInfo;

/**
 * Se crea puerto de salida
 */
public interface GoogleTokenVerifierPort {

    GoogleUserInfo verify(String idToken);

}
