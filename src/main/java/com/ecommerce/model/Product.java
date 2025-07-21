package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    private String category;
    
    @Column(nullable = false)
    private Integer stockQuantity;
}

