package com.cloud.aws.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.aws.model.Invoice;
import com.cloud.aws.model.User;
import com.cloud.aws.model.UserRequest;
import com.cloud.aws.service.PDFExtractorService;
import com.cloud.aws.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private PDFExtractorService pdfExtractorService;



	@PostMapping("/createuser")
	public ResponseEntity<User> createUser(@RequestBody User user) {
		// Set the current date and time for creation
		user.setCreateDt(LocalDateTime.now());
		User createdUser = userService.createUser(user);
		return ResponseEntity.status(201).body(createdUser);
	}

	@GetMapping("/sign")
	public ResponseEntity<String> Sign(@RequestBody UserRequest loginRequest) {
		boolean isValidUser = userService.validateUser(loginRequest.getEmail(), loginRequest.getPassword());
		if (isValidUser) {
			return ResponseEntity.ok("Login successful");
		}
		return ResponseEntity.status(401).body("Invalid credentials");
	}

	@GetMapping("/reset")
	public ResponseEntity<Boolean> loginPwd(@PathVariable String emailid) {
		boolean isValidUserpwd = userService.validateUserPwd(emailid);
		if (isValidUserpwd) {
			return ResponseEntity.ok(isValidUserpwd);
		}
		return ResponseEntity.status(401).body(false);
	}
	
	@PostMapping("/updaetuser")
	public ResponseEntity<Boolean> UpdateUser(@RequestBody User user) {
		boolean isUser =false;
		//user.setCreateDt(LocalDateTime.now());
		User updateUser = userService.updateUser(user);
		if (updateUser !=null) {
			isUser=true;
		}else
			isUser=false;
		return ResponseEntity.status(201).body(isUser);
	}

	@PostMapping("/upload/{emailid}")
	public ResponseEntity<String> uploadInvoice(@RequestParam("file") MultipartFile file,@PathVariable String emailid) {
		Map<String, Object> extractedData =null;
		String uploadDir  = "D:\\SpringTest\\CloudWebApplication\\src\\main\\resources\\uploads";
		File directory =null;
		try {
			// Check if file is not empty
			if (file.isEmpty()) {
				return ResponseEntity.ok("File is empty");
			}

			if(!file.getOriginalFilename().contains("pdf")) {		
				User user = userService.isEmailVerified(emailid);
				  directory = new File(uploadDir);

				// Create the directory if it doesn't exist
				if (!directory.exists()) {
					directory.mkdirs();
				}
				
				if(user.getEmail() != null) {
				// Save the file locally
				String filePath = uploadDir + "\\" + file.getOriginalFilename();
				File localFile = new File(filePath);
				 extractedData = new HashMap<>();
				 extractedData.put("userId", user.getId());
				 extractedData.put("user_id", user.getId());
				 extractedData.put("filePath", filePath);
				 pdfExtractorService.saveInvoiceData(extractedData, file.getOriginalFilename());
				 file.transferTo(localFile);
					System.out.println("File saved at: " + localFile.getAbsolutePath());
				return ResponseEntity.ok("Image Upload Successfully");
				}
			}else {
			// Extract data from PDF
				InputStream inputStream = file.getInputStream();
				//pdfExtractorService.convertPdfToImage(inputStream,uploadDir);
			
			// Save the file to local directory (src/main/resources)
			 directory = new File(uploadDir);

			// Create the directory if it doesn't exist
			if (!directory.exists()) {
				directory.mkdirs();
			}

			// Save the file locally
			 String filePath = uploadDir + "\\" + file.getOriginalFilename();
			 File localFile = new File(filePath);
			 extractedData = pdfExtractorService.extractData(inputStream,
					file.getOriginalFilename(),emailid,localFile);
							
				file.transferTo(localFile);
				System.out.println("File saved at: " + localFile.getAbsolutePath());
			
			 
			return ResponseEntity.ok("Pdf Upload Succussfully");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Internal Error");
		}
		return null;
	}

	@GetMapping("/getPdf/{emailid}")
	public ResponseEntity<byte[]> getPDFile(@PathVariable String emailid) {
		try {
			File pdfFile = pdfExtractorService.getPDFById(emailid);
			byte[] content = Files.readAllBytes(pdfFile.toPath());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);
			headers.setContentDisposition(ContentDisposition.attachment().filename(pdfFile.getName()).build());

			return new ResponseEntity<>(content, headers, HttpStatus.OK);
		} catch (FileNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@GetMapping("/getdata/{emailid}")
	public ResponseEntity<List<Invoice>> getViewdata(@PathVariable String emailid) {
		List<Invoice> invoice = pdfExtractorService.getViewData(emailid);
		if (invoice.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // No invoices found
		}

		return ResponseEntity.ok(invoice); // Return the list of invoices
	}
}