package org.example.deconverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultParser {
    private static final Pattern DIAGRAM_CODE_PATTERN = Pattern.compile("(@startuml.*?@enduml)", Pattern.DOTALL);
    private static final Pattern LEGEND_BLOCK_PATTERN = Pattern.compile("(?s)legend(.*?)end legend");

    public static String extractDiagramCode(String asciiContent) {
        Matcher matcher = DIAGRAM_CODE_PATTERN.matcher(asciiContent);
        if (matcher.find()) {
            return removeLegendBlock(matcher.group(1));
        }
        return "";
    }

    public static String extractLegendContent(String asciiContent) {
        Matcher matcher = LEGEND_BLOCK_PATTERN.matcher(asciiContent);
        if (matcher.find()) {
            return cleanLegendBlock(matcher.group(1));
        }
        return "";
    }

    private static String removeLegendBlock(String diagramBlock) {
        return diagramBlock.replaceAll("(?s)\\blegend\\b.*?\\bend legend\\b", "")
                .replaceAll("║", "")
                .replaceAll("\\s+\\n", "\n")
                .replaceAll("░", "")
                .trim();
    }

    private static String cleanLegendBlock(String block) {
        return block.replaceAll("║", "")
                .replaceAll("═+", "")
                .replaceAll("\\s+\\n", "\n")
                .replaceAll("^Карта процесса.*?====\\s*", "")
                .trim();
    }
}