package org.example.converter.helper;

import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EmbeddedLibInstaller {

    /**
     * Копирует встроенную библиотеку из ресурсов в указанную директорию вместе с plantuml.jar
     * @param targetDir директория, куда копировать библиотеку
     * @throws IOException если произошла ошибка при копировании
     */
    public static void copyEmbeddedLibrary(Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        
        // Копируем всю структуру sequenceLibPuml
        copyResourceDirectory("sequenceLibPuml", targetDir.resolve("sequenceLibPuml"));
        
        // Копируем plantuml.jar
        copyPlantUmlJar(targetDir);
        
        System.out.println("Встроенная библиотека скопирована в: " + targetDir);
    }

    /**
     * Копирует директорию из ресурсов рекурсивно
     */
    private static void copyResourceDirectory(String resourcePath, Path targetPath) throws IOException {
        ClassLoader classLoader = EmbeddedLibInstaller.class.getClassLoader();
        
        // Получаем все ресурсы в директории
        try {
            java.net.URL resourceUrl = classLoader.getResource(resourcePath);
            if (resourceUrl == null) {
                throw new IOException("Ресурс не найден: " + resourcePath);
            }
            
            // Если ресурс находится в JAR файле
            if (resourceUrl.getProtocol().equals("jar")) {
                String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
                try (JarFile jar = new JarFile(java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(resourcePath + "/") && !name.equals(resourcePath + "/")) {
                            String relativePath = name.substring(resourcePath.length() + 1);
                            Path targetFile = targetPath.resolve(relativePath);
                            
                            if (entry.isDirectory()) {
                                Files.createDirectories(targetFile);
                            } else {
                                Files.createDirectories(targetFile.getParent());
                                try (InputStream in = jar.getInputStream(entry)) {
                                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        }
                    }
                }
            } else {
                // Если ресурс находится в файловой системе (для разработки)
                try {
                    Path sourcePath = Paths.get(resourceUrl.toURI());
                    if (Files.exists(sourcePath)) {
                        Files.walk(sourcePath).forEach(source -> {
                            try {
                                Path target = targetPath.resolve(sourcePath.relativize(source));
                                if (Files.isDirectory(source)) {
                                    Files.createDirectories(target);
                                } else {
                                    Files.createDirectories(target.getParent());
                                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (java.net.URISyntaxException e) {
                    throw new IOException("Ошибка URI: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            throw new IOException("Ошибка копирования ресурсов: " + e.getMessage(), e);
        }
    }

    /**
     * Копирует plantuml.jar из ресурсов
     */
    private static void copyPlantUmlJar(Path targetDir) throws IOException {
        String jarResource = "plantuml-gplv2-1.2025.0.jar";
        try (InputStream in = EmbeddedLibInstaller.class.getClassLoader().getResourceAsStream(jarResource)) {
            if (in == null) {
                throw new IOException("Не найден plantuml.jar в ресурсах: " + jarResource);
            }
            Path jarPath = targetDir.resolve("plantuml.jar");
            Files.copy(in, jarPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Скопирован plantuml.jar в: " + jarPath);
        }
    }
}

