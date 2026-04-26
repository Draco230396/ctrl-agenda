package com.spiid.login.service.infrastructure.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleConfig {

    @Bean
    public NetHttpTransport netHttpTransport(){
        return new NetHttpTransport();
    }
}
