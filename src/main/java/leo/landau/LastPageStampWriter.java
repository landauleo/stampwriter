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
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
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

public class LastPageStampWriter implements StampWriter {

    private static final String FONT_PATH = "/PDFForms/calibri.ttf";
    private static final String OUTPUT_FILE = "src/main/resources/stampedOnLastPageFile.pdf";
    private static final Integer STAMPS_PER_PAGE = 10;
    private static final Integer STAMPS_PER_LINE = 2;
    private static final Integer SIGNATURES_NUMBER = 16;
    private static final Color COLOR = new DeviceRgb(44, 112, 186);
    private static final Border THICK_BORDER = new SolidBorder(COLOR, 2.5F);
    private static final float FONT_SIZE = 10;
    private static final float PAGE_MARGINS = 15;
    private static final float INTER_STAMP_GAP = 5;
    private static final float WARNING_HEIGHT = 30;

    @Override
    public void createStamps() throws IOException {
        byte[] originalPdfContent = Files.readAllBytes(Paths.get("src/main/resources/doc.pdf"));

        try (ByteArrayInputStream bais = new ByteArrayInputStream(originalPdfContent);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfFont font = PdfFontFactory.createFont(FONT_PATH, PdfEncodings.IDENTITY_H);

            PdfReader pdfReader = new PdfReader(bais);
            pdfReader.setUnethicalReading(true);

            try (PdfDocument pdf = new PdfDocument(pdfReader, new PdfWriter(baos));
                 Document doc = new Document(pdf)) {

                doc.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1));

                placeSignatureStampsOnTheLastPage(font, pdf, doc);
            }

            Path filePath = Paths.get(OUTPUT_FILE);
            Files.write(filePath, baos.toByteArray());
        }
    }

    private static void placeSignatureStampsOnTheLastPage(PdfFont font, PdfDocument pdf, Document doc) {
        int currentPageNumber = pdf.getNumberOfPages();
        Table stampsTable = null;
        int addedSignaturesCounter = 0;

        do {
            //if there's no stamps by now or max stamps number per page is reached -> create a new table of stamps and new extra page
            if (addedSignaturesCounter % STAMPS_PER_PAGE == 0) {
                currentPageNumber++;
                stampsTable = createNewStampsTable(font);
            }

            stampsTable.addCell(createSignatureCell(addedSignaturesCounter++));

            //add content on the last page(s) only after reaching max stamps per page number or when the last signature is added
            if (addedSignaturesCounter % STAMPS_PER_PAGE == 0 || addedSignaturesCounter == SIGNATURES_NUMBER) {
                LayoutResult result = stampsTable
                        .createRendererSubTree()
                        .setParent(doc.getRenderer())
                        //pageNumber doesn't play any role, as we set fixed position below
                        .layout(new LayoutContext(new LayoutArea(0, new Rectangle(1000, 1000))));

                stampsTable.setFixedPosition(
                        currentPageNumber,
                        PAGE_MARGINS,
                        pdf.getDefaultPageSize().getTop() - PAGE_MARGINS - WARNING_HEIGHT - INTER_STAMP_GAP * 2 - result.getOccupiedArea().getBBox().getHeight(),
                        pdf.getDefaultPageSize().getWidth() - PAGE_MARGINS * 2);

                Paragraph warningMessageParagraph = createWarningMessage(font, pdf, currentPageNumber);
                doc.add(warningMessageParagraph);
                doc.add(stampsTable);
            }
        } while (addedSignaturesCounter < SIGNATURES_NUMBER);
    }

    private static Cell createSignatureCell(int number) {
        var cell = new Cell().setBorder(THICK_BORDER);
        cell.add(new Paragraph("Princess Leia " + number).setBold());
        cell.add(new Paragraph("Certificate № Episode VI : Return of The Jedi"));
        cell.add(new Paragraph("Since: 1983"));
        return cell;
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
                .setBold();
    }

    private static Table createNewStampsTable(PdfFont font) {
        return new Table(STAMPS_PER_LINE)
                .setFont(font)
                .setFontSize(FONT_SIZE)
                .setFontColor(COLOR)
                .setBorderCollapse(BorderCollapsePropertyValue.SEPARATE)
                .setHorizontalBorderSpacing(INTER_STAMP_GAP)
                .setVerticalBorderSpacing(INTER_STAMP_GAP);
    }

}
