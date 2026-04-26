package com.spiid.login.service.application.dto;

/**
 * Representa el rol proviniente del catalogo (iam.catalog_role)
 * Code: Numero que envia el cliente (roleCodes)
 * Key: Cadena estable para autorizacion (ej: DRIVER)
 * Description: Testo para mostrar en UI/Mensajes
 * */
public record RoleCatalogItem(short code, String key, String description) {}