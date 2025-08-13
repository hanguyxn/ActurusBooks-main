package com.example.bookstore.service;

import com.example.bookstore.entity.Orders;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class InvoiceService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generateInvoicePDF(Orders order) throws IOException {
        try {
            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("order", order);

            // Process HTML template
            String htmlContent = templateEngine.process("invoice/invoice-template", context);

            // Convert HTML to PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new IOException("Error generating PDF invoice", e);
        }
    }

    public String generateBarcode(String invoiceNumber) {
        return "||||| " + invoiceNumber + " |||||";
    }

    public String getInvoiceFilename(Orders order) {
        return "invoice_" + order.getInvoice_number() + "_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf";
    }
}