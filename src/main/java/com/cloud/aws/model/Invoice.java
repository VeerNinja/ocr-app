package com.cloud.aws.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Invoice {
  
	@Id
    private Long id;
    private Long userId;  // Make sure this matches exactly
    private String fileName;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	@Override
	public String toString() {
		return "Invoice [id=" + id + ", userId=" + userId + ", fileName=" + fileName + "]";
	}

    // Getters and Setters
    
}
