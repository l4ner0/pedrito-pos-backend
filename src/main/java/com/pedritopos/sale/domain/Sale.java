package com.pedritopos.sale.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.pedritopos.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
public class Sale extends BaseEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 30)
    private String ticketCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "amount_received", precision = 10, scale = 2)
    private BigDecimal amountReceived;

    @Column(name = "change_given", precision = 10, scale = 2)
    private BigDecimal changeGiven;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
}
