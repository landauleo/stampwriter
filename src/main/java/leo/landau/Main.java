package leo.landau;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import io.micronaut.runtime.Micronaut;

public class Main {

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/application.properties"));

            byte[] originalPdfContent = Files.readAllBytes(Paths.get("src/main/resources/doc.pdf"));

            StampWriter.createStamps(originalPdfContent, properties);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Micronaut.run(Main.class, args);
    }

}