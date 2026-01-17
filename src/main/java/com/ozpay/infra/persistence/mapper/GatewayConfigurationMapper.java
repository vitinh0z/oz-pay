package com.ozpay.infra.persistence.mapper;

import com.ozpay.domain.entity.Gateway;
import com.ozpay.infra.persistence.gateway.GatewayEntity;
import com.ozpay.infra.security.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GatewayConfigurationMapper {

    private final ObjectMapper objectMapper;
    private final CryptoService cryptoService;

    public GatewayEntity toEntity (Gateway gateway){
        if (gateway == null) return null;

        try{
            String json = objectMapper.writeValueAsString(gateway.getCredential());
            String encrypted = cryptoService.encrypted(json);

            return new GatewayEntity(
                    gateway.getId(),
                    gateway.getTenentId(),
                    gateway.getGatewayName(),
                    encrypted,
                    gateway.isActive()
            );
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public Gateway toDomain (GatewayEntity gatewayEntity){
        if (gatewayEntity == null) return null;

        try {
            String json = cryptoService.descrypt(gatewayEntity.getEncryptedCredential());

            Map<String, String> credentials = objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, String>>() {}
            );

            return new Gateway(
                    gatewayEntity.getId(),
                    gatewayEntity.getTenentId(),
                    gatewayEntity.getGatewayName(),
                    credentials,
                    gatewayEntity.isActive()
            );

        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
