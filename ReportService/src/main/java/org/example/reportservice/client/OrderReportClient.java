package org.example.reportservice.client;

import org.example.reportservice.dto.MonthlySalesDTO;
import org.example.reportservice.dto.TopProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Component
public class OrderReportClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public OrderReportClient(RestTemplate restTemplate,
                             @Value("${order.service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<MonthlySalesDTO> fetchMonthlySales(int year) {
        String url = baseUrl + "/internal/reports/monthly-sales?year=" + year;
        ResponseEntity<MonthlySalesDTO[]> resp = restTemplate.getForEntity(url, MonthlySalesDTO[].class);
        return Arrays.asList(resp.getBody() == null ? new MonthlySalesDTO[0] : resp.getBody());
    }

    public List<TopProductDTO> fetchTopProducts(int year, int limit) {
        String url = baseUrl + "/internal/reports/top-products?year=" + year + "&limit=" + limit;
        ResponseEntity<TopProductDTO[]> resp = restTemplate.getForEntity(url, TopProductDTO[].class);
        return Arrays.asList(resp.getBody() == null ? new TopProductDTO[0] : resp.getBody());
    }
}

