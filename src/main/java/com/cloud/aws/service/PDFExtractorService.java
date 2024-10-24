package com.cloud.aws.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud.aws.model.Invoice;
import com.cloud.aws.model.User;

@Service
public class PDFExtractorService {

	@Autowired
	private DynamicSchemaService dynamicSchemaService;
	
	@Autowired
	private UserService userService;
	
	@Transactional
	public Map<String, Object> extractData(InputStream inputStream, String fileName,String emailid,File localFile) throws Exception {
		 User user  =null;
		try (PDDocument document = PDDocument.load(inputStream)) {
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(document);
			PDFRenderer pdfRenderer = new PDFRenderer(document);

	        for (int page = 0; page < document.getNumberOfPages(); ++page) {
	            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300); // DPI for quality
	            ImageIO.write(bim, "PNG", new File(localFile  +  ".png"));
	        }
			
	        user = userService.isEmailVerified(emailid);
			Map<String, Object> extractedData = new HashMap<>();
			extractedData.put("userid", user.getId());
			extractedData.put("user_id", user.getId());
			extractedData.put("filePath", localFile);
			// Extract invoice details
			extractedData.put("invoiceNumber", extractField(text, "Invoice\\s*Number\\s*([\\w-]+)"));
			extractedData.put("invoiceDate", extractField(text, "Invoice\\s*Date\\s*(\\d{2}/\\d{2}/\\d{4})"));

			extractedData.put("companyName", extractMultiLineField(text, "Company\\s*([\\s\\S]*?)(?=\\r?\\n|$)"));

			// Extract billing/shipping information
			extractedData.put("name", extractField(text, "Name\\s*([\\w\\s]+)"));
			extractedData.put("address", extractMultiLineField(text, "Address\\s*([\\s\\S]*?)(?=Email|$)"));
			extractedData.put("city", extractField(text, "([\\w\\s]+),\\s*[\\w\\s]+,\\s*\\d{5}"));
			extractedData.put("state", extractField(text, ",\\s*([\\w\\s]+),\\s*\\d{5}"));
			extractedData.put("zipCode", extractField(text, "(\\d{5})"));
			extractedData.put("country", extractField(text, "(United\\s*States)"));
			extractedData.put("email", extractField(text, "([\\w.-]+@[\\w.-]+\\.[\\w]+)"));

			// Extract total amount
			extractedData.put("totalAmount", extractField(text, "Total\\s*\\$(\\d+\\.\\d{2})"));

			// Extract product details
			extractedData.put("products", extractProducts(text));

			// Save extracted invoice data to the database
			saveInvoiceData(extractedData, fileName);

			return extractedData;
		}
	}

	@Transactional
	public void saveInvoiceData(Map<String, Object> extractedData, String fileName) throws Exception {
		// Assuming 'invoice' is the table name
		String tableName = "invoice";

		// Convert extractedData to String-based Map for DynamicSchemaService
		Map<String, String> stringData = new HashMap<>();
		extractedData.forEach((key, value) -> {
			// Convert value to string, handle null values
			stringData.put(key, value != null ? value.toString() : "");
		});

		// Add current date and time to the data map
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = LocalDateTime.now().format(formatter);
		stringData.put("created_at", currentDateTime);
		
		stringData.put("file_name", fileName);

		// Use the DynamicSchemaService to create/update the table and insert data
		dynamicSchemaService.updateSchemaAndStoreData(tableName, stringData);
	}

	private String extractField(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private String extractMultiLineField(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}

	private List<Map<String, String>> extractProducts(String text) {
		List<Map<String, String>> products = new ArrayList<>();
		Pattern pattern = Pattern.compile("(.*?)\\$([\\d.]+)\\s+(\\d+)\\s+\\$([\\d.]+)");
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			Map<String, String> product = new HashMap<>();
			product.put("description", matcher.group(1).trim());
			product.put("unitPrice", matcher.group(2));
			product.put("quantity", matcher.group(3));
			product.put("totalPrice", matcher.group(4));
			products.add(product);
		}

		return products;
	}

	public File getPDFById(String emailid) throws FileNotFoundException {
		User user = userService.isEmailVerified(emailid);
		File pdfFile  = null;
		if (user != null) {
			List<Invoice> invoice = userService.retrieveDataByUserId(user.getId());
			if (invoice != null && !invoice.isEmpty()) {
				for (int i = 0; i < invoice.size(); i++) {
					if (invoice.get(i).getUserId() == (user.getId())) {
						String uploadDir = "D:\\SpringTest\\CloudWebApplication\\src\\main\\resources\\uploads";
						pdfFile = new File(uploadDir, invoice.get(i).getFileName());
					}
				}
			}else 
				throw new FileNotFoundException("user is not found: " + user.getName());
		}else 
			throw new FileNotFoundException("user is not varified: " + emailid);
		if (!pdfFile.exists()) {
			throw new FileNotFoundException("PDF not found fro this User: " + user.getName());
		}
		return pdfFile;
	}
	
	public List<Invoice> getViewData(String emailid)  {
		User user = userService.isEmailVerified(emailid);
		File pdfFile  = null;
		List<Invoice> invoice =null;
		if (user != null) {
			 invoice = userService.retrieveDataByUserId(user.getId());
			if (invoice != null && !invoice.isEmpty()) {
				for (int i = 0; i < invoice.size(); i++) {
					if (invoice.get(i).getUserId() == (user.getId())) {
						return invoice;
					}
				}
			}else 
				throw new UsernameNotFoundException("View data is not found: " + user.getName());
		}else 
			throw new UsernameNotFoundException("user is not found: " + user.getName());
		
		return invoice;
	}
	
}
