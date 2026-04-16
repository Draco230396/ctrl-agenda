package com.spiid.login.service.infrastructure.inbound.rest.controller;

import com.spiid.login.service.domain.port.in.CatalogUseCase;
import com.spiid.login.service.infrastructure.inbound.rest.dto.AuthDtos;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogos (lectura) para UI.
 * Nota: en el futuro podrías mover catálogos a un microservicio propio, pero por ahora IAM está bien.
 */
@RestController
@RequestMapping(value = "/api/v1/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
public class CatalogController {

  private final CatalogUseCase catalog;

  public CatalogController(CatalogUseCase catalog) {
    this.catalog = catalog;
  }

  @GetMapping("/roles")
  public List<AuthDtos.RoleView> roles() {
    return catalog.listRoles()
        .stream()
        .map(r -> new AuthDtos.RoleView(r.code(), r.key(), r.description()))
        .toList();
  }
}
