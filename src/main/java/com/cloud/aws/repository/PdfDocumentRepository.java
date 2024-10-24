package com.cloud.aws.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloud.aws.model.UploadPdf;

public interface PdfDocumentRepository extends JpaRepository<UploadPdf, Long> {
}
