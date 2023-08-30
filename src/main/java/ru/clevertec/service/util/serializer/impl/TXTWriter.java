package ru.clevertec.service.util.serializer.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.util.serializer.Writable;

@RequiredArgsConstructor
public class TXTWriter implements Writable {
    private final String destDir;

    @Override
    public void write(String content, String fileName) {
        String classesRoot = Objects.requireNonNull(PDFWriter.class.getResource("/")).getPath();

        File file = new File(classesRoot);
        String root;
        try {
            root = file.getParentFile().getParentFile().getParentFile().getCanonicalPath();
            String dest = root + "/" + destDir;
            Path pathDir = Path.of(dest);
            Files.createDirectories(pathDir);
            fileName = pathDir + "/" + fileName + ".txt";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
