package org.example.inventoryservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.ProductRequest;
import org.example.inventoryservice.dto.ProductResponse;
import org.example.inventoryservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping
    public Page<ProductResponse> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(
            @RequestParam String name,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return productService.searchProducts(name, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        return productService.updateProduct(id, productRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @PostMapping("/{id}/decrement")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse decrement(@PathVariable Long id, @RequestParam int qty) {
        return productService.decrementStock(id, qty);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    public String bulkImport(@RequestPart("file") MultipartFile file) {
        int count = productService.importProducts(file);
        return "Imported " + count + " products";
    }
}