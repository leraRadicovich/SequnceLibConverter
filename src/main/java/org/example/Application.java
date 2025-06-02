package org.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Route("")
    public static class MainView extends VerticalLayout {

        private final MemoryBuffer buffer;
        private final Upload upload;
        private final Button convertButton;
        private final Button deconvertButton;

        public MainView() {
            setAlignItems(Alignment.CENTER);

            H2 title = new H2("Выберите .puml файл и действие");
            buffer = new MemoryBuffer();
            upload = new Upload(buffer);
            upload.setAcceptedFileTypes(".puml");

            convertButton = new Button("Конвертировать");
            deconvertButton = new Button("Деконвертировать");

            convertButton.addClickListener(e -> handleConvert());
            deconvertButton.addClickListener(e -> handleDeconvert());

            add(title, upload, convertButton, deconvertButton);
        }

        private void handleConvert() {
            try {
                File temp = saveTempFile();
                SequenceDiagramConverter.main(new String[]{temp.getAbsolutePath()});
                Notification.show("Файл конвертирован");
            } catch (Exception ex) {
                Notification.show("Ошибка: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        }

        private void handleDeconvert() {
            try {
                File temp = saveTempFile();
                App.main(new String[]{temp.getAbsolutePath()});
                Notification.show("Файл деконвертирован");
            } catch (Exception ex) {
                Notification.show("Ошибка: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        }

        private File saveTempFile() throws IOException {
            String filename = buffer.getFileName();
            File temp = File.createTempFile("upload_", filename);
            try (InputStream in = buffer.getInputStream();
                 OutputStream out = new FileOutputStream(temp)) {
                in.transferTo(out);
            }
            return temp;
        }
    }
} 