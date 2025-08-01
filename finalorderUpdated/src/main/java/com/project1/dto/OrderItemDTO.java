package com.project1.dto;
 
import lombok.*;
 
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
 
    
	public OrderItemDTO(Object object, int quantity, double price, String imageUrl) {
		super();
		this.productId = object;
		this.quantity = quantity;
		this.price = price;
		this.imageUrl = imageUrl;
	}

	private String productId;
    
    private int quantity;
    
    private double price;
    
    private String imageUrl;
}