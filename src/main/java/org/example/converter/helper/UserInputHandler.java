package org.example.converter.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class UserInputHandler {
    public static List<File> getInputFiles() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter path: ");
        File path = new File(scanner.nextLine().trim());

        List<File> files = new ArrayList<>();

        if (path.isDirectory()) {
            File[] found = path.listFiles(f ->
                    f.getName().endsWith(".puml") && !isAlreadyProcessed(f));
            if (found != null) Collections.addAll(files, found);
        } else if (path.isFile() && !isAlreadyProcessed(path)) {
            files.add(path);
        }

        if (files.isEmpty()) throw new IOException("No files to process");
        return files;
    }

    private static boolean isAlreadyProcessed(File f) {
        return f.getName().toLowerCase().endsWith("_bylib.puml");
    }
}