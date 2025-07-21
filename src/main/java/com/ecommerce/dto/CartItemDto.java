
package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double subTotal;
}