package com.motete.mango.ecommerce_backend.service;

import com.motete.mango.ecommerce_backend.model.Product;
import com.motete.mango.ecommerce_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts() {
        return  productRepository.findAll();
    }
}
