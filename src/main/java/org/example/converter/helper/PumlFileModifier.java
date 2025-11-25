package org.example.converter.helper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PumlFileModifier {

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^\\s*!include\\s+.*$", Pattern.CASE_INSENSITIVE);

    /**
     * Модифицирует puml файл: комментирует существующие include директивы и добавляет новую
     * @param filePath путь к файлу для модификации
     * @param newIncludePath путь для новой include директивы (относительно корня библиотеки)
     * @throws IOException если произошла ошибка при чтении/записи файла
     */
    public static void modifyIncludeStatements(Path filePath, String newIncludePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<String> modifiedLines = new ArrayList<>();
        boolean includeFound = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (INCLUDE_PATTERN.matcher(trimmed).matches()) {
                // Комментируем существующую include директиву
                if (!trimmed.startsWith("'") && !trimmed.startsWith("//")) {
                    modifiedLines.add("'" + line);
                    includeFound = true;
                } else {
                    modifiedLines.add(line);
                }
            } else {
                modifiedLines.add(line);
            }
        }

        // Если нашли include директиву, добавляем новую после неё
        if (includeFound) {
            boolean added = false;
            List<String> result = new ArrayList<>();
            for (int i = 0; i < modifiedLines.size(); i++) {
                result.add(modifiedLines.get(i));
                // Если это закомментированная include, добавляем новую после неё
                if (modifiedLines.get(i).trim().startsWith("'") && 
                    INCLUDE_PATTERN.matcher(modifiedLines.get(i).substring(1).trim()).matches() && 
                    !added) {
                    result.add("!include " + newIncludePath);
                    added = true;
                }
            }
            modifiedLines = result;
        } else {
            // Если include не найдена, добавляем в начало после @startuml если есть
            List<String> result = new ArrayList<>();
            boolean startumlFound = false;
            for (String line : modifiedLines) {
                result.add(line);
                if (!startumlFound && line.trim().equals("@startuml")) {
                    startumlFound = true;
                    result.add("!include " + newIncludePath);
                }
            }
            if (!startumlFound) {
                // Если @startuml нет, добавляем в начало
                result.add(0, "!include " + newIncludePath);
            }
            modifiedLines = result;
        }

        // Сохраняем модифицированный файл
        Files.write(filePath, modifiedLines, StandardCharsets.UTF_8);
    }

    /**
     * Восстанавливает оригинальный файл из резервной копии
     * @param filePath путь к файлу
     * @param backupPath путь к резервной копии
     * @throws IOException если произошла ошибка
     */
    public static void restoreFromBackup(Path filePath, Path backupPath) throws IOException {
        if (Files.exists(backupPath)) {
            Files.copy(backupPath, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Создает резервную копию файла
     * @param filePath путь к файлу
     * @return путь к резервной копии
     * @throws IOException если произошла ошибка
     */
    public static Path createBackup(Path filePath) throws IOException {
        Path backupPath = filePath.resolveSibling(filePath.getFileName().toString() + ".backup");
        Files.copy(filePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return backupPath;
    }
}

