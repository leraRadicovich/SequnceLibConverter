package org.example.deconverter;

import org.example.config.ConversionConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        // Получение пути к входному файлу или директории
        Path inputPath = getInputPath(args);

        // Получение флага для сохранения лога
        boolean outputLogEnabled = getOutputLogEnabled(args);

        // Создаем конфигурацию с дефолтными значениями для консольного приложения
        ConversionConfig config = new ConversionConfig(false, false, null);

        try (FileProcessor processor = new FileProcessor(inputPath.getParent(), outputLogEnabled, config)) {
            processor.process(inputPath);
        } catch (Exception e) {
            System.err.println("Ошибка при обработке: " + e.getMessage());
        }
    }

    private static Path getInputPath(String[] args) {
        if (args.length > 0 && !args[0].equalsIgnoreCase("--log") && !args[0].equalsIgnoreCase("--nolog")) {
            Path inputPath = Paths.get(args[0]).toAbsolutePath();
            if (Files.exists(inputPath)) {
                return inputPath;
            }
            System.err.println("Ошибка: путь не существует - " + inputPath);
        }

        // Интерактивный запрос пути у пользователя
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Введите путь к файлу или папке с диаграммами (.puml):");
                System.out.print("> ");
                String path = scanner.nextLine().trim();

                if (!path.isEmpty()) {
                    Path inputPath = Paths.get(path).toAbsolutePath();

                    if (Files.exists(inputPath)) {
                        return inputPath;
                    }

                    System.err.println("Ошибка: путь не существует - " + inputPath);
                }

                System.err.println("Пожалуйста, введите корректный путь.");
            }
        }
    }

    private static boolean getOutputLogEnabled(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--log")) {
                return true;
            } else if (arg.equalsIgnoreCase("--nolog")) {
                return false;
            }
        }
        // По умолчанию возвращаем false
        return false;
    }
}
