package org.example.config;

public record ConversionConfig(
        boolean applyLocalLib,
        boolean updateLocalLib,
        String libDirectory
) {}
