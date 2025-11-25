package org.example.service;

import org.example.converter.helper.EmbeddedLibInstaller;

import java.nio.file.Path;

/**
 * Сервис для работы с библиотеками (применение принципа Single Responsibility)
 */
public class LibraryService {
    
    /**
     * Копирует встроенную библиотеку в указанную директорию
     * @param targetDir директория для сохранения библиотеки
     * @throws Exception если произошла ошибка при копировании
     */
    public void copyEmbeddedLibrary(Path targetDir) throws Exception {
        EmbeddedLibInstaller.copyEmbeddedLibrary(targetDir);
    }
    
    /**
     * Вычисляет относительный путь от источника к библиотеке
     * @param sourcePath путь к исходному файлу
     * @param libDir директория библиотеки
     * @return относительный или абсолютный путь к SequenceLibIncludeFile_v4.puml
     */
    public String calculateIncludePath(Path sourcePath, String libDir) {
        if (libDir == null) {
            return null;
        }
        
        Path libPath = Path.of(libDir).resolve("sequenceLibPuml/SequenceLibIncludeFile_v4.puml").normalize();
        Path sourceParent = sourcePath.getParent();
        
        if (sourceParent == null) {
            return libPath.toAbsolutePath().toString().replace("\\", "/");
        }
        
        try {
            Path absoluteLibPath = libPath.toAbsolutePath();
            Path absoluteSourceParent = sourceParent.toAbsolutePath();
            Path relativePath = absoluteSourceParent.relativize(absoluteLibPath);
            String result = relativePath.normalize().toString().replace("\\", "/");
            // Убеждаемся, что путь начинается с ../ если это относительный путь
            return result;
        } catch (IllegalArgumentException e) {
            // Разные диски в Windows или другие проблемы - используем абсолютный путь
            return libPath.toAbsolutePath().toString().replace("\\", "/");
        }
    }
    
    /**
     * Вычисляет путь для include в результирующем файле конвертации
     * @param outputFilePath путь к результирующему файлу (_bylib.puml)
     * @param libDir директория библиотеки
     * @return путь для include директивы
     */
    public String calculateIncludePathForOutput(Path outputFilePath, String libDir) {
        return calculateIncludePath(outputFilePath, libDir);
    }
}

