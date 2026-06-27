package com.pedritopos.sale.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pedritopos.product.domain.Product;
import com.pedritopos.product.repositories.ProductRepository;
import com.pedritopos.sale.domain.Sale;
import com.pedritopos.sale.domain.SaleItem;
import com.pedritopos.sale.dto.request.SaleItemRequest;
import com.pedritopos.sale.dto.request.SaleRequest;
import com.pedritopos.sale.dto.response.SaleItemResponse;
import com.pedritopos.sale.dto.response.SaleResponse;
import com.pedritopos.sale.repositories.SaleItemRepository;
import com.pedritopos.sale.repositories.SaleRepository;
import com.pedritopos.shared.dto.PagedResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public SaleResponse create(UUID businessId, UUID userId, SaleRequest request) {
        List<SaleItem> saleItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (SaleItemRequest itemReq : request.items()) {
            Product product = productRepository.findByIdAndBusinessId(itemReq.productId(), businessId)
                    .filter(Product::isActive)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.productId()));

            if (product.getStock() < itemReq.quantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getName());
            }

            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            subtotal = subtotal.add(lineTotal);

            SaleItem item = new SaleItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(itemReq.quantity());
            item.setLineTotal(lineTotal);
            saleItems.add(item);
        }

        BigDecimal discount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) {
            throw new RuntimeException("El descuento no puede ser mayor al subtotal");
        }
        BigDecimal total = subtotal.subtract(discount);

        BigDecimal amountReceived = null;
        BigDecimal changeGiven = null;
        if ("EFECTIVO".equals(request.paymentMethod())) {
            if (request.amountReceived() == null) {
                throw new RuntimeException("Se requiere el monto recibido para pago en efectivo");
            }
            if (request.amountReceived().compareTo(total) < 0) {
                throw new RuntimeException("El monto recibido es insuficiente");
            }
            amountReceived = request.amountReceived();
            changeGiven = amountReceived.subtract(total);
        }

        Sale sale = new Sale();
        sale.setBusinessId(businessId);
        sale.setUserId(userId);
        sale.setTicketCode(String.format("#%04d", saleRepository.nextTicketNumber()));
        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discount);
        sale.setTotal(total);
        sale.setPaymentMethod(request.paymentMethod());
        sale.setAmountReceived(amountReceived);
        sale.setChangeGiven(changeGiven);
        saleRepository.save(sale);

        for (SaleItem item : saleItems) {
            item.setSaleId(sale.getId());
        }
        saleItemRepository.saveAll(saleItems);

        return toResponse(sale, saleItems);
    }

    public PagedResponse<SaleResponse> findAll(UUID businessId, String ticketCode, String paymentMethod,
            String status, Instant from, Instant to, int page, int size) {
        return PagedResponse.from(
                saleRepository.findByBusiness(businessId, blankToNull(ticketCode), paymentMethod, status, from, to,
                        PageRequest.of(page - 1, size))
                        .map(sale -> toResponse(sale, saleItemRepository.findBySaleId(sale.getId()))));
    }

    public SaleResponse findById(UUID businessId, UUID id) {
        Sale sale = saleRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        return toResponse(sale, saleItemRepository.findBySaleId(sale.getId()));
    }

    @Transactional
    public SaleResponse cancel(UUID businessId, UUID id) {
        Sale sale = saleRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if ("CANCELLED".equals(sale.getStatus())) {
            throw new RuntimeException("La venta ya fue anulada");
        }

        saleItemRepository.findBySaleId(sale.getId()).forEach(item -> {
            productRepository.findByIdAndBusinessId(item.getProductId(), businessId)
                    .filter(Product::isActive)
                    .ifPresent(product -> {
                        product.setStock(product.getStock() + item.getQuantity());
                        productRepository.save(product);
                    });
        });

        sale.setStatus("CANCELLED");
        saleRepository.save(sale);

        return toResponse(sale, saleItemRepository.findBySaleId(sale.getId()));
    }

    private String blankToNull(String value) {
        return (value != null && !value.isBlank()) ? value.trim() : null;
    }

    private SaleResponse toResponse(Sale sale, List<SaleItem> items) {
        List<SaleItemResponse> itemResponses = items.stream()
                .map(i -> new SaleItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()))
                .toList();

        return new SaleResponse(
                sale.getId(),
                sale.getUserId(),
                sale.getTicketCode(),
                sale.getSubtotal(),
                sale.getDiscountAmount(),
                sale.getTotal(),
                sale.getPaymentMethod(),
                sale.getAmountReceived(),
                sale.getChangeGiven(),
                itemResponses,
                sale.getStatus(),
                sale.getCreatedAt());
    }

}
