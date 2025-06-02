package org.example.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import org.example.converter.SequenceDiagramConverter;
import org.example.deconverter.FileProcessor;
import org.example.utils.FileZipper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Route("")
public class MainView extends VerticalLayout {

    private final TextArea logArea = new TextArea("Processing Log");
    private final Anchor downloadLink = new Anchor();
    private byte[] uploadedBytes;
    private String uploadedFilename;

    public MainView() {
        setPadding(true);
        setSpacing(true);

        Label instruction = new Label("Загрузите .puml для конвертации или деконвертации");

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

        add(instruction, upload, new Hr(), new HorizontalLayout(convertButton, deconvertButton), logArea, new Hr(), downloadLink);
    }

    private void handleConvert(boolean isConvert) {
        if (uploadedBytes == null || uploadedFilename == null) {
            logArea.setValue("Сначала загрузите файл.");
            return;
        }

        try {
            // Сохраняем загруженный файл
            File inputFile = File.createTempFile("upload_", "_" + uploadedFilename);
            try (OutputStream os = new FileOutputStream(inputFile)) {
                os.write(uploadedBytes);
            }

            // Создаём временную директорию
            String resultDir = System.getProperty("java.io.tmpdir") + "/result_" + UUID.randomUUID();
            Path resultPath = Path.of(resultDir);
            Files.createDirectories(resultPath);

            // Лог-файл
            File logFile = resultPath.resolve("processing.log").toFile();
            PrintStream logStream = new PrintStream(new FileOutputStream(logFile));
            System.setOut(logStream);
            System.setErr(logStream);

            // Обработка
            if (isConvert) {
                SequenceDiagramConverter converter = new SequenceDiagramConverter();
                converter.run(inputFile.toPath(), resultPath);
            } else {
                FileProcessor processor = new FileProcessor(resultPath);
                processor.process(inputFile.toPath());
            }

            logStream.close();

            // Показываем лог в UI
            if (logFile.exists()) {
                logArea.setValue(Files.readString(logFile.toPath()));
            }

            // Архивируем
            File zipFile = FileZipper.zipDirectory(resultPath.toFile());
            if (zipFile != null && zipFile.length() > 0) {
                downloadLink.setHref("/download/" + zipFile.getName()); // 👈 совпадает с контроллером
                downloadLink.getElement().setAttribute("download", true);
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