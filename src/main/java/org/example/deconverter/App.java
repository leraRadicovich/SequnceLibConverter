package org.example.deconverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Path inputPath = resolveInputPath(args);
        run(inputPath);
    }

    public static void run(Path inputPath) {
        Path baseDir = inputPath.getParent() != null ? inputPath.getParent() : Paths.get("");
        try (FileProcessor processor = new FileProcessor(baseDir)) {
            processor.process(inputPath);
        } catch (Exception e) {
            System.err.println("Critical error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Path resolveInputPath(String[] args) {
        if (args.length >= 1) {
            return Paths.get(args[0]).toAbsolutePath();
        }

        // Интерактивный режим
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Введите путь к файлу или папке с диаграммами (.puml или .txt):");
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                Path path = Paths.get(input).toAbsolutePath();
                if (Files.exists(path)) {
                    return path;
                } else {
                    System.err.println("Ошибка: путь не существует - " + path);
                }
            } else {
                System.err.println("Путь не может быть пустым");
            }
        }
    }
}