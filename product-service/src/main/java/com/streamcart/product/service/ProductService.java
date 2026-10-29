package com.streamcart.product.service;

import com.streamcart.product.dto.ProductRequest;
import com.streamcart.product.dto.ProductResponse;
import com.streamcart.product.exception.ProductNotFoundException;
import com.streamcart.product.model.Product;
import com.streamcart.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String CACHE_NAME = "products";

    private final ProductRepository productRepository;

    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stock(request.getStock())
                .build();

        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // First call reads from MongoDB and stores the result in Redis under key "products::{id}".
    // Subsequent calls for the same id are served straight from Redis until the TTL expires
    // or the entry is evicted below.
    @Cacheable(cacheNames = CACHE_NAME, key = "#id")
    public ProductResponse findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    // Removing the stale cache entry ensures the next findById() call re-reads from MongoDB.
    @CacheEvict(cacheNames = CACHE_NAME, key = "#id")
    public ProductResponse update(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock());

        return toResponse(productRepository.save(product));
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#id")
    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stock(product.getStock())
                .build();
    }
}
