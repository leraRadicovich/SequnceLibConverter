package org.example.converter.helper;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class LocalLibInstaller {

    private static final List<String> LIB_FILES = List.of("SequenceLibIncludeFile_v4.puml", "lib.puml");

    public static void installIfNeeded(boolean apply, boolean update, Path libPath) {
        if (!apply) return;

        boolean exists = Files.exists(libPath);

        if (!exists) {
            createLib(libPath);
            return;
        }

        if (update) {
            createLib(libPath);
        }
    }

    private static void createLib(Path libPath) {
        try {
            Files.createDirectories(libPath);
            for (String fileName : LIB_FILES) {
                Path file = libPath.resolve(fileName);
                try (InputStream in = getInternalFile(fileName)) {
                    if (in == null) {
                        System.err.println("Не найден файл библиотеки в ресурсах: " + fileName);
                        continue;
                    }
                    Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Скопирован файл: " + file);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка установки библиотеки: " + e.getMessage());
        }
    }

    private static InputStream getInternalFile(String name) {
        return LocalLibInstaller.class.getClassLoader().getResourceAsStream("plantuml-lib/" + name);
    }
}
