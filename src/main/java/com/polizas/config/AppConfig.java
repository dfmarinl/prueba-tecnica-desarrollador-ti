package com.polizas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppConfig(String apiKey, Ipc ipc) {

    public record Ipc(double porcentaje) {
    }
}
