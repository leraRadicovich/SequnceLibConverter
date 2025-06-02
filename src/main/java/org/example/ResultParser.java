package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultParser {
    // Регулярные выражения для извлечения блоков из ASCII-арта
    private static final Pattern DIAGRAM_CODE_PATTERN = Pattern.compile("(@startuml.*?@enduml)", Pattern.DOTALL);
    private static final Pattern LEGEND_BLOCK_PATTERN = Pattern.compile("(?s)legend(.*?)end legend");

    // Метод для извлечения исходного кода диаграммы из ASCII-арта
    public static String extractDiagramCode(String asciiContent) {
        Matcher matcher = DIAGRAM_CODE_PATTERN.matcher(asciiContent);
        if (matcher.find()) {
            String diagramBlock = matcher.group(1);
            return removeLegendBlock(diagramBlock);
        }
        return "";
    }

    // Метод для извлечения блока легенды из ASCII-арта
    public static String extractLegendContent(String asciiContent) {
        Matcher matcher = LEGEND_BLOCK_PATTERN.matcher(asciiContent);
        if (matcher.find()) {
            return cleanLegendBlock(matcher.group(1));
        }
        return "";
    }

    // Удаление блока legend из кода диаграммы
    private static String removeLegendBlock(String diagramBlock) {
        return diagramBlock.replaceAll("(?s)\\blegend\\b.*?\\bend legend\\b", "")
                .replaceAll("║", "")
                .replaceAll("\\s+\\n", "\n")
                .replaceAll("░", "")
                .trim();
    }

    // Очистка блока легенды
    private static String cleanLegendBlock(String block) {
        return block.replaceAll("║", "")
                .replaceAll("═+", "")
                .replaceAll("\\s+\\n", "\n")
                .replaceAll("^Карта процесса.*?====\\s*", "") // Убираем заголовки
                .trim();
    }

    // Метод для сохранения разделенных файлов
    public static void saveSplitResults(Path asciiFile, Path resultDir, String baseName) throws IOException {
        // Чтение содержимого ASCII-арт файла
        String asciiContent = Files.readString(asciiFile);

        // Извлечение компонентов
        String diagramCode = extractDiagramCode(asciiContent);
        String legendContent = extractLegendContent(asciiContent);

        // Сохранение исходного кода диаграммы (без легенды)
        if (!diagramCode.isEmpty()) {
            Path diagramFile = resultDir.resolve(baseName + "_original.puml");
            Files.writeString(diagramFile, diagramCode);
        }

        // Сохранение таблиц процесса
        if (!legendContent.isEmpty()) {
            Path tablesFile = resultDir.resolve(baseName + "_карта процесса.md");
            Files.writeString(tablesFile, legendContent);
        }
    }
}