package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Page<Product> searchProducts(String name, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (name != null && category != null) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(name, category, pageable);
        } else if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (category != null) {
            return productRepository.findByCategoryContainingIgnoreCase(category, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    public Product createProduct(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setStockQuantity(productDto.getStockQuantity());
        
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDto productDto) {
        Product product = getProductById(id);
        
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setStockQuantity(productDto.getStockQuantity());
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
