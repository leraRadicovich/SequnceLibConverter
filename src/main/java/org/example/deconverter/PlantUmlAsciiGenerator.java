package org.example.deconverter;

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
        String source = Files.readString(inputFile, StandardCharsets.UTF_8);

        log("Запуск генерации ASCII-art...");
        SourceStringReader reader = new SourceStringReader(source);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        reader.generateImage(os, new FileFormatOption(FileFormat.UTXT));
        os.close();

        String result = os.toString(StandardCharsets.UTF_8);
        log("Успешная генерация. Размер результата: " + result.length() + " символов");

        Path outputFile = outputDir.resolve(getBaseName(inputFile) + "_unicode_ascii_result.txt");
        Files.writeString(outputFile, result, StandardOpenOption.CREATE);
        log("Результат сохранен: " + outputFile);
    }

    private String getBaseName(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private void log(String message) {
        String entry = "[" + LocalDateTime.now() + "] [Generator] " + message;
        if (logger != null) {
            logger.println(entry);
        }
        System.out.println(entry);
    }
}