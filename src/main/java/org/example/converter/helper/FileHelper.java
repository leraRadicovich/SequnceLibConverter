package org.example.converter.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileHelper {

    public static File createOutputFile(File input) {
        String name = input.getName().replace(".puml", "_byLib.puml");
        return new File(input.getParentFile(), name);
    }

    public static List<File> collectFiles(File path) {
        List<File> files = new ArrayList<>();
        if (path.isDirectory()) {
            File[] found = path.listFiles(f ->
                    f.getName().endsWith(".puml") && !isAlreadyProcessed(f));
            if (found != null) Collections.addAll(files, found);
        } else if (path.isFile() && !isAlreadyProcessed(path)) {
            files.add(path);
        }
        return files;
    }

    private static boolean isAlreadyProcessed(File f) {
        return f.getName().toLowerCase().endsWith("_bylib.puml");
    }
}