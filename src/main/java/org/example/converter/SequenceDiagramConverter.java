package org.example.converter;

import org.example.converter.helper.FileHelper;
import org.example.converter.helper.model.Box;
import org.example.converter.helper.model.Participant;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;

import static org.example.constant.PatternStatic.*;

public class SequenceDiagramConverter {

    public void run(Path inputPath, Path outputDir) throws IOException {
        System.setProperty("conversion.output.dir", outputDir.toString());
        runWithPath(inputPath.toFile());
    }

    public static void main(String[] args) {
        try {
            String pathString;
            if (args.length == 0) {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter path to .puml file or folder: ");
                pathString = scanner.nextLine().trim();
            } else {
                pathString = args[0];
            }

            Path inputPath = Path.of(pathString);
            Path outputDir = inputPath.getParent() != null ? inputPath.getParent() : Path.of(".");
            new SequenceDiagramConverter().run(inputPath, outputDir);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void runWithPath(File path) {
        List<File> inputFiles = FileHelper.collectFiles(path);
        for (File inputFile : inputFiles) {
            runWithFile(inputFile);
        }
    }

    public static void runWithFile(File inputFile) {
        if (isAlreadyProcessed(inputFile)) {
            System.out.println("Skipping: " + inputFile.getName());
            return;
        }

        File outputFile = FileHelper.createOutputFile(inputFile);
        try {
            processPumlFile(inputFile, outputFile);
            System.out.println("Processed: " + inputFile.getName());
        } catch (IOException e) {
            System.err.println("Error processing file: " + inputFile.getName() + ": " + e.getMessage());
        }
    }

    private static void processPumlFile(File inputFile, File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            boolean umlSectionStarted = false;
            String title = inputFile.getName().replace(".puml", "");
            boolean inSkinparamBlock = false;
            boolean inBox = false;
            Box currentBox = null;
            List<Participant> boxParticipants = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (inSkinparamBlock) {
                    if (line.equals("}")) inSkinparamBlock = false;
                    continue;
                } else if (line.startsWith("skinparam")) {
                    if (line.contains("{")) inSkinparamBlock = true;
                    continue;
                } else if (line.startsWith("autonumber")) {
                    continue;
                }

                Matcher activateMatcher = ACTIVATE_PATTERN.matcher(line);
                if (activateMatcher.matches()) {
                    writer.write("ACTIVATE(" + activateMatcher.group(1) + ")\n");
                    continue;
                }

                Matcher deactivateMatcher = DEACTIVATE_PATTERN.matcher(line);
                if (deactivateMatcher.matches()) {
                    writer.write("DEACTIVATE(" + deactivateMatcher.group(1) + ")\n");
                    continue;
                }

                Matcher dividerMatcher = DIVIDER_PATTERN.matcher(line);
                if (dividerMatcher.matches()) {
                    writer.write("DEVIDER(\"" + dividerMatcher.group(1) + "\")\n");
                    continue;
                }

                String groupLine = processGroup(line);
                if (groupLine != null) {
                    writer.write(groupLine + "\n");
                    continue;
                }

                if (line.equals("end")) {
                    writer.write("END()\n");
                    continue;
                }

                Matcher boxMatcher = BOX_START_PATTERN.matcher(line);
                if (boxMatcher.matches()) {
                    inBox = true;
                    currentBox = new Box(boxMatcher.group(1), boxMatcher.group(2));
                    continue;
                }

                if (line.equals("end box")) {
                    writeBoxContent(writer, currentBox, boxParticipants);
                    inBox = false;
                    boxParticipants.clear();
                    continue;
                }

                line = processParticipantColor(line);
                Participant participant = processParticipant(line);
                if (participant != null) {
                    if (inBox) {
                        boxParticipants.add(participant);
                    } else {
                        writer.write(participant.toPartiesString() + "\n");
                    }
                    continue;
                }

                String arrowLine = processArrow(line);
                if (arrowLine != null) {
                    writer.write(arrowLine + "\n");
                    continue;
                }

                if (!umlSectionStarted && line.equals("@startuml")) {
                    umlSectionStarted = true;
                    writer.write("@startuml\n");
                    writer.write("!include path/to/SequenceLibIncludeFile.puml\n");
                    writer.write("diagramInit(draft, \"" + title + "\")\n");
                    continue;
                }

                writer.write(line + "\n");
            }
        }
    }

    private static String processGroup(String line) {
        Matcher matcher = GROUP_PATTERN.matcher(line);
        if (!matcher.matches()) return null;

        String type = matcher.group(1).toUpperCase();
        String rest = matcher.group(2).trim();

        String color = "";
        String text = rest;

        Matcher colorMatcher = GROUP_COLOR_PATTERN.matcher(rest);
        if (colorMatcher.find()) {
            color = colorMatcher.group(1) != null ?
                    colorMatcher.group(1).replace("#", "") :
                    colorMatcher.group(2);

            text = rest.substring(colorMatcher.end()).trim();
        }

        return String.format("%s(%s, \"%s\")", type, color, text.replace("\"", "\\\""));
    }

    private static String processParticipantColor(String line) {
        return PARTICIPANT_COLOR_PATTERN.matcher(line).replaceAll("");
    }

    private static Participant processParticipant(String line) {
        Matcher matcher = PARTICIPANT_PATTERN.matcher(line);
        if (!matcher.matches()) return null;

        return new Participant(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                matcher.group(4)
        );
    }

    private static String processArrow(String line) {
        Matcher matcher = ARROW_PATTERN.matcher(line);
        if (!matcher.matches()) return null;

        String left = matcher.group(1);
        String arrow = matcher.group(2);
        String right = matcher.group(3);
        String operators = matcher.group(4);
        String text = matcher.group(5).replaceAll("\"", "");

        boolean isReverse = arrow.startsWith("<");
        String procType = arrow.contains("--") ? "rs" : "rq";
        String from = isReverse ? right : left;
        String to = isReverse ? left : right;

        return String.format("%s(%s, %s, \"%s\", \"%s\", \"\")", procType, from, to, operators, text);
    }

    private static void writeBoxContent(BufferedWriter writer, Box box, List<Participant> participants) throws IOException {
        for (Participant p : participants) {
            writer.write(p.toPartiesString() + "\n");
        }

        StringBuilder participantsList = new StringBuilder();
        for (Participant p : participants) {
            participantsList.append(p.aliasOrName()).append(",");
        }

        if (participantsList.length() > 0) {
            participantsList.deleteCharAt(participantsList.length() - 1);
        }

        writer.write(String.format("BOX(\"%s\", %s, \"%s\")\n", box.name(), box.color(), participantsList));
    }

    private static boolean isAlreadyProcessed(File file) {
        return file.getName().toLowerCase().endsWith("_bylib.puml");
    }
}