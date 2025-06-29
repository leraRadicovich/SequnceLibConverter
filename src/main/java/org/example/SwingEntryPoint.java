package org.example;

import org.example.converter.SequenceDiagramConverter;
import org.example.config.ConversionConfig;
import org.example.deconverter.FileProcessor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SwingEntryPoint {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingEntryPoint::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("PlantUML Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JButton convertButton = new JButton("Конвертировать (PUML → ASCII)");
        JButton deconvertButton = new JButton("Деконвертировать (ASCII → PUML)");
        JCheckBox applyLocalLib = new JCheckBox("Применить локальную библиотеку");
        JCheckBox updateLocalLib = new JCheckBox("Обновить локальную библиотеку");
        JTextField libPathField = new JTextField(System.getProperty("user.home") + "/Documents/PlantUML_sequenceLib");

        JPanel topPanel = new JPanel(new GridLayout(5, 1));
        topPanel.add(applyLocalLib);
        topPanel.add(updateLocalLib);
        topPanel.add(new JLabel("Путь до локальной библиотеки:"));
        topPanel.add(libPathField);

        JButton fileChooserButton = new JButton("Выбрать файл или директорию");
        topPanel.add(fileChooserButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(convertButton);
        buttonPanel.add(deconvertButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        final Path[] selectedPath = new Path[1];

        fileChooserButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new FileNameExtensionFilter("PUML Files", "puml"));
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                selectedPath[0] = file.toPath();
                logArea.setText("Выбран путь: " + file.getAbsolutePath());
            }
        });

        convertButton.addActionListener(e -> {
            handleConvert(true, selectedPath[0], applyLocalLib.isSelected(), updateLocalLib.isSelected(), libPathField.getText(), logArea);
        });

        deconvertButton.addActionListener(e -> {
            handleConvert(false, selectedPath[0], applyLocalLib.isSelected(), updateLocalLib.isSelected(), libPathField.getText(), logArea);
        });

        frame.setVisible(true);
    }

    private static void handleConvert(boolean isConvert, Path inputPath, boolean apply, boolean update, String libPath, JTextArea logArea) {
        try {
            if (inputPath == null || !Files.exists(inputPath)) {
                logArea.setText("Ошибка: путь не выбран или не существует");
                return;
            }

            Path resultPath = inputPath.getParent().resolve("result");
            Files.createDirectories(resultPath);

            ConversionConfig config = new ConversionConfig(apply, update, libPath);

            if (isConvert) {
                new SequenceDiagramConverter().run(inputPath, resultPath, config);
            } else {
                try (FileProcessor processor = new FileProcessor(inputPath.getParent())) {
                    processor.process(inputPath);
                }
            }

            Path logFile = resultPath.resolve("processing.log");
            if (Files.exists(logFile)) {
                logArea.setText(Files.readString(logFile));
            } else {
                logArea.setText("Конвертация завершена. Лог не найден.");
            }
        } catch (Exception ex) {
            logArea.setText("Ошибка: " + ex.getMessage());
        }
    }
}
