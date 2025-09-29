package org.example.reportservice.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.reportservice.client.OrderReportClient;
import org.example.reportservice.dto.MonthlySalesDTO;
import org.example.reportservice.dto.TopProductDTO;
import org.example.reportservice.util.CsvUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/reports")
@Validated
public class ReportController {

    private final OrderReportClient client;

    public ReportController(OrderReportClient client) { this.client = client; }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/monthly-sales", produces = "text/csv")
    public ResponseEntity<byte[]> monthlySalesCsv(@RequestParam @Min(2000) @Max(2100) int year) {
        String csv = CsvUtil.monthlySalesToCsv(client.fetchMonthlySales(year));
        return csvResponse(csv, "monthly-sales-" + year + ".csv");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/top-products", produces = "text/csv")
    public ResponseEntity<byte[]> topProductsCsv(@RequestParam @Min(2000) @Max(2100) int year,
                                                 @RequestParam(defaultValue = "10") @Positive @Max(100) int limit) {
        String csv = CsvUtil.topProductsToCsv(client.fetchTopProducts(year, limit));
        return csvResponse(csv, "top-products-" + year + ".csv");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/monthly-sales.xlsx")
    public ResponseEntity<byte[]> monthlySalesExcel(@RequestParam @Min(2000) @Max(2100) int year) {
        List<MonthlySalesDTO> rows = client.fetchMonthlySales(year);
        byte[] bytes = buildMonthlySalesWorkbook(rows);
        return excelResponse(bytes, "monthly-sales-" + year + ".xlsx");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/top-products.xlsx")
    public ResponseEntity<byte[]> topProductsExcel(@RequestParam @Min(2000) @Max(2100) int year,
                                                    @RequestParam(defaultValue = "10") @Positive @Max(100) int limit) {
        List<TopProductDTO> rows = client.fetchTopProducts(year, limit);
        byte[] bytes = buildTopProductsWorkbook(rows);
        return excelResponse(bytes, "top-products-" + year + ".xlsx");
    }

    private ResponseEntity<byte[]> csvResponse(String csv, String filename) {
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(body.length)
                .body(body);
    }

    private ResponseEntity<byte[]> excelResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private byte[] buildMonthlySalesWorkbook(List<MonthlySalesDTO> rows) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("MonthlySales");
            int r = 0; Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("Year");
            header.createCell(1).setCellValue("Month");
            header.createCell(2).setCellValue("TotalSales");
            header.createCell(3).setCellValue("OrderCount");
            header.createCell(4).setCellValue("AvgOrderValue");
            for (MonthlySalesDTO dto : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(dto.year());
                row.createCell(1).setCellValue(dto.month());
                row.createCell(2).setCellValue(dto.totalSales().doubleValue());
                row.createCell(3).setCellValue(dto.orderCount());
                row.createCell(4).setCellValue(dto.avgOrderValue().doubleValue());
            }
            autosize(sheet,5);
            wb.write(bos);
            return bos.toByteArray();
        } catch(Exception e){ throw new RuntimeException("Failed to build Excel", e);}    }

    private byte[] buildTopProductsWorkbook(List<TopProductDTO> rows) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("TopProducts");
            int r = 0; Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("ProductId");
            header.createCell(1).setCellValue("TotalQuantity");
            header.createCell(2).setCellValue("TotalRevenue");
            for (TopProductDTO dto : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(dto.productId());
                row.createCell(1).setCellValue(dto.totalQuantity());
                row.createCell(2).setCellValue(dto.totalRevenue().doubleValue());
            }
            autosize(sheet,3);
            wb.write(bos);
            return bos.toByteArray();
        } catch(Exception e){ throw new RuntimeException("Failed to build Excel", e);}    }

    private void autosize(Sheet sheet, int cols){
        for(int i=0;i<cols;i++){ sheet.autoSizeColumn(i);} }
}

