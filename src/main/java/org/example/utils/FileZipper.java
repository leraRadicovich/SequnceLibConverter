package org.example.utils;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class FileZipper {
    public static File zipDirectory(File directoryToZip) throws IOException {
        String zipFileName = "result_" + System.currentTimeMillis() + ".zip";
        File zipFile = new File(System.getProperty("java.io.tmpdir"), zipFileName);

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Files.walk(directoryToZip.toPath())
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            ZipEntry zipEntry = new ZipEntry(directoryToZip.toPath().relativize(path).toString());
                            zos.putNextEntry(zipEntry);
                            zos.write(Files.readAllBytes(path));
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        return zipFile;
    }
}