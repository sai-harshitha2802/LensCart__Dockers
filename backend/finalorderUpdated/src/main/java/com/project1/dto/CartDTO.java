package com.project1.dto;
 
 
import java.util.List;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
	private Long customerId;
    private List<CartItemDTO> cartItems;
    private double totalAmount;
    
    
	// public Long getCustomerId() {
	// 	return customerId;
	// }


	// public void setCustomerId(Long customerId) {
	// 	this.customerId = customerId;
	// }


	// public double getTotalAmount() {
	// 	return totalAmount;
	// }


	// public void setTotalAmount(double totalAmount) {
	// 	this.totalAmount = totalAmount;
	// }


	// public void setCartItems(List<CartItemDTO> cartItems) {
	// 	this.cartItems = cartItems;
	// }


	// public String getCartItems() {
	// 	// TODO Auto-generated method stub
	// 	return null;
	// } 
}