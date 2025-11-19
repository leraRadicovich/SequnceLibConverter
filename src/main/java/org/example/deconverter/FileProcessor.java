package org.example.deconverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class FileProcessor implements AutoCloseable {
    private static final String RESULT_DIR = "result";
    private static final String OUTPUT_SUFFIX = "_unicode_ascii_result.txt";
    private static final String LOG_FILE = "processing.log";

    private final PlantUmlAsciiGenerator generator;
    private PrintWriter logger;
    private final Path baseDir;
    private final boolean outputLogEnabled;

    /**
     * Конструктор класса FileProcessor.
     *
     * @param baseDir          Базовая директория для обработки файлов.
     * @param outputLogEnabled Флаг, определяющий, следует ли сохранять лог.
     *                         Если true, лог сохраняется всегда.
     *                         Если false, лог сохраняется только при возникновении ошибок.
     * @throws IOException Если произошла ошибка при настройке логгера.
     */
    public FileProcessor(Path baseDir, boolean outputLogEnabled) throws IOException {
        this.baseDir = baseDir;
        this.outputLogEnabled = outputLogEnabled;
        this.generator = new PlantUmlAsciiGenerator(null); // Изначально передаем null
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
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.filter(this::isPumlFile)
                    .forEach(this::processSingleFile);
        }
    }

    private void processSingleFile(Path pumlFile) {
        try {
            log("Обработка файла: " + pumlFile.getFileName());
            Path resultDir = createResultDir(pumlFile.getParent());

            // Генерация ASCII-арта
            generator.generateAsciiArt(pumlFile, resultDir);

            // Переименование выходного файла
            renameOutputFile(pumlFile, resultDir);

            // Обработка результатов
            processGeneratedFile(pumlFile, resultDir);

            // Удаление временного файла
            deleteTempFile(pumlFile, resultDir);

            log("Успешно обработан: " + pumlFile.getFileName());
        } catch (Exception e) {
            log("ОШИБКА при обработке " + pumlFile + ": " + e.getMessage());
        }
    }

    private void processGeneratedFile(Path pumlFile, Path resultDir) throws IOException {
        String fileName = pumlFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        // Путь к сгенерированному файлу
        Path asciiFile = resultDir.resolve(baseName + "_unicode_ascii_result.txt");

        // Парсинг и сохранение разделенных файлов
        ResultParser.saveSplitResults(asciiFile, resultDir, baseName);
    }

    private void renameOutputFile(Path pumlFile, Path resultDir) throws IOException {
        String fileName = pumlFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        Path source = resultDir.resolve(baseName + ".utxt");
        Path target = resultDir.resolve(baseName + OUTPUT_SUFFIX);

        if (Files.exists(source)) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log("Файл переименован: " + target.getFileName());
        }
    }

    private void deleteTempFile(Path pumlFile, Path resultDir) throws IOException {
        String fileName = pumlFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        Path tempFile = resultDir.resolve(baseName + "_unicode_ascii_result.txt");

        if (Files.exists(tempFile)) {
            Files.delete(tempFile);
            log("Удален временный файл: " + tempFile.getFileName());
        }
    }

    private Path createResultDir(Path parentDir) throws IOException {
        Path resultDir = parentDir.resolve(RESULT_DIR);
        if (!Files.exists(resultDir)) {
            Files.createDirectories(resultDir);
        }
        return resultDir;
    }

    private void setupLogger() throws IOException {
        if (logger == null) {
            Path logDir = baseDir.resolve(RESULT_DIR);
            Files.createDirectories(logDir);
            Path logFile = logDir.resolve(LOG_FILE);
            logger = new PrintWriter(new FileWriter(logFile.toFile(), true), true);
        }
    }

    private void log(String message) {
        String entry = "[" + LocalDateTime.now() + "] " + message;
        if (outputLogEnabled || message.contains("ОШИБКА")) {
            try {
                setupLogger(); // Создаем логгер, если он еще не создан
                logger.println(entry);
            } catch (IOException e) {
                System.err.println("Не удалось записать в лог: " + e.getMessage());
            }
        }
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
