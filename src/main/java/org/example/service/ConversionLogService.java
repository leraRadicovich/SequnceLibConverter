package org.example.service;

import org.example.config.ConversionConfig;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сервис для логирования операций конвертации
 */
public class ConversionLogService {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    
    /**
     * Формирует строку с параметрами запуска операции
     */
    public String formatOperationParameters(String operationType, Path inputPath, ConversionConfig config, boolean loggingEnabled) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append(String.format("Операция: %s\n", operationType));
        sb.append(String.format("Время запуска: %s\n", LocalDateTime.now().format(FORMATTER)));
        sb.append(String.format("Входной путь: %s\n", inputPath));
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("Параметры конвертации:\n");
        sb.append(String.format("  • Применить локальную библиотеку: %s\n", config.applyLocalLib() ? "Да" : "Нет"));
        sb.append(String.format("  • Обновить локальную библиотеку: %s\n", config.updateLocalLib() ? "Да" : "Нет"));
        sb.append(String.format("  • Путь библиотеки: %s\n", config.libDirectory() != null ? config.libDirectory() : "не указан"));
        sb.append(String.format("  • Логирование: %s\n", loggingEnabled ? "Включено" : "Выключено"));
        sb.append("═══════════════════════════════════════════════════════════\n");
        return sb.toString();
    }
    
    /**
     * Формирует сообщение о завершении операции
     */
    public String formatCompletionMessage(String operationType, long durationSeconds) {
        return String.format("Операция %s завершена за %d сек. Время завершения: %s\n",
                operationType, durationSeconds, LocalDateTime.now().format(FORMATTER));
    }
}

