package com.cloud.aws.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud.aws.model.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	//@Query("SELECT new com.cloud.aws.model.UserRequest(i.id, i.userid, i.fileName) FROM invoice i WHERE i.userid = :id")
   // List<Invoice> findByUserId(@Param("id") long id);

	List<Invoice> findByUserId(long id);

}
