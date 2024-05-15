package leo.landau;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.Leading;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;

public class EveryPageStampWriter implements StampWriter {

    private static final String FONT_PATH = "/PDFForms/calibri.ttf";
    private static final String OUTPUT_FILE = "src/main/resources/stampedOnEveryPageFile.pdf";
    private static final Color COLOR = new DeviceRgb(44, 112, 186);
    private static final Border THIN_BORDER = new SolidBorder(COLOR, 1F);
    private static final float FONT_SIZE = 10;

    @Override
    public void createStamps() throws IOException {
        byte[] originalPdfContent = Files.readAllBytes(Paths.get("src/main/resources/doc.pdf"));

        try (ByteArrayInputStream bais = new ByteArrayInputStream(originalPdfContent);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            //obtain font
            PdfFont font = PdfFontFactory.createFont(FONT_PATH, PdfEncodings.IDENTITY_H);

            PdfReader pdfReader = new PdfReader(bais);
            //true to make it editable
            pdfReader.setUnethicalReading(true);

            try (PdfDocument pdf = new PdfDocument(pdfReader, new PdfWriter(baos));
                 Document doc = new Document(pdf)) {

                doc.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1));

                Table stamp = createStamp(font);
                placeStampOnEveryPage(pdf, stamp);
            }

            Path filePath = Paths.get(OUTPUT_FILE);
            Files.write(filePath, baos.toByteArray());
        }
    }

    private static void placeStampOnEveryPage(PdfDocument pdf, Table stamp) {
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, evt -> {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) evt;
            PdfCanvas pdfCanvas = new PdfCanvas(docEvent.getPage().newContentStreamAfter(), docEvent.getPage().getResources(), docEvent.getDocument());
            Canvas canvas = new Canvas(pdfCanvas, new Rectangle(0, 40));
            canvas.setBackgroundColor(DeviceRgb.GREEN);
            canvas.add(stamp);
        });
    }

    private static Table createStamp(PdfFont font) {
        return new Table(1)
                .setFixedPosition(0, 200, 20, 250)
                .setFont(font).setFontSize(FONT_SIZE)
                .setFontColor(COLOR)
                .addCell(new Cell().setBorder(THIN_BORDER)
                        .add(new Paragraph("ПОДПИСАНО ЭЛЕКТРОННОЙ ПОДПИСЬЮ").setBold().setTextAlignment(TextAlignment.CENTER))
                        .add(new Paragraph("Документ: 1234567890").setTextAlignment(TextAlignment.CENTER)));
    }

}
