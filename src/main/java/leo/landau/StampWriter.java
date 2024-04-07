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
import com.itextpdf.kernel.pdf.PdfPage;
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
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.properties.BorderCollapsePropertyValue;
import com.itextpdf.layout.properties.Leading;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;


public class StampWriter {

    private static final Color COLOR = new DeviceRgb(44, 112, 186);
    private static final Border THIN_BORDER = new SolidBorder(COLOR, 1F);
    private static final Border THICK_BORDER = new SolidBorder(COLOR, 2.5F);
    private static final float FONT_SIZE = 10;
    private static final float PAGE_MARGINS = 15;
    private static final float INTER_STAMP_GAP = 5;
    private static final float WARNING_HEIGHT = 30;
    private static final int STAMPS_PER_PAGE = 10;

    private StampWriter() {
    }

    public static void createStamps(byte[] originalPdfContent) throws IOException {
        int givenSignaturesNumber = 16; //or any other random number
        PdfFont font = PdfFontFactory.createFont(StampWriter.class.getResourceAsStream("/PDFForms/calibri.ttf").readAllBytes(), PdfEncodings.IDENTITY_H);
        ByteArrayInputStream bais = new ByteArrayInputStream(originalPdfContent);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfReader pdfReader = new PdfReader(bais);
        pdfReader.setUnethicalReading(true);

        try (PdfDocument pdf = new PdfDocument(pdfReader, new PdfWriter(baos));
             Document doc = new Document(pdf)) {

            //setting distance between the baselines -> property LEADING
            //Leading.MULTIPLIED - leading will depend on the font size, affecting the vertical distance between the baselines in accordance with the font size and the bounding box of the text
            //1 - coefficient that will be multiplied by the font size to determine the final vertical distance between the baselines of adjacent lines of text
            doc.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1));

            Table commonStamp = createCommonStamp(font);
            placeStampOnEveryPage(pdf, commonStamp);

            placeSignatureStampsOnTheLastPage(givenSignaturesNumber, font, pdf, doc);
        }

        String relativePath = "src/main/resources/stampedFile.pdf";
        Path filePath = Paths.get(System.getProperty("user.dir"), relativePath);
        Files.write(filePath, baos.toByteArray());
    }

    private static void placeSignatureStampsOnTheLastPage(int signaturesNumber, PdfFont font, PdfDocument pdf, Document doc) {
        int stampPage = pdf.getNumberOfPages();
        Table stampsTable = null;
        int addedSignaturesNumber = 0;

        do {

            //if it's no stamps by now or max stamps per page is reached -> create a new table of stamps
            if (addedSignaturesNumber % STAMPS_PER_PAGE == 0) {
                stampPage++; //we want stamps table on extra page
                stampsTable = createNewStampsTable(font);
            }

            stampsTable.addCell(createSignatureCell(addedSignaturesNumber++));

            //actually add content on the last page(s) only after reaching max stamps per page
            if (addedSignaturesNumber % STAMPS_PER_PAGE == 0 || addedSignaturesNumber == signaturesNumber) {
                LayoutResult result = stampsTable.createRendererSubTree().setParent(doc.getRenderer())
                        .layout(new LayoutContext(new LayoutArea(1, new Rectangle(1000, 1000))));

                stampsTable.setFixedPosition(stampPage, PAGE_MARGINS,
                        pdf.getDefaultPageSize().getTop() - PAGE_MARGINS - WARNING_HEIGHT - INTER_STAMP_GAP * 2 - result.getOccupiedArea().getBBox().getHeight(),
                        pdf.getDefaultPageSize().getWidth() - PAGE_MARGINS * 2);

                Paragraph warningMessageParagraph = createWarningMessage(font, pdf, stampPage);
                doc.add(warningMessageParagraph);
                doc.add(stampsTable);
            }
        } while (addedSignaturesNumber < signaturesNumber);
    }

    private static Table createNewStampsTable(PdfFont font) {
        return new Table(2) //how many columns in the table
                .setFont(font)
                .setFontSize(FONT_SIZE)
                .setFontColor(COLOR)
                .setBorderCollapse(BorderCollapsePropertyValue.SEPARATE)
                .setHorizontalBorderSpacing(INTER_STAMP_GAP)
                .setVerticalBorderSpacing(INTER_STAMP_GAP);
    }

    private static Paragraph createWarningMessage(PdfFont font, PdfDocument pdf, int stampPage) {
        return new Paragraph("Нижеприведённая информация о цифровых подписях без заверения собственноручной подписью " +
                "доверенного лица на бумаге или без личной проверки подлинности не действительна и может использоваться только в справочных целях.")
                .setFixedPosition(stampPage, PAGE_MARGINS + INTER_STAMP_GAP,
                        pdf.getDefaultPageSize().getTop() - PAGE_MARGINS - WARNING_HEIGHT,
                        pdf.getDefaultPageSize().getWidth() - PAGE_MARGINS * 4)
                .setFont(font)
                .setFontSize(FONT_SIZE)
                .setFontColor(COLOR)
                .setBold()
                .setPadding(5);
    }

    private static void placeStampOnEveryPage(PdfDocument pdf, Table commonStamp) {
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, evt -> {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) evt;
            PdfPage page = docEvent.getPage();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), docEvent.getDocument());
            Canvas canvas = new Canvas(pdfCanvas, new Rectangle(0, 40));
            canvas.setBackgroundColor(DeviceRgb.GREEN);
            canvas.add(commonStamp);
        });
    }

    private static Table createCommonStamp(PdfFont font) {
        return new Table(1).setFixedPosition(0, 200, 20, 250).setFont(font).setFontSize(FONT_SIZE).setFontColor(COLOR)
                .addCell(new Cell().setBorder(THIN_BORDER)
                        .add(new Paragraph("ПОДПИСАНО ЭЛЕКТРОННОЙ ПОДПИСЬЮ").setBold().setTextAlignment(TextAlignment.CENTER))
                        .add(new Paragraph("Документ: 1234567890").setTextAlignment(TextAlignment.CENTER)));
    }

    private static Cell createSignatureCell(int number) {
        var cell = new Cell().setBorder(THICK_BORDER);
        cell.add(new Paragraph("Princess Leia " + number).setBold());
        cell.add(new Paragraph("Certificate № Episode VI : Return of The Jedi"));
        cell.add(new Paragraph("Since: 1983"));
        return cell;
    }

}
