package com.cloud.aws.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
	@Table(name = "upload_pdf")
	public class UploadPdf {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	    
	    @Lob
	    private String content;
	    
	    private String email;
	    
	    @ManyToOne
	    @JoinColumn(name = "user_id") // Foreign key column
	    private User user;

	    
	    public User getUser() {
			return user;
		}
		public void setUser(User user) {
			this.user = user;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		// getters and setters
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		@Override
		public String toString() {
			return "UploadPdf [id=" + id + ", content=" + content + ", email=" + email + "]";
		}
		
	    
	}