package com.ozpay.infra.persistence.repository.gateway;

import com.ozpay.domain.entity.Gateway;
import com.ozpay.domain.repository.gateway.GatewayConfigurationRepository;
import com.ozpay.infra.persistence.gateway.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GatewayJpaRepository extends JpaRepository<GatewayEntity, UUID> {

    Optional<GatewayEntity> findByTenantIdAndGatewayName(UUID tenentId, String gatewayName);
}
