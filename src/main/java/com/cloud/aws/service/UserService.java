package com.cloud.aws.service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloud.aws.model.Invoice;
import com.cloud.aws.model.User;
import com.cloud.aws.repository.InvoiceRepository;
import com.cloud.aws.repository.PdfDocumentRepository;
import com.cloud.aws.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository ;
    
    @Autowired 
    private PdfDocumentRepository pdfDocumentRepository;
    
    @Autowired
	private JavaMailSender mailSender;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

	private void sendVendorRegistrationConfirmationEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		message.setFrom("2024mt03108@wilp.bits-pilani.ac.in");
		mailSender.send(message);

	}
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
    	String subject ="Sign Up Request";
    	user.setPassword(passwordEncoder.encode(user.getPassword()));
    	sendVendorRegistrationConfirmationEmail(user.getEmail(), subject, "Hi "+ user.getName()+ "Your "+subject +" Successfully");
        return userRepository.save(user);
    }
    
	public boolean validateUser(String email, String password) {
		User user = userRepository.findByEmail(email);
		if (user != null && passwordEncoder.matches(password, user.getPassword())) {			
			return true;
		} else {
			return false; // User not found
		}
	}
	
	public boolean validateUserPwd(String email) {
		User user = userRepository.findByEmail(email);
		if (user != null) {			
			return true;
		} else {
			return false; // User not found
		}
	}

	public User isEmailVerified(String email) {
		// Check if the email exists and is verified
		User user = null;
		user = userRepository.findByEmail(email);
		if (user != null)
			return user;
		return user;
	}


	public List<Invoice> retrieveDataByUserId(Long id) {
		List<Invoice> inv=  invoiceRepository.findByUserId(id);
		return inv;
	}

	public User updateUser(User user) {
		String subject ="Password Reset Request";
		User user1 = userRepository.findByEmail(user.getEmail());
		user1.setCreateDt(LocalDateTime.now());
		user1.setName(user.getName() != null ? user.getName() : user1.getName());
    	user1.setPassword(passwordEncoder.encode(user.getPassword()));
    	sendVendorRegistrationConfirmationEmail(user.getEmail(), subject, "Hi "+ user.getName()+ "Your "+subject +" Successfully");
        return userRepository.save(user1);
	}
    
/*
 * public boolean checkPassword(String plaintextPassword, String hashedPassword)
 * { return passwordEncoder.matches(plaintextPassword, hashedPassword); } }
 */

}