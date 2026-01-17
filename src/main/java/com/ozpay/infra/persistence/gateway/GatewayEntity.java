package com.ozpay.infra.persistence.gateway;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "gatewayName"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GatewayEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenentId;

    @Column(nullable = false)
    private String gatewayName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedCredential;

    private boolean isActive;

}
