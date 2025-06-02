package org.example.constant;

import java.util.regex.Pattern;

public interface PatternStatic {
    Pattern PARTICIPANT_PATTERN = Pattern.compile(
            "^(actor|participant|database|queue)\\s+\"([^\"]+)\"(?:\\s+as\\s+(\\w+))?(?:\\s+order\\s+(\\d+))?.*"
    );

     Pattern BOX_START_PATTERN = Pattern.compile(
            "^box\\s+\"([^\"]+)\"(?:\\s+#(\\w+))?.*"
    );

     Pattern ARROW_PATTERN = Pattern.compile(
            "^\\s*([a-zA-Z0-9_]+)\\s*([<]?--?[>]?)\\s*([a-zA-Z0-9_]+)\\s*([+\\-]*)\\s*:\\s*(.+?)\\s*$"
    );

     Pattern GROUP_PATTERN = Pattern.compile(
            "^(alt|opt|par|group|else)\\b\\s*(.*?)\\s*$"
    );

     Pattern GROUP_COLOR_PATTERN = Pattern.compile(
            "(#\\w+)|%\\w+\\((\\w+),\\s*-?\\d+\\)"
    );

     Pattern PARTICIPANT_COLOR_PATTERN = Pattern.compile(
            "\\s*#\\w+\\s*$"
    );

    // Новые паттерны для активации/деактивации и разделителей
     Pattern ACTIVATE_PATTERN = Pattern.compile(
            "^(?:activate\\s+)?(\\w+)\\s*\\+\\+\\s*$"
    );

     Pattern DEACTIVATE_PATTERN = Pattern.compile(
            "^(?:deactivate\\s+)?(\\w+)\\s*--\\s*$"
    );

     Pattern DIVIDER_PATTERN = Pattern.compile(
            "^==\\s*(.+?)\\s*==\\s*$"
    );
}
