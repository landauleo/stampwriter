package leo.landau;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.micronaut.runtime.Micronaut;

public class Main {

    public static void main(String[] args) throws IOException {
        String relativePath = "src/main/resources/doc.pdf";
        Path filePath = Paths.get(System.getProperty("user.dir"), relativePath);
        byte[] content = Files.readAllBytes(filePath);

        StampWriter.createStamps(content);

        Micronaut.run(Main.class, args);
    }

}