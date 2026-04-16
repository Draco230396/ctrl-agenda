package com.spiid.login.service.infrastructure.inbound.rest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class InfoController {

  @Value("${spring.application.name}")
  private String serviceName;

  @GetMapping("/info")
  public Map<String, Object> info() {
    return Map.of(
        "service", serviceName,
        "status", "ok",
        "ts", Instant.now().toString()
    );
  }
}
