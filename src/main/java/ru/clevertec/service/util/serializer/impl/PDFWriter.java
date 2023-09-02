package ru.clevertec.service.util.serializer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import ru.clevertec.service.util.serializer.Writable;

@RequiredArgsConstructor
public class PDFWriter implements Writable {
    private final String fontPath;
    private final String destinationDir;

    @Override
    public void write(String content, String fileName) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            stream.beginText();
            InputStream fontStream = Objects.requireNonNull(getClass().getResourceAsStream(fontPath));
            PDType0Font font = PDType0Font.load(document, fontStream, false);
            stream.setFont(font, 12);
            stream.newLineAtOffset(25, 700);
            stream.setLeading(14.5f);
            String[] lines = content.split("\n");
            for (String line : lines) {
                stream.showText(line);
                stream.newLine();
            }
            stream.endText();
            stream.close();
            Path pathDir = Path.of(destinationDir);
            Files.createDirectories(pathDir);
            Path path = pathDir.resolve(fileName + ".pdf");
            document.save(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
