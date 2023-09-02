package ru.clevertec.service.util.serializer.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.util.serializer.Writable;

@RequiredArgsConstructor
public class TXTWriter implements Writable {
    private final String destDir;

    @Override
    public void write(String content, String fileName) {
        try {
            Path pathDir = Path.of(destDir);
            Files.createDirectories(pathDir);
            Path path = pathDir.resolve(fileName + ".txt");
            try (FileWriter writer = new FileWriter(path.toFile())) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
