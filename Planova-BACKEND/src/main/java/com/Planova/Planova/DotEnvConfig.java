package com.Planova.Planova;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Carga variables de entorno desde archivo .env antes de que Spring cargue las propiedades
 * Se registra en spring.factories para ejecutarse temprano en el bootstrap
 */
public class DotEnvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Path envPath = Paths.get(".env");
        
        if (!Files.exists(envPath)) {
            return;
        }

        Map<String, Object> envMap = new HashMap<>();
        
        try (Stream<String> lines = Files.lines(envPath)) {
            lines.filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                 .forEach(line -> {
                     int equalsIndex = line.indexOf('=');
                     if (equalsIndex > 0) {
                         String key = line.substring(0, equalsIndex).trim();
                         String value = line.substring(equalsIndex + 1).trim();
                         envMap.put(key, value);
                     }
                 });
        } catch (IOException e) {
            System.err.println("Error al cargar archivo .env: " + e.getMessage());
            return;
        }

        // Agregar al environment de Spring con alta prioridad
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("dotEnv", envMap));
    }
}