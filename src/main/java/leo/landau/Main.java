package leo.landau;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.micronaut.runtime.Micronaut;

public class Main {

    public static void main(String[] args) throws IOException {
        byte[] content = Files.readAllBytes(Path.of("/Users/ruanaaf/Downloads/github/stampwriter/src/main/resources/doc.pdf"));

        StampWriter.createStamps(content);

        Micronaut.run(Main.class, args);
    }

}