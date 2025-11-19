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
import java.nio.file.Paths; // Добавленный импорт

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

        JButton convertButton = new JButton("Конвертировать (origin → libSyntax)");
        JButton deconvertButton = new JButton("Деконвертировать (libSyntax → origin)");
        JCheckBox applyLocalLib = new JCheckBox("Применить локальную библиотеку");
        JCheckBox updateLocalLib = new JCheckBox("Обновить локальную библиотеку");
        JCheckBox enableLogging = new JCheckBox("Включить логирование"); // Новый чекбокс для логирования
        JTextField libPathField = new JTextField(System.getProperty("user.home") + "/Documents/PlantUML_sequenceLib");
        JTextField pathField = new JTextField(); // Поле ввода пути

        JPanel topPanel = new JPanel(new GridLayout(8, 1)); // Изменено на 8 строк для нового поля ввода пути
        topPanel.add(applyLocalLib);
        topPanel.add(updateLocalLib);
        topPanel.add(enableLogging); // Добавляем новый чекбокс
        topPanel.add(new JLabel("Путь до локальной библиотеки:"));
        topPanel.add(libPathField);

        // Создаем горизонтальный лэйаут для кнопки и поля ввода пути
        JPanel pathPanel = new JPanel(new BorderLayout());
        JButton fileChooserButton = new JButton("Выберите файл/директорию или укажите путь вручную");
        pathPanel.add(fileChooserButton, BorderLayout.WEST);
        pathPanel.add(pathField, BorderLayout.CENTER);

        topPanel.add(pathPanel); // Горизонтальный лэйаут с кнопкой и полем ввода пути

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
                pathField.setText(file.getAbsolutePath()); // Устанавливаем выбранный путь в поле ввода
            }
        });

        convertButton.addActionListener(e -> {
            handleConvert(true, pathField.getText(), applyLocalLib.isSelected(), updateLocalLib.isSelected(), enableLogging.isSelected(), libPathField.getText(), logArea);
        });

        deconvertButton.addActionListener(e -> {
            handleConvert(false, pathField.getText(), applyLocalLib.isSelected(), updateLocalLib.isSelected(), enableLogging.isSelected(), libPathField.getText(), logArea);
        });

        frame.setVisible(true);
    }

    private static void handleConvert(boolean isConvert, String pathText, boolean apply, boolean update, boolean outputLogEnabled, String libPath, JTextArea logArea) {
        try {
            Path inputPath = getPathFromText(pathText, logArea);
            if (inputPath == null) {
                return;
            }

            Path resultPath = inputPath.getParent().resolve("result");
            Files.createDirectories(resultPath);

            ConversionConfig config = new ConversionConfig(apply, update, libPath);

            if (isConvert) {
                new SequenceDiagramConverter().run(inputPath, resultPath, config);
            } else {
                try (FileProcessor processor = new FileProcessor(inputPath.getParent(), outputLogEnabled)) {
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

    private static Path getPathFromText(String pathText, JTextArea logArea) {
        if (pathText == null || pathText.isBlank()) {
            logArea.setText("Ошибка: путь не указан");
            return null;
        }

        Path inputPath = Paths.get(pathText).toAbsolutePath();
        if (!Files.exists(inputPath)) {
            logArea.setText("Ошибка: путь не существует - " + inputPath);
            return null;
        }

        return inputPath;
    }
}
