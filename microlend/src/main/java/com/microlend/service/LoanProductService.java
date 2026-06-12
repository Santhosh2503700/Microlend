package com.microlend.service;

import com.microlend.dto.request.LoanProductRequest;
import com.microlend.entity.LoanProduct;
import com.microlend.enums.ProductStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanProductService {

    private final LoanProductRepository productRepository;

    public LoanProduct create(LoanProductRequest req) {
        LoanProduct product = LoanProduct.builder()
                .productName(req.getProductName())
                .category(req.getCategory())
                .minAmount(req.getMinAmount())
                .maxAmount(req.getMaxAmount())
                .tenureMonths(req.getTenureMonths())
                .interestRatePercent(req.getInterestRatePercent())
                .interestType(req.getInterestType())
                .processingFeePercent(req.getProcessingFeePercent())
                .status(req.getStatus() != null ? req.getStatus() : ProductStatus.ACTIVE)
                .build();
        return productRepository.save(product);
    }

    public List<LoanProduct> getAll() {
        return productRepository.findAll();
    }

    public LoanProduct getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product not found: " + id));
    }

    public LoanProduct update(Long id, LoanProductRequest req) {
        LoanProduct product = getById(id);
        product.setProductName(req.getProductName());
        if (req.getMinAmount() != null) product.setMinAmount(req.getMinAmount());
        if (req.getMaxAmount() != null) product.setMaxAmount(req.getMaxAmount());
        if (req.getInterestRatePercent() != null) product.setInterestRatePercent(req.getInterestRatePercent());
        if (req.getStatus() != null) product.setStatus(req.getStatus());
        return productRepository.save(product);
    }

    public void discontinue(Long id) {
        LoanProduct product = getById(id);
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }
}
