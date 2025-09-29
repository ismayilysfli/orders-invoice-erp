package org.example.reportservice.util;

import org.example.reportservice.dto.MonthlySalesDTO;
import org.example.reportservice.dto.TopProductDTO;

import java.util.List;

public final class CsvUtil {
    private CsvUtil() {}

    public static String monthlySalesToCsv(List<MonthlySalesDTO> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("year,month,totalSales,orderCount,avgOrderValue\n");
        for (MonthlySalesDTO r : rows) {
            sb.append(r.year()).append(',')
              .append(r.month()).append(',')
              .append(r.totalSales()).append(',')
              .append(r.orderCount()).append(',')
              .append(r.avgOrderValue())
              .append('\n');
        }
        return sb.toString();
    }

    public static String topProductsToCsv(List<TopProductDTO> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("productId,totalQuantity,totalRevenue\n");
        for (TopProductDTO r : rows) {
            sb.append(r.productId()).append(',')
              .append(r.totalQuantity()).append(',')
              .append(r.totalRevenue())
              .append('\n');
        }
        return sb.toString();
    }
}

