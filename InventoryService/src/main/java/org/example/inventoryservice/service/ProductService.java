package org.example.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventoryservice.dto.ProductRequest;
import org.example.inventoryservice.dto.ProductResponse;
import org.example.inventoryservice.exception.DuplicateResourceException;
import org.example.inventoryservice.exception.ResourceNotFoundException;
import org.example.inventoryservice.exception.InsufficientStockException;
import org.example.inventoryservice.model.Product;
import org.example.inventoryservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService{
    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest productRequest){
        if(productRepository.findBySku(productRequest.getSku()).isPresent()){
            throw new DuplicateResourceException("Product with SKU " + productRequest.getSku() + " already exists");
        }

        Product product = mapToProduct(productRequest);
        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());
        return mapToProductResponse(savedProduct);
    }

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        return mapToProductResponse(product);

    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(this::mapToProductResponse);
    }

    public ProductResponse updateProduct(Long id, ProductRequest productRequest){
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        if(!existingProduct.getSku().equals(productRequest.getSku()) && productRepository.findBySku(productRequest.getSku()).isPresent()){
            throw new DuplicateResourceException("Product with SKU " + productRequest.getSku() + " already exists");
        }
        existingProduct.setName(productRequest.getName());
        existingProduct.setSku(productRequest.getSku());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setStockQty(productRequest.getStockQty());
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated with ID: {} updated successfully", updatedProduct.getId());
        return mapToProductResponse(updatedProduct);
    }
    public void deleteProduct(Long id) {
        if(!productRepository.existsById(id)){
            throw new ResourceNotFoundException("Product with ID " + id + " not found ");
        }
        productRepository.deleteById(id);
        log.info("Product with ID: {} deleted successfully", id);
    }
    public Page<ProductResponse> searchProducts(String name, Pageable pageable) {
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return productPage.map(this::mapToProductResponse);
    }

    public ProductResponse decrementStock(Long id, int qty) {
        if (qty <= 0) {
            throw new org.example.inventoryservice.exception.ControllerException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        int available = product.getStockQty() == null ? 0 : product.getStockQty();
        if (available < qty) {
            throw new InsufficientStockException(id, qty, available);
        }
        product.setStockQty(available - qty);
        Product updated = productRepository.save(product);
        log.info("Decremented stock for product {} by {}, new stock {}", id, qty, updated.getStockQty());
        return mapToProductResponse(updated);
    }

    public int importProducts(MultipartFile file) {
        int imported = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                // optional header skip if first line contains non-numeric price
                if (lineNo == 1 && line.toLowerCase().contains("name") && line.toLowerCase().contains("sku")) {
                    continue; // skip header
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    log.warn("Skipping malformed line {}: {}", lineNo, line);
                    continue;
                }
                String name = parts[0].trim();
                String sku = parts[1].trim();
                String priceStr = parts[2].trim();
                String stockStr = parts[3].trim();
                try {
                    BigDecimal price = new BigDecimal(priceStr);
                    int stock = Integer.parseInt(stockStr);
                    if (sku.isEmpty()) {
                        log.warn("Skipping line {} empty sku", lineNo);
                        continue;
                    }
                    if (productRepository.findBySku(sku).isPresent()) {
                        // skip duplicates (could update instead)
                        log.info("Duplicate SKU {} skipped", sku);
                        continue;
                    }
                    Product p = new Product();
                    p.setName(name);
                    p.setSku(sku);
                    p.setPrice(price);
                    p.setStockQty(stock);
                    productRepository.save(p);
                    imported++;
                } catch (Exception ex) {
                    log.warn("Skipping line {} parse error: {}", lineNo, ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import products: " + e.getMessage(), e);
        }
        log.info("Imported {} products from file {}", imported, file.getOriginalFilename());
        return imported;
    }

    private Product mapToProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setSku(productRequest.getSku());
        product.setPrice(productRequest.getPrice());
        product.setStockQty(productRequest.getStockQty());
        return product;
    }
    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getStockQty()
        );
    }

}
