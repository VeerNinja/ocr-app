package com.cloud.aws.model;

public class UserRequest {

	 private long userid;
	    private String fileName;
	    private long id;
	    private String email;
	    private String password;
		public long getUserid() {
			return userid;
		}
		public void setUserid(long userid) {
			this.userid = userid;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		@Override
		public String toString() {
			return "UserRequest [userid=" + userid + ", fileName=" + fileName + ", id=" + id + ", email=" + email
					+ ", password=" + password + "]";
		}
		
		
	    
}
