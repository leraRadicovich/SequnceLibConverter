package org.example.deconverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class FileProcessor implements AutoCloseable {
    private static final String OUTPUT_SUFFIX = "_unicode_ascii_result.txt";
    private static final String LOG_FILE = "processing.log";

    private final PlantUmlAsciiGenerator generator;
    private final PrintWriter logger;
    private final Path baseDir;
    private final Path resultDir;

    public FileProcessor(Path baseDir) throws IOException {
        this.baseDir = baseDir;
        this.resultDir = baseDir.resolve("result");
        Files.createDirectories(resultDir);
        this.logger = setupLogger();
        this.generator = new PlantUmlAsciiGenerator(logger);
    }

    public void process(Path inputPath) throws IOException {
        log("Начало обработки: " + inputPath);

        if (Files.isDirectory(inputPath)) {
            processDirectory(inputPath);
        } else if (isPumlFile(inputPath)) {
            processSingleFile(inputPath);
        } else {
            log("Пропущен файл не .puml формата: " + inputPath);
        }

        log("Обработка завершена");
    }

    private void processDirectory(Path dir) throws IOException {
        log("Сканирование директории: " + dir);
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(this::isPumlFile)
                    .forEach(this::processSingleFile);
        }
    }

    private void processSingleFile(Path pumlFile) {
        try {
            log("Обработка файла: " + pumlFile.getFileName());

            generator.generateAsciiArt(pumlFile, resultDir);
            Path asciiFile = renameOutputFile(pumlFile);
            processGeneratedFile(pumlFile, asciiFile);

            log("Успешно обработан: " + pumlFile.getFileName());
        } catch (Exception e) {
            log("ОШИБКА при обработке " + pumlFile + ": " + e.getMessage());
        }
    }

    private void processGeneratedFile(Path pumlFile, Path asciiFile) throws IOException {
        String fileName = pumlFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        String targetPumlName = baseName + "_original.puml";
        String targetMdName = baseName + "_procMap.md";

        String asciiContent = Files.readString(asciiFile);
        String diagramCode = ResultParser.extractDiagramCode(asciiContent);
        String legendContent = ResultParser.extractLegendContent(asciiContent);

        if (!diagramCode.isEmpty()) {
            Files.writeString(resultDir.resolve(targetPumlName), diagramCode);
        }

        if (!legendContent.isEmpty()) {
            Files.writeString(resultDir.resolve(targetMdName), legendContent);
        }
    }

    private Path renameOutputFile(Path pumlFile) throws IOException {
        String fileName = pumlFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        Path source = resultDir.resolve(baseName + ".utxt");
        Path target = resultDir.resolve(baseName + OUTPUT_SUFFIX);

        if (Files.exists(source)) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log("Файл переименован: " + target.getFileName());
        }

        return target;
    }

    private PrintWriter setupLogger() throws IOException {
        Path logFile = resultDir.resolve(LOG_FILE);
        return new PrintWriter(new FileWriter(logFile.toFile(), false), true);
    }

    private void log(String message) {
        String entry = "[" + LocalDateTime.now() + "] " + message;
        logger.println(entry);
        System.out.println(entry);
    }

    @Override
    public void close() {
        if (logger != null) {
            logger.close();
        }
    }

    private boolean isPumlFile(Path path) {
        return path.toString().toLowerCase().endsWith(".puml");
    }
}