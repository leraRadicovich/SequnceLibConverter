package org.example.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import org.example.converter.SequenceDiagramConverter;
import org.example.config.ConversionConfig;
import org.example.deconverter.FileProcessor; // Импорт класса FileProcessor
import org.example.utils.FileZipper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Route("")
public class MainView extends VerticalLayout {

    private final TextField pathField = new TextField("Путь к файлу или папке (.puml)");
    private final TextField libPathField = new TextField("Путь до локальной библиотеки (по умолчанию: ~/Documents/PlantUML_sequenceLib)");
    private final Checkbox applyLocalLib = new Checkbox("Применить локальную библиотеку");
    private final Checkbox updateLocalLib = new Checkbox("Обновить локальную библиотеку");
    private final TextArea logArea = new TextArea("Processing Log");
    private final Anchor downloadLink = new Anchor();

    private byte[] uploadedBytes;
    private String uploadedFilename;

    public MainView() {
        setPadding(true);
        setSpacing(true);

        Label instruction = new Label("Загрузите .puml для конвертации или деконвертации");

        pathField.setPlaceholder("или введите путь вручную...");
        pathField.setWidthFull();

        libPathField.setPlaceholder("~/Documents/PlantUML_sequenceLib");
        libPathField.setWidthFull();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".puml", ".txt");
        upload.addSucceededListener(event -> {
            try (InputStream input = buffer.getInputStream()) {
                uploadedBytes = input.readAllBytes();
                uploadedFilename = event.getFileName();
                logArea.setValue("Файл загружен: " + uploadedFilename);
                downloadLink.setVisible(false);
            } catch (IOException e) {
                logArea.setValue("Ошибка при загрузке файла: " + e.getMessage());
            }
        });

        Button convertButton = new Button("Конвертировать (PUML → ASCII)");
        Button deconvertButton = new Button("Деконвертировать (ASCII → PUML)");

        convertButton.addClickListener(event -> handleConvert(true));
        deconvertButton.addClickListener(event -> handleConvert(false));

        logArea.setReadOnly(true);
        logArea.setWidthFull();
        logArea.setHeight("300px");

        downloadLink.setText("Скачать ZIP");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.setVisible(false);

        add(instruction, pathField, upload, applyLocalLib, updateLocalLib, libPathField,
                new Hr(), new HorizontalLayout(convertButton, deconvertButton),
                logArea, new Hr(), downloadLink);
    }

    private void handleConvert(boolean isConvert) {
        try {
            Path inputPath;
            String manualPath = pathField.getValue();
            boolean useManualPath = manualPath != null && !manualPath.isBlank();

            if (useManualPath) {
                inputPath = Path.of(manualPath).toAbsolutePath();
                if (!Files.exists(inputPath)) {
                    logArea.setValue("Ошибка: путь не существует - " + inputPath);
                    return;
                }
            } else {
                if (uploadedBytes == null || uploadedFilename == null) {
                    logArea.setValue("Сначала загрузите файл.");
                    return;
                }
                File inputFile = File.createTempFile("upload_", "_" + uploadedFilename);
                try (OutputStream os = new FileOutputStream(inputFile)) {
                    os.write(uploadedBytes);
                }
                inputPath = inputFile.toPath();
            }

            Path resultPath = inputPath.getParent().resolve("result");
            Files.createDirectories(resultPath);
            Files.deleteIfExists(resultPath.resolve("processing.log"));

            File logFile = resultPath.resolve("processing.log").toFile();
            PrintStream logStream = new PrintStream(new FileOutputStream(logFile));
            System.setOut(logStream);
            System.setErr(logStream);

            String libDir = libPathField.getValue();
            if (libDir == null || libDir.isBlank()) {
                libDir = System.getProperty("user.home") + "/Documents/PlantUML_sequenceLib";
            }

            ConversionConfig config = new ConversionConfig(
                    applyLocalLib.getValue(),
                    updateLocalLib.getValue(),
                    libDir
            );

            if (isConvert) {
                new SequenceDiagramConverter().run(inputPath, resultPath, config);
            } else {
                try (FileProcessor processor = new FileProcessor(inputPath.getParent(), updateLocalLib.getValue())) {
                    processor.process(inputPath);
                }
            }

            logStream.close();

            if (logFile.exists()) {
                logArea.setValue(Files.readString(logFile.toPath()));
            }

            File zipFile = FileZipper.zipDirectory(resultPath.toFile());
            if (zipFile != null && zipFile.length() > 0) {
                downloadLink.setHref("/download/" + zipFile.getName());
                downloadLink.setText("Скачать ZIP");
                downloadLink.setVisible(true);
            } else {
                downloadLink.setHref("");
                downloadLink.setText("ZIP пустой, скачать нельзя");
                downloadLink.setVisible(false);
            }

        } catch (Exception e) {
            logArea.setValue("Ошибка обработки: " + e.getMessage());
        }
    }
}