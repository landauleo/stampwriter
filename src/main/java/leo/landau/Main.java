package leo.landau;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import io.micronaut.runtime.Micronaut;

public class Main {

    public static void main(String[] args) throws IOException {
        try {
            // Загрузка свойств из файла application.properties
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/application.properties"));

            // Загрузка содержимого PDF-файла
            byte[] originalPdfContent = Files.readAllBytes(Paths.get("src/main/resources/doc.pdf"));

            // Создание печатей на PDF-страницах согласно переданным свойствам
            StampWriter.createStamps(originalPdfContent, properties);

            System.out.println("Штампы успешно добавлены в PDF-файл.");
        } catch (IOException e) {
            System.err.println("Произошла ошибка при чтении файлов или свойств.");
            e.printStackTrace();
        }

        Micronaut.run(Main.class, args);
    }

}