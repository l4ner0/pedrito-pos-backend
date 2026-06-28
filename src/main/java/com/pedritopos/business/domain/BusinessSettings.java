package com.pedritopos.business.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_settings")
@Getter
@Setter
@NoArgsConstructor
public class BusinessSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false, unique = true)
    private UUID businessId;

    @Column(name = "yape_number", length = 15)
    private String yapeNumber;

    @Column(name = "yape_qr_url", length = 500)
    private String yapeQrUrl;

    @Column(name = "print_enabled", nullable = false)
    private boolean printEnabled = true;

    @Column(name = "ticket_footer", length = 255)
    private String ticketFooter;
}
