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
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class SwingEntryPoint {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingEntryPoint::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("PlantUML Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500); // Увеличиваем размер окна
        frame.setLayout(new BorderLayout());

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JButton clearLogButton = new JButton("Очистить лог");

        JButton convertButton = new JButton("Конвертировать (origin → libSyntax)");
        JButton deconvertButton = new JButton("Деконвертировать (libSyntax → origin)");
        JCheckBox applyLocalLib = new JCheckBox("Применить локальную библиотеку");
        JCheckBox updateLocalLib = new JCheckBox("Обновить локальную библиотеку");
        JCheckBox enableLogging = new JCheckBox("Включить логирование");
        JTextField libPathField = new JTextField(System.getProperty("user.home") + "/Documents/PlantUML_sequenceLibv1");
        JTextField pathField = new JTextField();

        JPanel topPanel = new JPanel(new GridLayout(6, 1)); // Уменьшаем количество строк на 1
        topPanel.add(applyLocalLib);
        topPanel.add(updateLocalLib);
        topPanel.add(enableLogging);

        // Панель для выбора пути до локальной библиотеки
        JPanel libPathPanel = new JPanel(new BorderLayout());
        JButton libPathButton = new JButton("Путь до локальной библиотеки"); // Кнопка с текстом "Путь до локальной библиотеки"
        libPathButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                libPathField.setText(file.getAbsolutePath());
            }
        });
        libPathPanel.add(libPathButton, BorderLayout.WEST); // Кнопка слева
        libPathPanel.add(libPathField, BorderLayout.CENTER); // Поле ввода справа

        topPanel.add(libPathPanel);

        JPanel pathPanel = new JPanel(new BorderLayout());
        JButton fileChooserButton = new JButton("Путь до файла/директории");
        pathPanel.add(fileChooserButton, BorderLayout.WEST);
        pathPanel.add(pathField, BorderLayout.CENTER);

        topPanel.add(pathPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deconvertButton);
        buttonPanel.add(convertButton);
        buttonPanel.add(clearLogButton); // Добавляем кнопку очистки лога в ту же панель

        // Создание заголовка для области вывода
        JLabel resultLabel = new JLabel("Результат конвертации");

        // Добавление заголовка и области вывода в центральную панель
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(resultLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER); // Добавляем панель с заголовком и логом в центральную часть окна
        frame.add(buttonPanel, BorderLayout.SOUTH); // Добавляем панель с кнопками в нижнюю часть окна

        clearLogButton.addActionListener(e -> logArea.setText(""));

        final Path[] selectedPath = new Path[1];

        fileChooserButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new FileNameExtensionFilter("PUML Files", "puml"));
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                selectedPath[0] = file.toPath();
                logArea.append("Выбран путь: " + file.getAbsolutePath() + "\n"); // Изменяем на append
                pathField.setText(file.getAbsolutePath());
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

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            LocalDateTime startTime = LocalDateTime.now();
            String operationType = isConvert ? "конвертация" : "деконвертация";

            // Добавление разделителя между новыми запусками
            logArea.append("\n----------------------------------------\n");

            // Запись о начале операции
            logArea.append(String.format("Операция %s началась в %s%n", operationType, startTime.format(formatter)));
            logArea.append(String.format("Путь до конвертируемого объекта: %s%n", inputPath));

            long startTimeMillis = System.currentTimeMillis();

            if (isConvert) {
                SequenceDiagramConverter converter = new SequenceDiagramConverter();
                converter.run(inputPath, resultPath, config);
            } else {
                try (FileProcessor processor = new FileProcessor(inputPath.getParent(), outputLogEnabled)) {
                    processor.process(inputPath);
                }
            }

            long endTimeMillis = System.currentTimeMillis();
            long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(endTimeMillis - startTimeMillis);
            LocalDateTime endTime = LocalDateTime.now();

            // Запись о завершении операции
            logArea.append(String.format("Операция %s завершилась в %s%n", operationType, endTime.format(formatter)));
            logArea.append(String.format("Время %s заняло %d сек.%n", operationType, durationSeconds));

            Path logFile = resultPath.resolve("processing.log");
            if (Files.exists(logFile)) {
                logArea.append(Files.readString(logFile));
            } else {
                logArea.append("Лог не найден.");
            }
        } catch (Exception ex) {
            logArea.append("Ошибка: " + ex.getMessage() + "\n"); // Изменяем на append
        }
    }

    private static Path getPathFromText(String pathText, JTextArea logArea) {
        if (pathText == null || pathText.isBlank()) {
            logArea.append("Ошибка: путь не указан\n"); // Изменяем на append
            return null;
        }

        Path inputPath = Paths.get(pathText).toAbsolutePath();
        if (!Files.exists(inputPath)) {
            logArea.append("Ошибка: путь не существует - " + inputPath + "\n"); // Изменяем на append
            return null;
        }

        return inputPath;
    }
}
