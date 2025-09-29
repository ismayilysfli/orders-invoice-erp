package org.example.orderservice.repository;

import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(String customerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.totalAmount > :amount")
    List<Order> findOrdersWithTotalGreaterThan(@Param("amount") BigDecimal amount);

    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);

    // Monthly sales aggregation native query
    @Query(value = """
            SELECT EXTRACT(YEAR FROM o.order_date)  AS year,
                   EXTRACT(MONTH FROM o.order_date) AS month,
                   SUM(o.total_amount)             AS totalSales,
                   COUNT(*)                        AS orderCount,
                   AVG(o.total_amount)             AS avgOrderValue
            FROM orders o
            WHERE EXTRACT(YEAR FROM o.order_date) = :year
            GROUP BY year, month
            ORDER BY month
            """, nativeQuery = true)
    List<MonthlySalesRow> monthlySales(@Param("year") int year);

    interface MonthlySalesRow {
        Integer getYear();
        Integer getMonth();
        BigDecimal getTotalSales();
        Long getOrderCount();
        BigDecimal getAvgOrderValue();
    }

    @Query(value = """
            SELECT oi.product_id        AS productId,
                   SUM(oi.quantity)     AS totalQuantity,
                   SUM(oi.subtotal)     AS totalRevenue
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            WHERE EXTRACT(YEAR FROM o.order_date) = :year
            GROUP BY oi.product_id
            ORDER BY totalRevenue DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopProductRow> topProducts(@Param("year") int year, @Param("limit") int limit);

    interface TopProductRow {
        Long getProductId();
        Long getTotalQuantity();
        BigDecimal getTotalRevenue();
    }
}