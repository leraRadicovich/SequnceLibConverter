package org.example;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class PlantUmlAsciiGenerator {
    private final PrintWriter logger;

    public PlantUmlAsciiGenerator(PrintWriter logger) {
        this.logger = logger;
    }

    public void generateAsciiArt(Path inputFile, Path outputDir) throws Exception {
        log("Чтение файла: " + inputFile);
        String source = new String(Files.readAllBytes(inputFile), StandardCharsets.UTF_8);

        log("Запуск генерации ASCII-art...");
        SourceStringReader reader = new SourceStringReader(source);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // Генерация в формате UTXT
        reader.generateImage(os, new FileFormatOption(FileFormat.UTXT));
        os.close();

        String result = os.toString(StandardCharsets.UTF_8);
        log("Успешная генерация. Размер результата: " + result.length() + " символов");

        // Сохранение результата
        String fileName = inputFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        Path outputFile = outputDir.resolve(baseName + "_unicode_ascii_result.txt");

        Files.write(outputFile, result.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        log("Результат сохранен: " + outputFile);
    }

    private void log(String message) {
        String entry = "[" + LocalDateTime.now() + "] [Generator] " + message;
        if (logger != null) {
            logger.println(entry);
        }
        System.out.println(entry);
    }
}