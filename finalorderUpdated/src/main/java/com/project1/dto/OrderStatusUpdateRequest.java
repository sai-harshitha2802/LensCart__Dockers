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
public class OrderStatusUpdateRequest {
    private List<Long> orderIds;
    private String status;
    
    
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public void setOrderIds(List<Long> orderIds) {
		this.orderIds = orderIds;
	}


	public List<Long> getOrderIds() {
		// TODO Auto-generated method stub
		return null;
	}
}