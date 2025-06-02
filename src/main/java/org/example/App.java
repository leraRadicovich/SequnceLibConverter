package org.example;

import java.nio.file.Files; // Добавляем импорт для Files
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Path inputPath = getInputPath(args);
        runWithPath(inputPath);
        Path baseDir = inputPath.getParent() != null ?
                inputPath.getParent() : Paths.get("");

        try (FileProcessor processor = new FileProcessor(baseDir)) {
            processor.process(inputPath);
        } catch (Exception e) {
            System.err.println("Critical error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Path getInputPath(String[] args) {
        if (args.length >= 1) {
            return Paths.get(args[0]).toAbsolutePath();
        }

        // Интерактивный запрос пути у пользователя
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Введите путь к файлу или папке с диаграммами (.puml):");
            System.out.print("> ");
            String path = scanner.nextLine().trim();

            if (!path.isEmpty()) {
                Path inputPath = Paths.get(path).toAbsolutePath();

                if (Files.exists(inputPath)) { // Здесь используется Files.exists
                    return inputPath;
                }

                System.err.println("Ошибка: путь не существует - " + inputPath);
            }

            System.err.println("Пожалуйста, введите корректный путь.");
        }
    }
    public static void runWithPath(Path inputPath) {
        Path baseDir = inputPath.getParent() != null ?
                inputPath.getParent() : Paths.get("");

        try (FileProcessor processor = new FileProcessor(baseDir)) {
            processor.process(inputPath);
        } catch (Exception e) {
            System.err.println("Critical error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}