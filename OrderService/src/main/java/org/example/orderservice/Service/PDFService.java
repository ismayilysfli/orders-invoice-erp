package org.example.orderservice.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfService {

    @Value("${invoice.directory:/data/uploads/invoices}")
    private String invoiceDirectory;

    public String generateInvoicePdf(OrderResponse order) {
        try {
            File dir = new File(invoiceDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "invoice_" + order.getOrderNumber() + ".pdf";
            String filePath = invoiceDirectory + File.separator + fileName;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            Font detailsFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            document.add(new Paragraph("Invoice Number: " + order.getOrderNumber(), detailsFont));
            document.add(new Paragraph("Order Date: " +
                    order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), detailsFont));
            document.add(new Paragraph("Customer: " + order.getCustomerName(), detailsFont));
            document.add(new Paragraph("Email: " + order.getCustomerEmail(), detailsFont));

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            PdfPCell header1 = new PdfPCell(new Phrase("Product", tableHeaderFont));
            PdfPCell header2 = new PdfPCell(new Phrase("Quantity", tableHeaderFont));
            PdfPCell header3 = new PdfPCell(new Phrase("Unit Price", tableHeaderFont));
            PdfPCell header4 = new PdfPCell(new Phrase("Total", tableHeaderFont));

            header1.setBackgroundColor(BaseColor.DARK_GRAY);
            header2.setBackgroundColor(BaseColor.DARK_GRAY);
            header3.setBackgroundColor(BaseColor.DARK_GRAY);
            header4.setBackgroundColor(BaseColor.DARK_GRAY);

            table.addCell(header1);
            table.addCell(header2);
            table.addCell(header3);
            table.addCell(header4);

            Font tableFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
            order.getItems().forEach(item -> {
                table.addCell(new Phrase(item.getProductName(), tableFont));
                table.addCell(new Phrase(item.getQuantity().toString(), tableFont));
                table.addCell(new Phrase("$" + item.getUnitPrice(), tableFont));
                table.addCell(new Phrase("$" + item.getTotalPrice(), tableFont));
            });

            document.add(table);

            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Paragraph total = new Paragraph("Total Amount: $" + order.getTotalAmount(), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();

            log.info("Invoice PDF generated: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error generating PDF invoice: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}