    package org.example.inventoryservice.service;

    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.example.inventoryservice.dto.ProductRequest;
    import org.example.inventoryservice.dto.ProductResponse;
    import org.example.inventoryservice.exception.DuplicateResourceException;
    import org.example.inventoryservice.exception.ResourceNotFoundException;
    import org.example.inventoryservice.model.Product;
    import org.example.inventoryservice.repository.ProductRepository;
    import org.springframework.stereotype.Service;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;

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
